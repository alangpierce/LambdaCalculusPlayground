package com.alangpierce.lambdacalculusplayground.evaluator;

import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.expression.FuncCall;
import com.alangpierce.lambdacalculusplayground.expression.Lambda;
import com.alangpierce.lambdacalculusplayground.expression.Variable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OptimizedExpressionEvaluator implements ExpressionEvaluator {
    @Override
    public Expression evaluate(Expression expression) {
        EvalExpression evalExpr = compile(new HashMap<>(), expression);
        EvalExpression result = evaluateRec(evalExpr, new HashSet<>(), true);
        return toExpression(result);
    }

    private EvalExpression evaluateRec(EvalExpression evalExpression, Set<Object> freeMarkers, boolean topLevel) {
        return evalExpression.visit(
                lambda -> {
                    if (topLevel) {
                        freeMarkers.add(lambda.varMarker());
                        EvalExpression newBody = evaluateRec(lambda.body(), freeMarkers, true);
                        freeMarkers.remove(lambda.varMarker());
                        return EvalLambda.create(
                                lambda.varMarker(), lambda.originalVarName(), newBody);
                    } else {
                        EvalExpression newBody = evaluateRec(lambda.body(), freeMarkers, false);
                        return EvalLambda.create(
                                lambda.varMarker(), lambda.originalVarName(), newBody);
                    }
                },
                funcCall -> {
                    // TODO: Maybe we don't want to fully evaluate the func if it's a lambda?
                    EvalExpression func = evaluateRec(funcCall.func(), freeMarkers, false);
                    if (func instanceof EvalLambda) {
                        EvalLambda lambda = (EvalLambda) func;
                        Slot slot = Slot.create(funcCall.arg(), lambda.originalVarName());
                        EvalExpression boundExpression = bindVariable(
                                lambda.varMarker(), slot, lambda.body());
                        return evaluateRec(boundExpression, freeMarkers, topLevel);
                    } else {
                        // We can't do anything more with the left side, so we might as well try
                        // evaluating the right side.
                        // TODO: What if the left side is an unbound variable that will later be
                        // bound? It could also be an expression consisting of unbound variables.
                        EvalExpression arg = funcCall.arg();
                        if (containsOnlyFreeVars(func, freeMarkers)) {
                            arg = evaluateRec(arg, freeMarkers, topLevel);
                        }
                        return EvalFuncCall.create(func, arg);
                    }
                },
                boundVariable -> {
                    Slot slot = boundVariable.slot();
                    if (!slot.isValue) {
                        slot.expr = evaluateRec(slot.expr, freeMarkers, topLevel);
                        slot.isValue = true;
                    }
                    return slot.expr;
                },
                unboundVariable -> unboundVariable,
                freeVariable -> freeVariable
        );
    }

    private EvalExpression bindVariable(Object varMarker, Slot slot, EvalExpression expression) {
        return expression.visit(
                lambda -> {
                    // TODO: Is it possible to get ambiguous markers? What if a lambda in the
                    // original code is able to have a copy of itself with both an outer variable
                    // and normal unbound variable?
                    if (lambda.varMarker() == varMarker) {
                        return lambda;
                    }
                    return EvalLambda.create(
                            lambda.varMarker(), lambda.originalVarName(),
                            bindVariable(varMarker, slot, lambda.body()));
                },
                funcCall -> EvalFuncCall.create(
                        bindVariable(varMarker, slot, funcCall.func()),
                        bindVariable(varMarker, slot, funcCall.arg())),
                boundVariable -> {
                    if (containsUsage(varMarker, boundVariable.slot().expr)) {
                        return EvalFuncCall.create(
                                EvalLambda.create(varMarker, slot.originalVarName, boundVariable),
                                slot.expr);
                    } else {
                        return boundVariable;
                    }
                },
                unboundVariable -> {
                    if (unboundVariable.varMarker() != varMarker) {
                        return unboundVariable;
                    }
                    return EvalBoundVariable.create(slot);
                },
                freeVariable -> freeVariable
        );
    }

    private boolean containsUsage(Object varMarker, EvalExpression expression) {
        return expression.visit(
                lambda -> containsUsage(varMarker, lambda.body()),
                funcCall -> containsUsage(varMarker, funcCall.arg()) ||
                        containsUsage(varMarker, funcCall.func()),
                boundVariable -> containsUsage(varMarker, boundVariable.slot().expr),
                unboundVariable -> unboundVariable.varMarker() == varMarker,
                freeVariable -> false
        );
    }

    private boolean containsOnlyFreeVars(EvalExpression expression, Set<Object> freeMarkers) {
        return expression.visit(
                // TODO: Think more about this. The expression has been evaluated, so what we're
                // really saying here is that the expression isn't going to change anymore.
                lambda -> true,
                funcCall -> containsOnlyFreeVars(funcCall.arg(), freeMarkers) &&
                        containsOnlyFreeVars(funcCall.func(), freeMarkers),
                boundVariable -> containsOnlyFreeVars(boundVariable.slot().expr, freeMarkers),
                unboundVariable -> freeMarkers.contains(unboundVariable.varMarker()),
                freeVariable -> true
        );
    }

    /**
     * Given an expression, turn it into an EvalExpression, attaching all necessary metadata.
     */
    private EvalExpression compile(Map<String, Object> context, Expression expression) {
        return expression.visit(
                lambda -> {
                    Object varMarker = new Object();
                    Object oldMarker = context.put(lambda.varName(), varMarker);
                    EvalExpression compiledBody = compile(context, lambda.body());
                    context.put(lambda.varName(), oldMarker);
                    return EvalLambda.create(varMarker, lambda.varName(), compiledBody);
                },
                funcCall -> EvalFuncCall.create(
                        compile(context, funcCall.func()), compile(context, funcCall.arg())),
                variable -> {
                    Object varMarker = context.get(variable.varName());
                    if (varMarker == null) {
                        return EvalFreeVariable.create(variable.varName());
                    } else {
                        return EvalUnboundVariable.create(varMarker, variable.varName());
                    }
                }
        );
    }

    private Expression toExpression(EvalExpression evalExpression) {
        return evalExpression.visit(
                // TODO: Deal with cases where we need to change the var name.
                lambda -> Lambda.create(lambda.originalVarName(), toExpression(lambda.body())),
                funcCall -> FuncCall.create(
                        toExpression(funcCall.func()), toExpression(funcCall.arg())),
                boundVariable -> {
                    throw new IllegalStateException(
                            "Bound variables shouldn't exist in value expressions.");
                },
                // TODO: Deal with cases where the var name needs to change.
                unboundVariable -> Variable.create(unboundVariable.originalVarName()),
                freeVariable -> Variable.create(freeVariable.varName())
        );
    }
}
