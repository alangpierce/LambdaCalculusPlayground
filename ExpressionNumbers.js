/**
 * @flow
 */

import type {
    Expression,
    UserExpression,
} from './types';
import {
    FuncCall,
    Lambda,
    UserFuncCall,
    UserLambda,
    UserVariable,
    Variable,
} from './types';

export const tryExpressionForNumber = (defName: string): ?Expression => {
    if (!isNumber(defName)) {
        return null;
    }
    const num = Number(defName);
    let body = Variable.make('z');
    for (let i = 0; i < num; i++) {
        body = FuncCall.make(Variable.make('s'), body);
    }
    return Lambda.make('s', Lambda.make('z', body));
};

export const tryUserExpressionForNumber = (defName: string): ?UserExpression => {
    if (!isNumber(defName)) {
        return null;
    }
    const num = Number(defName);
    let body = UserVariable.make('z');
    for (let i = 0; i < num; i++) {
        body = UserFuncCall.make(UserVariable.make('s'), body);
    }
    return UserLambda.make('s', UserLambda.make('z', body));
};

export const isNumber = (defName: string): boolean => {
    const num = Number(defName);
    // The name must be a natural number in canonical form, so -5 and 03 are
    // both disallowed.
    return num >= 0 && ('' + num == defName);
};

/**
 * Return true if the given number is too big.
 */
export const isGiantNumber = (defName: string): boolean => {
    return isNumber(defName) && Number(defName) > 30;
};

export const tryResolveToNumber = (expr: Expression): ?string => {
    if (!(expr instanceof Lambda) || expr.varName !== 's') {
        return null;
    }
    expr = expr.body;
    if (!(expr instanceof Lambda) || expr.varName !== 'z') {
        return null;
    }
    expr = expr.body;
    let result = 0;
    while (expr instanceof FuncCall) {
        const {func, arg} = expr;
        if (!(func instanceof Variable) || func.varName !== 's') {
            return null;
        }
        expr = arg;
        result++;
    }
    if (!(expr instanceof Variable) || expr.varName !== 'z') {
        return null;
    }
    return '' + result;
};