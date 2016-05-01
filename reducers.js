/**
 * @flow
 */

import * as Immutable from 'immutable';

import {evaluateUserExpr, canStepUserExpr} from './UserExpressionEvaluator'
import type {
    Action,
    ScreenExpression,
    UserExpression,
    PathComponent,
    State
} from './types'
import * as t from './types'
import {resolveTouch} from './HitTester'

const initialState: State = t.newState(
    new Immutable.Map(), 0, new Immutable.Map());

// TODO: Consider adding a top-level try/catch.
const playgroundApp = (state: State = initialState, action: Action): State => {
    // Despite our action union, there are some internal redux actions that
    // start with @@, which we want to just ignore.
    if (action.type.startsWith('@@')) {
        return state;
    }

    const exprWithId = (exprId: number): ScreenExpression => {
        const result = state.screenExpressions.get(exprId);
        if (!result) {
            throw new Error('Expected expression with ID ' + exprId);
        }
        return result;
    };

    return t.matchAction(action, {
        reset: () => initialState,
        addExpression: ({screenExpr}) => addExpression(state, screenExpr),
        moveExpression: ({exprId, pos}) => {
            return state.updateScreenExpressions((exprs) =>
                exprs.update(exprId, (screenExpr) =>
                    screenExpr.withPos(pos)));
        },
        decomposeExpression: ({path: {exprId, pathSteps}, targetPos}) => {
            const existingScreenExpr = exprWithId(exprId);
            const {original, extracted} = decomposeExpression(
                existingScreenExpr.expr, pathSteps);
            state = addExpression(
                state, t.newScreenExpression(extracted, targetPos));
            state = modifyExpression(state, exprId,
                () => existingScreenExpr.withExpr(original));
            return state;
        },
        insertAsArg: ({argExprId, path: {exprId, pathSteps}}) => {
            const argScreenExpr = exprWithId(argExprId);
            const targetScreenExpr = exprWithId(exprId);
            const resultExpr = insertAsArg(
                targetScreenExpr.expr, argScreenExpr.expr, pathSteps);
            const newScreenExpr = targetScreenExpr.withExpr(resultExpr);
            return state.updateScreenExpressions((exprs) =>
                exprs.remove(argExprId).set(exprId, newScreenExpr));
        },
        insertAsBody: ({bodyExprId, path: {exprId, pathSteps}}) => {
            const bodyScreenExpr = exprWithId(bodyExprId);
            const targetScreenExpr = exprWithId(exprId);
            const resultExpr = insertAsBody(
                targetScreenExpr.expr, bodyScreenExpr.expr, pathSteps);
            const newScreenExpr = targetScreenExpr.withExpr(resultExpr);
            return state.updateScreenExpressions((exprs) =>
                exprs.remove(bodyExprId).set(exprId, newScreenExpr));
        },
        evaluateExpression: ({exprId, targetPos}) => {
            const existingScreenExpr = exprWithId(exprId);
            if (!canStepUserExpr(existingScreenExpr.expr)) {
                return state;
            }
            const evaluatedExpr = evaluateUserExpr(existingScreenExpr.expr);
            if (!evaluatedExpr) {
                return state;
            }
            return addExpression(
                state, t.newScreenExpression(evaluatedExpr, targetPos));
        },
        fingerDown: ({fingerId, screenPos}) => {
            const exprId = resolveTouch(state, screenPos);
            if (exprId === null || exprId === undefined) {
                console.log("Touch didn't match anything.");
                return state;
            }
            return state.updateActiveDrags((drags) => drags.set(fingerId, exprId));
        },
        fingerMove: ({fingerId, screenPos}) => {
            const dragData = state.activeDrags.get(fingerId);
            if (!dragData) {
                return state;
            }
            const {exprId, offsetX, offsetY} = dragData;
            const {screenX, screenY} = screenPos;
            return modifyExpression(
                state, exprId, (expr) =>
                    expr.withPos(t.newCanvasPoint(
                        screenX - offsetX, screenY - offsetY)));
        },
        fingerUp: ({fingerId, screenPos}) => {
            const exprId = resolveTouch(state, screenPos);
            return state.withActiveDrags(
                state.activeDrags.remove(fingerId)
            );
        },
    });
};

const addExpression = (state: State, screenExpr: ScreenExpression): State => {
    const nextExprId = state.nextExprId;
    return state
        .updateScreenExpressions((exprs) => exprs.set(nextExprId, screenExpr))
        .withNextExprId(nextExprId + 1);
};

type Transform<T> = (t: T) => T;

const modifyExpression = (state: State, exprId: number,
                          transform: Transform<ScreenExpression>): State => {
    return state.updateScreenExpressions((exprs) =>
        exprs.update(exprId, transform)
    );
};

type DecomposeResult = {
    original: UserExpression,
    extracted: UserExpression,
}

/**
 * Assuming that the given expression is either a lambda with a body or a
 * function call, extract the body or argument. The "original" result is the
 * original expression with the child removed, and the "extracted" result is the
 * removed child.
 */
const decomposeExpression = (expr: UserExpression,
                             path: Immutable.List<PathComponent>):
        DecomposeResult => {
    let extracted = null;
    const original = transformAtPath(expr, path, (expr) => {
        if (expr.type === 'userLambda' && expr.body) {
            extracted = expr.body;
            return expr.withBody(null);
        } else if (expr.type === 'userFuncCall') {
            extracted = expr.arg;
            return expr.func;
        }
        throw new Error('Unexpected expression to decompose.');
    });
    if (!extracted) {
        throw new Error('Expected transform to be called exactly once.');
    }
    return {
        original,
        extracted,
    }
};

const insertAsArg = (targetExpr: UserExpression, newArgExpr: UserExpression,
                     path: Immutable.List<PathComponent>): UserExpression => {
    return transformAtPath(targetExpr, path, (expr) =>
        t.newUserFuncCall(expr, newArgExpr)
    );
};

const insertAsBody = (targetExpr: UserExpression, newBodyExpr: UserExpression,
                      path: Immutable.List<PathComponent>): UserExpression => {
    return transformAtPath(targetExpr, path, (expr) => {
        if (expr.type !== 'userLambda' || expr.body) {
            throw new Error('Invalid expression to insert body into.');
        }
        return expr.withBody(newBodyExpr);
    });
};


/**
 * Given a path to a part of an expression, run a transformation at that
 * position and return the resulting top-level expression.
 *
 * Throws an exception if the path was invalid.
 */
const transformAtPath = (
        expr: UserExpression, path: Immutable.List<PathComponent>,
        transform: Transform<UserExpression>): UserExpression => {
    if (path.size === 0) {
        return transform(expr);
    }
    return updateChild(expr, path.get(0), (child) => {
        return transformAtPath(child, path.slice(1), transform);
    });
};

const updateChild = (expr: UserExpression, step: PathComponent,
                     updater: Transform<UserExpression>): UserExpression => {
    if (step === 'func' && expr.type === 'userFuncCall') {
        return expr.updateFunc(updater);
    } else if (step === 'arg' && expr.type === 'userFuncCall') {
        return expr.updateArg(updater);
    } else if (step === 'body' && expr.type === 'userLambda' && expr.body) {
        return expr.withBody(updater(expr.body));
    }
    throw new Error('Unexpected step: ' + JSON.stringify(step));
};

export default playgroundApp;
