/**
 * Simple definition manager.
 *
 * @flow
 */

import * as Immutable from 'immutable'

import type {Expression, UserExpression} from './types'
import * as t from './types'

let definitions: Immutable.Map<string, Expression> = new Immutable.Map();
// TODO: Handle multiple definitions mapping to the same thing.
let namesByDef: Immutable.Map<Expression, string> = new Immutable.Map();

/**
 * Create a definition. Currently, the definition must only use existing valid
 * definitions.
 *
 * TODO: Allow invalid definitions.
 */
export const define = (defName: string, userExpression: UserExpression) => {
    const expr = expandUserExpr(userExpression);
    if (!expr) {
        throw new Error('Can only define valid expressions for now.');
    }
    definitions = definitions.set(defName, expr);
    namesByDef = namesByDef.set(expr, defName);
};

/**
 * Given an expression, try to find its matching definition, if any. Return null
 * if no definition matched. Currently all variable names must match exactly for
 * a definition to match.
 */
export const tryResolveExpression = (expr: Expression): ?string => {
    return namesByDef.get(expr);
};

/**
 * Return the equivalent expression for the given user expression, or null if
 * the user expression is invalid in any way.
 */
export const expandUserExpr = (userExpr: UserExpression): ?Expression => {
    return t.matchUserExpression(userExpr, {
        userLambda: ({varName, body}) => {
            if (!body) {
                return null;
            }
            const bodyExpr = expandUserExpr(body);
            if (!bodyExpr) {
                return null;
            }
            return t.newLambda(varName, bodyExpr);
        },
        userFuncCall: ({func, arg}) => {
            const funcExpr = expandUserExpr(func);
            if (!funcExpr) {
                return null;
            }
            const argExpr = expandUserExpr(arg);
            if (!argExpr) {
                return null;
            }
            return t.newFuncCall(funcExpr, argExpr);
        },
        userVariable: ({varName}) => t.newVariable(varName),
        // This returns null if the definition isn't valid.
        userReference: ({defName}) => definitions.get(defName)
    })
};