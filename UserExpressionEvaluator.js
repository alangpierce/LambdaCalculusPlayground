/**
 * @flow
 */

import {evaluate, canStep} from './ExpressionEvaluator';
import {tryExpressionForNumber, tryResolveToNumber} from './ExpressionNumbers';
import * as t from './types';
import type {Expression, UserExpression} from './types';
import {IMap} from './types-collections';

export type UserDefinitions = IMap<string, ?UserExpression>;
export type Definitions = IMap<string, ?Expression>;

// TODO: Do something like dependency injection with the definitions.
/**
 * Evaluate the given expression. Return null if there was any problem. In
 * practice, this will only be called with expressions that can be evaluated, so
 * returning null indicates that the evaluation ran out of time or hit a stack
 * overflow.
 */
export const evaluateUserExpr = (
        definitions: Definitions, isAutomaticNumbersEnabled: boolean,
        userExpr: UserExpression): ?UserExpression => {
    const expr = expandUserExpr(
        defLookup(definitions, isAutomaticNumbersEnabled), userExpr);
    if (!expr) {
        return null;
    }
    const evaluatedExpr = evaluate(expr);
    if (evaluatedExpr == null) {
        return null;
    }
    return collapseDefinitions(
        definitions, isAutomaticNumbersEnabled, evaluatedExpr);
};

export const canStepUserExpr = (
        definitions: Definitions, isAutomaticNumbersEnabled: boolean,
        userExpr: UserExpression): boolean => {
    const expr = expandUserExpr(
        defLookup(definitions, isAutomaticNumbersEnabled), userExpr);
    if (!expr) {
        return false;
    }
    return canStep(expr);
};

export const defLookup = (
    definitions: Definitions, isAutomaticNumbersEnabled: boolean):
        (defName: string) => ?Expression => {
    return (defName) => {
        if (definitions.hasKey(defName)) {
            return definitions.get(defName);
        }
        if (isAutomaticNumbersEnabled) {
            return tryExpressionForNumber(defName);
        }
        return null;
    }
};

/**
 * Given an expression, convert it to a UserExpression, attempting to account
 * for expressions already defined.
 */
const collapseDefinitions = (
        definitions: Definitions, isAutomaticNumbersEnabled: boolean,
        expr: Expression): UserExpression => {
    let reverseDefinitions: IMap<Expression, string> = IMap.make();
    for (let [defName, expr] of definitions) {
        // Currently we require an exact match, including all variable names,
        // and we pick an arbitrary definition name if we match multiple.
        if (expr != null) {
            reverseDefinitions = reverseDefinitions.set(expr, defName);
        }
    }

    const rec = (expr: Expression): UserExpression => {
        let defName = reverseDefinitions.get(expr);
        if (defName == null && isAutomaticNumbersEnabled) {
            defName = tryResolveToNumber(expr);
        }
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

export const expandAllDefinitions = (
        userDefs: UserDefinitions, isAutomaticNumbersEnabled: boolean): Definitions => {
    const resultDefinitions: Map<string, ?Expression> = new Map();
    const memoizedCompute = (defName: string): ?Expression => {
        if (isAutomaticNumbersEnabled && !userDefs.hasKey(defName)) {
            return tryExpressionForNumber(defName);
        }

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
        lookupDef: (defName: string) => ?Expression,
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