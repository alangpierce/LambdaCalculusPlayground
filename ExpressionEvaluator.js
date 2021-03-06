/**
 * Evaluator for raw expressions.
 *
 * For evaluation purposes, we transform the AST format to a new EvalExpression
 * format and use that.
 *
 * @flow
 */

import * as t from './types'
import type {Expression, EvalExpression, Slot, VarMarker} from './types'
import {IMap, ISet} from './types-collections'

export const canStep = (expr: Expression): boolean => {
    return expr.match({
        lambda: ({body}) => canStep(body),
        funcCall: ({func, arg}) =>
            func.type === 'lambda' || canStep(func) || canStep(arg),
        variable: () => false,
    });
};

/**
 * Returns the result expression if possible, or null if the evaluation timed
 * out (or there was any other error). Unfortunately, distinguishing between
 * stack overflow exceptions and other errors is hard because different runtimes
 * provide different types of errors.
 */
export const evaluate = (expr: Expression): ?Expression => {
    try {
        const compiledExpr = compile(IMap.make(), expr);
        const evaluatedExpr = evaluateCompiledExpr(compiledExpr);
        return assignNames(evaluatedExpr, IMap.make());
    } catch (e) {
        return null;
    }
};

const evaluateCompiledExpr = (compiledExpr: EvalExpression): EvalExpression => {
    const startTime = new Date().getTime();
    const checkForCancellation = () => {
        if (new Date().getTime() > startTime + 1000) {
            throw new Error('Time limit exceeded.');
        }
    };

    const evaluateRec = (expr: EvalExpression, freeMarkers: ISet<VarMarker>,
            topLevel: boolean): EvalExpression => {
        checkForCancellation();
        return expr.match({
            evalLambda: (lambda) => {
                if (topLevel) {
                    freeMarkers = freeMarkers.add(lambda.varMarker);
                }
                return lambda.withBody(
                    evaluateRec(lambda.body, freeMarkers, topLevel));
            },
            evalFuncCall: ({func, arg}) => {
                // TODO: Maybe don't fully evaluate the func if it's a lambda?
                func = evaluateRec(func, freeMarkers, false);
                if (func instanceof t.EvalLambda) {
                    const {varMarker, originalVarName, body} = func;
                    const slot = {
                        isValue: false,
                        expr: arg,
                        originalVarName: originalVarName,
                    };
                    const boundExpression = bindVariable(varMarker, slot, body);
                    return evaluateRec(boundExpression, freeMarkers, topLevel);
                } else {
                    // We can't do anything more with the left side, so we might as
                    // well try evaluating the right side. However, we want to hold
                    // off on that if we know that the left side has any variables
                    // that might change through a later substitution.
                    if (containsOnlyFreeVars(func, freeMarkers)) {
                        arg = evaluateRec(arg, freeMarkers, topLevel);
                    }
                    return t.EvalFuncCall.make(func, arg);
                }

            },
            evalBoundVariable: ({slot}) => {
                if (!slot.isValue) {
                    slot.expr = evaluateRec(slot.expr, freeMarkers, topLevel);
                    slot.isValue = true;
                }
                return slot.expr;
            },
            evalUnboundVariable: (unboundVariable) => unboundVariable,
            evalFreeVariable: (freeVariable) => freeVariable,
        });
    };

    const bindVariable = (varMarker: VarMarker, slot: Slot, expr: EvalExpression):
            EvalExpression => {
        checkForCancellation();
        return expr.match({
            evalLambda: (lambda) => {
                // TODO: Is it possible to get ambiguous markers? What if a lambda
                // in the original code is able to have a copy of itself with both
                // an outer variable and normal unbound variable?
                if (lambda.varMarker === varMarker) {
                    return lambda;
                }
                return lambda.withBody(bindVariable(varMarker, slot, lambda.body));
            },
            evalFuncCall: ({func, arg}) => t.EvalFuncCall.make(
                bindVariable(varMarker, slot, func),
                bindVariable(varMarker, slot, arg)),
            evalBoundVariable: (boundVariable) => {
                if (containsUsage(varMarker, boundVariable.slot.expr)) {
                    return t.EvalFuncCall.make(
                        t.EvalLambda.make(
                            varMarker, slot.originalVarName, boundVariable),
                        slot.expr,
                    );
                } else {
                    return boundVariable;
                }
            },
            evalUnboundVariable: (unboundVariable) => {
                if (unboundVariable.varMarker != varMarker) {
                    return unboundVariable;
                }
                return t.EvalBoundVariable.make(slot);
            },
            evalFreeVariable: (freeVariable) => freeVariable,
        });
    };

    const containsUsage = (varMarker: VarMarker, expr: EvalExpression) => {
        checkForCancellation();
        return expr.match({
            evalLambda: ({body}) => containsUsage(varMarker, body),
            evalFuncCall: ({func, arg}) =>
            containsUsage(varMarker, func) || containsUsage(varMarker, arg),
            evalBoundVariable: ({slot}) => containsUsage(varMarker, slot.expr),
            evalUnboundVariable: (variable) => variable.varMarker === varMarker,
            evalFreeVariable: () => false,
        })
    };

    const containsOnlyFreeVars = (
            expr: EvalExpression, freeMarkers: ISet<VarMarker>):
            boolean => {
        checkForCancellation();
        return expr.match({
            evalLambda: () => true,
            evalFuncCall: ({func, arg}) =>
            containsOnlyFreeVars(func, freeMarkers) &&
            containsOnlyFreeVars(arg, freeMarkers),
            evalBoundVariable: ({slot}) =>
                containsOnlyFreeVars(slot.expr, freeMarkers),
            evalUnboundVariable: ({varMarker}) => freeMarkers.has(varMarker),
            evalFreeVariable: () => true,
        });
    };

    return evaluateRec(compiledExpr, ISet.make(), true);
};

