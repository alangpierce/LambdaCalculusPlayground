/**
 * @flow
 */

import {expandUserExpr, tryResolveExpression} from './DefinitionManager'
import {evaluate, canStep} from './ExpressionEvaluator'
import * as t from './types'
import type {Expression, UserExpression} from './types'

export const evaluateUserExpr = (userExpr: UserExpression): ?UserExpression => {
    const expr = expandUserExpr(userExpr);
    if (!expr) {
        return null;
    }
    const evaluatedExpr = evaluate(expr);
    return collapseDefinitions(evaluatedExpr);
};

export const canStepUserExpr = (userExpr: UserExpression): boolean => {
    const expr = expandUserExpr(userExpr);
    if (!expr) {
        return false;
    }
    return canStep(expr);
};

/**
 * Given an expression, convert it to a UserExpression, attempting to account
 * for expressions already defined.
 */
const collapseDefinitions = (expr: Expression): UserExpression => {
    const defName = tryResolveExpression(expr);
    if (defName) {
        return t.newUserReference(defName);
    }
    return t.matchExpression(expr, {
        lambda: ({varName, body}) =>
            t.newUserLambda(varName, collapseDefinitions(body)),
        funcCall: ({func, arg}) =>
            t.newUserFuncCall(collapseDefinitions(func), collapseDefinitions(arg)),
        variable: ({varName}) => t.newUserVariable(varName),
    })
};
