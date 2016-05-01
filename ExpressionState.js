/**
 * @flow
 */

import * as Immutable from 'immutable';

import type {
    ScreenExpression,
    UserExpression,
    PathComponent,
    State
} from './types'
import * as t from './types'

export const addExpression = (state: State, screenExpr: ScreenExpression):
        State => {
    const nextExprId = state.nextExprId;
    return state
        .updateScreenExpressions((exprs) => exprs.set(nextExprId, screenExpr))
        .withNextExprId(nextExprId + 1);
};

type Transform<T> = (t: T) => T;

export const modifyExpression = (state: State, exprId: number,
                                 transform: Transform<ScreenExpression>):
        State => {
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
export const decomposeExpression = (
        expr: UserExpression, path: Immutable.List<PathComponent>):
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

export const insertAsArg = (
        targetExpr: UserExpression, newArgExpr: UserExpression,
        path: Immutable.List<PathComponent>): UserExpression => {
    return transformAtPath(targetExpr, path, (expr) =>
        t.newUserFuncCall(expr, newArgExpr)
    );
};

export const insertAsBody = (
        targetExpr: UserExpression, newBodyExpr: UserExpression,
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