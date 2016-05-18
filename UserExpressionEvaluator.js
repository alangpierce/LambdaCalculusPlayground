/**
 * @flow
 */

import {evaluate, canStep} from './ExpressionEvaluator';
import * as t from './types';
import type {Expression, UserExpression} from './types';
import {IMap} from './types-collections';

export type UserDefinitions = IMap<string, ?UserExpression>;
export type Definitions = IMap<string, ?Expression>;

// TODO: Do something like dependency injection with the definitions.
export const evaluateUserExpr = (
        definitions: Definitions, userExpr: UserExpression): ?UserExpression => {
    const expr = expandUserExpr((defName) => definitions.get(defName), userExpr);
    if (!expr) {
        return null;
    }
    const evaluatedExpr = evaluate(expr);
    return collapseDefinitions(definitions, evaluatedExpr);
};

export const canStepUserExpr = (
        definitions: Definitions, userExpr: UserExpression): boolean => {
    const expr = expandUserExpr((defName) => definitions.get(defName), userExpr);
    if (!expr) {
        return false;
    }
    return canStep(expr);
};

/**
 * Given an expression, convert it to a UserExpression, attempting to account
 * for expressions already defined.
 */
const collapseDefinitions = (
        definitions: Definitions, expr: Expression): UserExpression => {
    let reverseDefinitions: IMap<Expression, string> = IMap.make();
    for (let [defName, expr] of definitions) {
        // Currently we require an exact match, including all variable names,
        // and we pick an arbitrary definition name if we match multiple.
        if (expr != null) {
            reverseDefinitions = reverseDefinitions.set(expr, defName);
        }
    }

    const rec = (expr: Expression): UserExpression => {
        const defName = reverseDefinitions.get(expr);
        if (defName) {
            return t.UserReference.make(defName);
        }
        return expr.match({
            lambda: ({varName, body}) => t.UserLambda.make(varName, rec(body)),
            funcCall: ({func, arg}) => t.UserFuncCall.make(rec(func), rec(arg)),
            variable: ({varName}) => t.UserVariable.make(varName),
        })
    };
    return rec(expr);
};

export const expandAllDefinitions = (userDefs: UserDefinitions): Definitions => {
    const resultDefinitions: Map<string, ?Expression> = new Map();
    const memoizedCompute = (defName: string): ?Expression => {
        if (resultDefinitions.has(defName)) {
            return resultDefinitions.get(defName);
        }
        resultDefinitions.set(defName, null);
        const result = expandUserExpr(memoizedCompute, userDefs.get(defName));
        resultDefinitions.set(defName, result);
        return result;
    };
    for (let [defName, _] of userDefs) {
        memoizedCompute(defName);
    }
    return IMap.make(resultDefinitions);
};

/**
 * Return the equivalent expression for the given user expression, or null if
 * the user expression is invalid in any way.
 *
 * We take the definition lookup function as a parameter, since sometimes we
 * need to do cycle detection.
 */
const expandUserExpr = (
        lookupDef: (name: string) => ?Expression,
        userExpr: ?UserExpression): ?Expression => {
    return userExpr && userExpr.match({
        userLambda: ({varName, body}) => {
            const bodyExpr = expandUserExpr(lookupDef, body);
            return bodyExpr && t.Lambda.make(varName, bodyExpr);
        },
        userFuncCall: ({func, arg}) => {
            const funcExpr = expandUserExpr(lookupDef, func);
            const argExpr = expandUserExpr(lookupDef, arg);
            return funcExpr && argExpr && t.FuncCall.make(funcExpr, argExpr);
        },
        userVariable: ({varName}) => t.Variable.make(varName),
        userReference: ({defName}) => lookupDef(defName),
    });
};