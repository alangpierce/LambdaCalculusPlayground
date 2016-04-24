/**
 * @flow
 */

import * as Immutable from 'immutable'

import type {ScreenExpression, UserExpression, PathComponent} from './types'
import * as t from './types'

const initialState = t.newState(new Immutable.Map(), 0);

const playgroundApp = (state: t.State = initialState, action: t.Action): t.State => {
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
            state = addExpression(
                state, t.newScreenExpression(extracted, targetPos));
            state = modifyExpression(state, exprId,
                () => existingScreenExpr.withExpr(original));
            return state;
        }
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
            const body = expr.body;
            return {
                original: expr.withBody(null),
                extracted: body,
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
            original: expr.withFunc(subResult.original),
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
            original: expr.withArg(subResult.original),
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
            original: expr.withBody(subResult.original),
            extracted: subResult.extracted,
        };
    }
};

export default playgroundApp;