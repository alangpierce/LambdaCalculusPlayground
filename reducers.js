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
            return modifyExpression(state, exprId,
                (screenExpr) => screenExpr.withPos(pos));
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
            const newScreenExpressions = state.screenExpressions
                .remove(argExprId).set(exprId, newScreenExpr);
            return state.withScreenExpressions(newScreenExpressions);
        },
        insertAsBody: ({bodyExprId, path: {exprId, pathSteps}}) => {
            const bodyScreenExpr = exprWithId(bodyExprId);
            const targetScreenExpr = exprWithId(exprId);
            const resultExpr = insertAsBody(
                targetScreenExpr.expr, bodyScreenExpr.expr, pathSteps);
            const newScreenExpr = targetScreenExpr.withExpr(resultExpr);
            const newScreenExpressions = state.screenExpressions
                .remove(bodyExprId).set(exprId, newScreenExpr);
            return state.withScreenExpressions(newScreenExpressions);
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
            return state.withActiveDrags(state.activeDrags.set(fingerId, exprId)
            );
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

const addExpression = (state: t.State, screenExpr: ScreenExpression): t.State => {
    const nextExprId = state.nextExprId;
    return t.newState(
        state.screenExpressions.set(nextExprId, screenExpr),
        nextExprId + 1,
        state.activeDrags,
    );
};

type Transform<T> = (t: T) => T;

const modifyExpression = (state: t.State, exprId: number,
                          transform: Transform<ScreenExpression>): t.State => {
    let {screenExpressions} = state;
    const screenExpr = screenExpressions.get(exprId);
    if (screenExpr) {
        screenExpressions = screenExpressions.set(exprId, transform(screenExpr));
    }
    return state.withScreenExpressions(screenExpressions);
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
    const childRef = getChildRef(expr, path.get(0));
    const newChild = transformAtPath(childRef.expr, path.slice(1), transform);
    return childRef.replaceWith(newChild);
};

type ChildRef = {
    expr: UserExpression,
    replaceWith: (newChild: UserExpression) => UserExpression
};

const getChildRef = (expr: UserExpression, step: PathComponent): ChildRef => {
    if (step === 'func' && expr.type === 'userFuncCall') {
        return {expr: expr.func, replaceWith: expr.withFunc.bind(expr)};
    } else if (step === 'arg' && expr.type === 'userFuncCall') {
        return {expr: expr.arg, replaceWith: expr.withArg.bind(expr)};
    } else if (step === 'body' && expr.type === 'userLambda' && expr.body) {
        return {expr: expr.body, replaceWith: expr.withBody.bind(expr)};
    }
    throw new Error('Unexpected step: ' + JSON.stringify(step));
};

export default playgroundApp;
