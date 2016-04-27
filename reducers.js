/**
 * @flow
 */

import * as Immutable from 'immutable'

import {evaluateUserExpr, canStepUserExpr} from './UserExpressionEvaluator'
import type {
    Action,
    ScreenExpression,
    UserExpression,
    PathComponent,
    State
} from './types'
import * as t from './types'

const initialState: State = t.newState(new Immutable.Map(), 0);

const playgroundApp = (state: State = initialState, action: Action): State => {
    // Despite our action union, there are some internal redux actions that
    // start with @@, which we want to just ignore.
    if (action.type.startsWith('@@')) {
        return state;
    }

    return t.matchAction(action, {
        reset: () => initialState,
        addExpression: ({screenExpr}) => addExpression(state, screenExpr),
        moveExpression: ({exprId, pos}) => {
            return modifyExpression(state, exprId,
                (screenExpr) => screenExpr.withPos(pos));
        },
        decomposeExpression: ({path: {exprId, pathSteps}, targetPos}) => {
            const existingScreenExpr = state.screenExpressions.get(exprId);
            if (!existingScreenExpr) {
                return state;
            }
            const extractResult = decomposeExpression(
                existingScreenExpr.expr, pathSteps);
            if (!extractResult) {
                return state;
            }
            const {original, extracted} = extractResult;
            state = addExpression(
                state, t.newScreenExpression(extracted, targetPos));
            state = modifyExpression(state, exprId,
                () => existingScreenExpr.withExpr(original));
            return state;
        },
        insertAsArg: ({argExprId, path: {exprId, pathSteps}}) => {
            const argScreenExpr = state.screenExpressions.get(argExprId);
            const targetScreenExpr = state.screenExpressions.get(exprId);
            if (!argScreenExpr || !targetScreenExpr) {
                return state;
            }
            const resultExpr = insertAsArg(
                targetScreenExpr.expr, argScreenExpr.expr, pathSteps);
            if (!resultExpr) {
                return state;
            }
            const newScreenExpr = targetScreenExpr.withExpr(resultExpr);
            const newScreenExpressions = state.screenExpressions
                .remove(argExprId).set(exprId, newScreenExpr);
            return state.withScreenExpressions(newScreenExpressions);
        },
        insertAsBody: ({bodyExprId, path: {exprId, pathSteps}}) => {
            const bodyScreenExpr = state.screenExpressions.get(bodyExprId);
            const targetScreenExpr = state.screenExpressions.get(exprId);
            if (!bodyScreenExpr || !targetScreenExpr) {
                return state;
            }
            const resultExpr = insertAsBody(
                targetScreenExpr.expr, bodyScreenExpr.expr, pathSteps);
            if (!resultExpr) {
                return state;
            }
            const newScreenExpr = targetScreenExpr.withExpr(resultExpr);
            const newScreenExpressions = state.screenExpressions
                .remove(bodyExprId).set(exprId, newScreenExpr);
            return state.withScreenExpressions(newScreenExpressions);
        },
        evaluateExpression: ({exprId, targetPos}) => {
            const existingScreenExpr = state.screenExpressions.get(exprId);
            if (!existingScreenExpr) {
                return state;
            }
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
    });
};

const addExpression = (state: t.State, screenExpr: ScreenExpression): t.State => {
    const nextExprId = state.nextExprId;
    return t.newState(
        state.screenExpressions.set(nextExprId, screenExpr), nextExprId + 1
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
const decomposeExpression = (expr: UserExpression, path: Array<PathComponent>):
        ?DecomposeResult => {
    if (path.length === 0) {
        return t.matchUserExpression(expr, {
            userLambda: (lambda) => {
                if (lambda.body != null) {
                    const body = lambda.body;
                    return {
                        original: lambda.withBody(null),
                        extracted: body,
                    }
                } else {
                    return null;
                }
            },
            userFuncCall: ({func, arg}) => ({
                original: func,
                extracted: arg,
            }),
            userVariable: () => null,
            userReference: () => null,
        });
    }

    const childRef = getChildRef(expr, path[0]);
    if (!childRef) {
        return null;
    }
    const subResult = decomposeExpression(childRef.expr, path.slice(1));
    if (!subResult) {
        return null;
    }
    return {
        original: childRef.replaceWith(subResult.original),
        extracted: subResult.extracted,
    }
};

const insertAsArg = (targetExpr: UserExpression, newArgExpr: UserExpression,
                     path: Array<PathComponent>): ?UserExpression => {
    if (path.length === 0) {
        return t.newUserFuncCall(targetExpr, newArgExpr);
    }
    const childRef = getChildRef(targetExpr, path[0]);
    if (!childRef) {
        return null;
    }
    const newChild = insertAsArg(childRef.expr, newArgExpr, path.slice(1));
    if (!newChild) {
        return null;
    }
    return childRef.replaceWith(newChild);
};

const insertAsBody = (targetExpr: UserExpression, newBodyExpr: UserExpression,
                        path: Array<PathComponent>): ?UserExpression => {
    if (path.length === 0) {
        if (targetExpr.type === 'userLambda' && targetExpr.body === null) {
            return targetExpr.withBody(newBodyExpr);
        } else {
            return null;
        }
    }
    const childRef = getChildRef(targetExpr, path[0]);
    if (!childRef) {
        return null;
    }
    const newChild = insertAsBody(childRef.expr, newBodyExpr, path.slice(1));
    if (!newChild) {
        return null;
    }
    return childRef.replaceWith(newChild);
};

type ChildRef = {
    expr: UserExpression,
    replaceWith: (newChild: UserExpression) => UserExpression
};

const getChildRef = (expr: UserExpression, step: PathComponent): ?ChildRef => {
    if (step === 'func' && expr.type === 'userFuncCall') {
        return {expr: expr.func, replaceWith: expr.withFunc.bind(expr)};
    } else if (step === 'arg' && expr.type === 'userFuncCall') {
        return {expr: expr.arg, replaceWith: expr.withArg.bind(expr)};
    } else if (step === 'body' && expr.type === 'userLambda' && expr.body) {
        return {expr: expr.body, replaceWith: expr.withBody.bind(expr)};
    }
    return null;
};

export default playgroundApp;
