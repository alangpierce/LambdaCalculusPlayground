/**
 * @flow
 */

import * as t from './types'
import type {Expression, UserExpression} from './types'

export const evaluateUserExpr = (userExpr: UserExpression): ?UserExpression => {
    const expr = userExprToExpr(userExpr);
    if (!expr) {
        return null;
    }
    const evaluatedExpr = evaluate(expr);
    return exprToUserExpr(evaluatedExpr);
};

export const canStepUserExpr = (userExpr: UserExpression): boolean => {
    const expr = userExprToExpr(userExpr);
    if (!expr) {
        return false;
    }
    return canStep(expr);
};

/**
 * Return the equivalent expression for the given user expression, or null if
 * the user expression is invalid in any way.
 */
const userExprToExpr = (userExpr: UserExpression): ?Expression => {
    return t.matchUserExpression(userExpr, {
        userLambda: ({varName, body}) => {
            if (!body) {
                return null;
            }
            const bodyExpr = userExprToExpr(body);
            if (!bodyExpr) {
                return null;
            }
            return t.newLambda(varName, bodyExpr);
        },
        userFuncCall: ({func, arg}) => {
            const funcExpr = userExprToExpr(func);
            if (!funcExpr) {
                return null;
            }
            const argExpr = userExprToExpr(arg);
            if (!argExpr) {
                return null;
            }
            return t.newFuncCall(funcExpr, argExpr);
        },
        userVariable: ({varName}) => t.newVariable(varName),
        userReference: () => {
            throw new Error('Evaluation of references not supported yet.');
        }
    })
};

const exprToUserExpr = (expr: Expression): UserExpression => {
    return t.matchExpression(expr, {
        lambda: ({varName, body}) =>
            t.newUserLambda(varName, exprToUserExpr(body)),
        funcCall: ({func, arg}) =>
            t.newUserFuncCall(exprToUserExpr(func), exprToUserExpr(arg)),
        variable: ({varName}) => t.newUserVariable(varName),
    })
};

const canStep = (expr: Expression): boolean => {
    return t.matchExpression(expr, {
        lambda: ({body}) => canStep(body),
        funcCall: ({func, arg}) =>
            func.type === 'lambda' || canStep(func) || canStep(arg),
        variable: () => false,
    });
};

const evaluate = (expr: Expression): Expression => {
    // TODO.
    return expr;
};
