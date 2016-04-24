/**
 * @flow
 */

import * as Immutable from 'immutable'

import evaluateExpression from './evaluateExpression'
import type {
    Action,
    ScreenExpression,
    UserExpression,
    PathComponent,
    State
} from './types'
import * as t from './types'

const initialState = t.newState(new Immutable.Map(), 0);

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
        evaluateExpression: ({exprId, targetPos}) => {
            const existingScreenExpr = state.screenExpressions.get(exprId);
            if (!existingScreenExpr) {
                return state;
            }
            // TODO: Do nothing if the expression can't be stepped.
            const evaluatedExpr = evaluateExpression(existingScreenExpr.expr);
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
const decomposeExpression =
        (expr: UserExpression, path: Array<PathComponent>): ?DecomposeResult => {
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

    if (path[0] === 'func') {
        if (expr.type !== 'userFuncCall') {
            return null;
        }
        const subResult = decomposeExpression(expr.func, path.slice(1));
        if (!subResult) {
            return null;
        }
        return {
            original: expr.withFunc(subResult.original),
            extracted: subResult.extracted,
        };
    } else if (path[0] === 'arg') {
        if (expr.type !== 'userFuncCall') {
            return null;
        }
        const subResult = decomposeExpression(expr.arg, path.slice(1));
        if (!subResult) {
            return null;
        }
        return {
            original: expr.withArg(subResult.original),
            extracted: subResult.extracted,
        };
    } else if (path[0] === 'body') {
        if (expr.type !== 'userLambda' || !expr.body) {
            return null;
        }
        const subResult = decomposeExpression(expr.body, path.slice(1));
        if (!subResult) {
            return null;
        }
        return {
            original: expr.withBody(subResult.original),
            extracted: subResult.extracted,
        };
    }
};

export default playgroundApp;