type Context = IMap<string, VarMarker>;

const compile = (context: Context, expr: Expression): EvalExpression => {
    return expr.match({
        lambda: ({varName, body}) => {
            const marker = newMarker();
            const newContext = context.set(varName, marker);
            return t.EvalLambda.make(marker, varName, compile(newContext, body));
        },
        funcCall: ({func, arg}) =>
            t.EvalFuncCall.make(compile(context, func), compile(context, arg)),
        variable: ({varName}) => {
            const marker = context.get(varName);
            if (marker !== null && marker !== undefined) {
                return t.EvalUnboundVariable.make(marker, varName);
            } else {
                return t.EvalFreeVariable.make(varName);
            }
        },
    })
};

let lastMarker = 0;
const newMarker = (): VarMarker => {
    return lastMarker++;
};

const assignNames = (
        expr: EvalExpression, namesByMarker: IMap<VarMarker, string>):
        Expression => {
    return expr.match({
        evalLambda: ({varMarker, originalVarName, body}) => {
            let varName = originalVarName;
            // We shouldn't use a variable name that conflicts with a
            // user-specified name, so add primes until it doesn't conflict.
            // TODO: Prove that this doesn't create conflicts among unbound
            // variable names.
            while (containsFreeVarName(body, varName)) {
                varName = varName + "'";
            }
            const newNamesByMarker = namesByMarker.set(varMarker, varName);
            return t.Lambda.make(varName, assignNames(body, newNamesByMarker));
        },
        evalFuncCall: ({func, arg}) => t.FuncCall.make(
            assignNames(func, namesByMarker),
            assignNames(arg, namesByMarker)),
        evalBoundVariable: () => {
            throw new Error(
                "Bound variables shouldn't exist in value expressions.");
        },
        evalUnboundVariable: ({varMarker}) => {
            const varName = namesByMarker.get(varMarker);
            if (!varName) {
                throw new Error(
                    'All unbound variables must be assigned names.');
            }
            return t.Variable.make(varName);
        },
        evalFreeVariable: ({varName}) => t.Variable.make(varName),
    });
};

/**
 * Determine if the given name is used as a free variable in the given
 * expression. "Free" means that the variable specified directly by the user, so
 * we're not allowed to change the name.
 *
 * If we care about performance, this would be pretty easy to optimize.
 */
const containsFreeVarName = (expr: EvalExpression, name: string): boolean => {
    return expr.match({
        evalLambda: ({body}) => containsFreeVarName(body, name),
        evalFuncCall: ({func, arg}) =>
            containsFreeVarName(func, name) || containsFreeVarName(arg, name),
        evalBoundVariable: () => false,
        evalUnboundVariable: () => false,
        evalFreeVariable: ({varName}) => varName === name,
    })
};