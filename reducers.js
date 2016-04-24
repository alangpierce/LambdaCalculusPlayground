/**
 * @flow
 */

import * as Immutable from 'immutable'

import type {ScreenExpression, UserExpression, PathComponent} from './types'
import * as t from './types'

export type State = {
    screenExpressions: Immutable.Map<number, ScreenExpression>,
    nextExprId: number,
};

const initialState = {
    screenExpressions: new Immutable.Map(),
    nextExprId: 0,
};

const playgroundApp = (state: State = initialState, action: t.Action): State => {
    // Despite our action union, there are some internal redux actions that
    // start with @@, which we want to just ignore.
    if (action.type.startsWith('@@')) {
        return state;
    }

    return t.matchAction(action, {
        reset: () => initialState,
        addExpression: ({screenExpr}) => addExpression(state, screenExpr),
        moveExpression: ({exprId, pos}) => {
            return modifyExpression(state, exprId, (screenExpr) => {
                return {
                    expr: screenExpr.expr,
                    pos: pos,
                };
            });
        },
        extractBody: ({path: {exprId, pathSteps}, targetPos}) => {
            const existingScreenExpr = state.screenExpressions.get(exprId);
            if (!existingScreenExpr) {
                return state;
            }
            const extractResult = extractBody(
                existingScreenExpr.expr, pathSteps);
            if (!extractResult) {
                return state;
            }
            const {original, extracted} = extractResult;
            state = addExpression(state, {
                expr: extracted,
                pos: targetPos,
            });
            state = modifyExpression(state, exprId, () => {
                return {
                    expr: original,
                    pos: existingScreenExpr.pos,
                };
            });
            return state;
        }
    });
};

const addExpression = (state: State, screenExpr: ScreenExpression): State => {
    const nextExprId = state.nextExprId;
    return {
        screenExpressions: state.screenExpressions.set(nextExprId, screenExpr),
        nextExprId: nextExprId + 1,
    };
};

type Transform<T> = (t: T) => T;

const modifyExpression = (state: State, exprId: number,
                          transform: Transform<ScreenExpression>): State => {
    let {screenExpressions} = state;
    const screenExpr = screenExpressions.get(exprId);
    if (screenExpr) {
        screenExpressions = screenExpressions.set(exprId, transform(screenExpr));
    }
    return {
        screenExpressions: screenExpressions,
        nextExprId: state.nextExprId,
    };
};

type ExtractResult = {
    original: UserExpression,
    extracted: UserExpression,
}

/**
 * Assuming that the given expression
 *
 * TODO: Also extract func args.
 */
const extractBody = (expr: UserExpression, path: Array<PathComponent>): ?ExtractResult => {
    if (path.length === 0) {
        if (expr.type === 'userLambda' && expr.body) {
            return {
                original: {
                    ...expr,
                    body: null,
                },
                extracted: expr.body,
            }
        }
    }

    if (path[0] === 'func') {
        if (expr.type !== 'userFuncCall') {
            return null;
        }
        const subResult = extractBody(expr.func, path.slice(1));
        if (!subResult) {
            return null;
        }
        return {
            original: {
                ...expr,
                func: subResult.original,
            },
            extracted: subResult.extracted,
        };
    } else if (path[0] === 'arg') {
        if (expr.type !== 'userFuncCall') {
            return null;
        }
        const subResult = extractBody(expr.arg, path.slice(1));
        if (!subResult) {
            return null;
        }
        return {
            original: {
                ...expr,
                arg: subResult.original,
            },
            extracted: subResult.extracted,
        };
    } else if (path[0] === 'body') {
        if (expr.type !== 'userLambda' || !expr.body) {
            return null;
        }
        const subResult = extractBody(expr.body, path.slice(1));
        if (!subResult) {
            return null;
        }
        return {
            original: {
                ...expr,
                body: subResult.original,
            },
            extracted: subResult.extracted,
        };
    }
};

export default playgroundApp;