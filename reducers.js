/**
 * @flow
 */

import type {Action} from './actions'
import type {ScreenExpression, UserExpression, PathComponent} from './types'

export type State = {
    screenExpressions: Map<number, ScreenExpression>,
    nextExprId: number,
};

const initialState = {
    screenExpressions: new Map(),
    nextExprId: 0,
};

const playgroundApp = (state: State = initialState, action: Action): State => {
    if (action.type === 'RESET') {
        return initialState;
    } else if (action.type === 'ADD_EXPRESSION') {
        return addExpression(state, action.screenExpr);
    } else if (action.type === 'MOVE_EXPRESSION') {
        const pos = action.pos;
        return modifyExpression(state, action.exprId, (screenExpr) => {
            return {
                expr: screenExpr.expr,
                pos: pos,
            };
        });
    } else if (action.type === 'EXTRACT_BODY') {
        const {path: {exprId, pathSteps}, targetPos} = action;
        const existingScreenExpr = state.screenExpressions.get(exprId);
        if (!existingScreenExpr) {
            return state;
        }
        const extractResult = extractBody(existingScreenExpr.expr, pathSteps);
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
    return state;
};

const addExpression = (state: State, screenExpr: ScreenExpression): State => {
    const nextExprId = state.nextExprId;
    const newScreenExpressions = new Map(state.screenExpressions);
    newScreenExpressions.set(nextExprId, screenExpr);
    return {
        screenExpressions: newScreenExpressions,
        nextExprId: nextExprId + 1,
    };
};

type Transform<T> = (t: T) => T;

const modifyExpression = (state: State, exprId: number,
                          transform: Transform<ScreenExpression>): State => {
    const newScreenExpressions = new Map(state.screenExpressions);
    const screenExpr = newScreenExpressions.get(exprId);
    if (screenExpr) {
        newScreenExpressions.set(exprId, transform(screenExpr));
    }
    return {
        screenExpressions: newScreenExpressions,
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
        if (expr.type === 'lambda' && expr.body) {
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
        if (expr.type !== 'funcCall') {
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
        if (expr.type !== 'funcCall') {
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
        if (expr.type !== 'lambda' || expr.body == null) {
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