/**
 * Evaluator for raw expressions.
 *
 * For evaluation purposes, we transform the AST format to a new EvalExpression
 * format and use that.
 *
 * @flow
 */

import * as t from './types'
import type {Expression} from './types'

export const evaluate = (expr: Expression): Expression => {
    return expr;
};

export const canStep = (expr: Expression): boolean => {
    return t.matchExpression(expr, {
        lambda: ({body}) => canStep(body),
        funcCall: ({func, arg}) =>
        func.type === 'lambda' || canStep(func) || canStep(arg),
        variable: () => false,
    });
};
