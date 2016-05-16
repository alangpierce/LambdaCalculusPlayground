/**
 * @flow
 */

import type {
    CanvasExpression,
    ExprContainer,
    UserExpression,
    PathComponent,
    State
} from './types'
import * as t from './types'
import {IList} from './types-collections'
import type {Updater} from './types-lib'

export const addExpression = (state: State, canvasExpr: CanvasExpression):
        State => {
    const nextExprId = state.nextExprId;
    return state
        .updateCanvasExpressions((exprs) => exprs.set(nextExprId, canvasExpr))
        .withNextExprId(nextExprId + 1);
};

export const updateExprContainer = (
        state: State, container: ExprContainer,
        updater: Updater<UserExpression>): State => {
    return container.match({
        exprIdContainer: ({exprId}) =>
            state.lens()
                .canvasExpressions().atKey(exprId).expr().update(updater),
        definitionContainer: ({defName}) =>
            state.lens().definitions().atKey(defName).update((def) => {
                if (def == null) {
                    throw new Error('Cannot update an empty definition.');
                }
                return updater(def);
            }),
    });
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
        expr: UserExpression, path: IList<PathComponent>): DecomposeResult => {
    let extracted = null;
    const original = transformAtPath(expr, path, (expr) => {
        if (expr instanceof t.UserLambda && expr.body) {
            extracted = expr.body;
            return expr.withBody(null);
        } else if (expr instanceof t.UserFuncCall) {
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
        path: IList<PathComponent>): UserExpression => {
    return transformAtPath(targetExpr, path, (expr) =>
        t.UserFuncCall.make(expr, newArgExpr)
    );
};

export const insertAsBody = (
        targetExpr: UserExpression, newBodyExpr: UserExpression,
        path: IList<PathComponent>): UserExpression => {
    return transformAtPath(targetExpr, path, (expr) => {
        if (!(expr instanceof t.UserLambda) || expr.body) {
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
        expr: UserExpression, path: IList<PathComponent>,
        updater: Updater<UserExpression>): UserExpression => {
    if (path.size === 0) {
        return updater(expr);
    }
    return updateChild(expr, path.get(0), (child) => {
        return transformAtPath(child, path.slice(1), updater);
    });
};

const updateChild = (expr: UserExpression, step: PathComponent,
                     updater: Updater<UserExpression>): UserExpression => {
    if (step === 'func' && expr instanceof t.UserFuncCall) {
        return expr.updateFunc(updater);
    } else if (step === 'arg' && expr instanceof t.UserFuncCall) {
        return expr.updateArg(updater);
    } else if (step === 'body' && expr instanceof t.UserLambda && expr.body) {
        return expr.withBody(updater(expr.body));
    }
    throw new Error('Unexpected step: ' + JSON.stringify(step));
};