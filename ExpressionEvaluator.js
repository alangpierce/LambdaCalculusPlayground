/**
 * Evaluator for raw expressions.
 *
 * For evaluation purposes, we transform the AST format to a new EvalExpression
 * format and use that.
 *
 * @flow
 */

import * as Immutable from 'immutable'

import * as t from './types'
import type {Expression, EvalExpression, Slot, VarMarker} from './types'

export const canStep = (expr: Expression): boolean => {
    return t.matchExpression(expr, {
        lambda: ({body}) => canStep(body),
        funcCall: ({func, arg}) =>
        func.type === 'lambda' || canStep(func) || canStep(arg),
        variable: () => false,
    });
};

// TODO: Run in a web worker or something to avoid infinite loops.
export const evaluate = (expr: Expression): Expression => {
    const compiledExpr = compile(new Immutable.Map(), expr);
    const evaluatedExpr = evaluateRec(compiledExpr, new Immutable.Set(), true);
    return assignNames(evaluatedExpr, new Immutable.Map());
};

const evaluateRec = (
        expr: EvalExpression, freeMarkers: Immutable.Set<VarMarker>,
        topLevel: boolean): EvalExpression => {
    return t.matchEvalExpression(expr, {
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
            if (func.type === 'evalLambda') {
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
                return t.newEvalFuncCall(func, arg);
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
    return t.matchEvalExpression(expr, {
        evalLambda: (lambda) => {
            // TODO: Is it possible to get ambiguous markers? What if a lambda
            // in the original code is able to have a copy of itself with both
            // an outer variable and normal unbound variable?
            if (lambda.varMarker === varMarker) {
                return lambda;
            }
            return lambda.withBody(bindVariable(varMarker, slot, lambda.body));
        },
        evalFuncCall: ({func, arg}) => t.newEvalFuncCall(
            bindVariable(varMarker, slot, func),
            bindVariable(varMarker, slot, arg)),
        evalBoundVariable: (boundVariable) => {
            if (containsUsage(varMarker, boundVariable.slot.expr)) {
                return t.newEvalFuncCall(
                    t.newEvalLambda(
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
            return t.newEvalBoundVariable(slot);
        },
        evalFreeVariable: (freeVariable) => freeVariable,
    });
};

const containsUsage = (varMarker: VarMarker, expr: EvalExpression) => {
    return t.matchEvalExpression(expr, {
        evalLambda: ({body}) => containsUsage(varMarker, body),
        evalFuncCall: ({func, arg}) =>
            containsUsage(varMarker, func) || containsUsage(varMarker, arg),
        evalBoundVariable: ({slot}) => containsUsage(varMarker, slot.expr),
        evalUnboundVariable: (variable) => variable.varMarker === varMarker,
        evalFreeVariable: () => false,
    })
};

const containsOnlyFreeVars = (
        expr: EvalExpression, freeMarkers: Immutable.Set<VarMarker>):
        boolean => {
    return t.matchEvalExpression(expr, {
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


type Context = Immutable.Map<string, VarMarker>;

const compile = (context: Context, expr: Expression): EvalExpression => {
    return t.matchExpression(expr, {
        lambda: ({varName, body}) => {
            const marker = newMarker();
            const newContext = context.set(varName, marker);
            return t.newEvalLambda(marker, varName, compile(newContext, body));
        },
        funcCall: ({func, arg}) =>
            t.newEvalFuncCall(compile(context, func), compile(context, arg)),
        variable: ({varName}) => {
            const marker = context.get(varName);
            if (marker !== null && marker !== undefined) {
                return t.newEvalUnboundVariable(marker, varName);
            } else {
                return t.newEvalFreeVariable(varName);
            }
        },
    })
};

let lastMarker = 0;
const newMarker = (): VarMarker => {
    return lastMarker++;
};

const assignNames = (
        expr: EvalExpression, namesByMarker: Immutable.Map<VarMarker, string>):
        Expression => {
    return t.matchEvalExpression(expr, {
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
            return t.newLambda(varName, assignNames(body, newNamesByMarker));
        },
        evalFuncCall: ({func, arg}) => t.newFuncCall(
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
            return t.newVariable(varName);
        },
        evalFreeVariable: ({varName}) => t.newVariable(varName),
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
    return t.matchEvalExpression(expr, {
        evalLambda: ({body}) => containsFreeVarName(body, name),
        evalFuncCall: ({func, arg}) =>
            containsFreeVarName(func, name) || containsFreeVarName(arg, name),
        evalBoundVariable: () => false,
        evalUnboundVariable: () => false,
        evalFreeVariable: ({varName}) => varName === name,
    })
};