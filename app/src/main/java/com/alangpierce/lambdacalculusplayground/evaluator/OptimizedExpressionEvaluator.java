package com.alangpierce.lambdacalculusplayground.evaluator;

import android.util.Log;

import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.expression.FuncCall;
import com.alangpierce.lambdacalculusplayground.expression.Lambda;
import com.alangpierce.lambdacalculusplayground.expression.Variable;
import com.google.common.base.Throwables;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OptimizedExpressionEvaluator implements ExpressionEvaluator {
    private static final String TAG = "ExpressionEvaluator";

    @Override
    public Expression evaluate(Expression expression) throws EvaluationFailedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        Future<Expression> future = executorService.submit(() -> evaluateInterruptible(expression));
        try {
            return future.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw Throwables.propagate(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof StackOverflowError) {
                throw new EvaluationFailedException("Evaluation took too long.");
            }
            Log.e(TAG, "Error evaluating expression.", e.getCause());
            throw new EvaluationFailedException("Something went wrong. :-(");
        } catch (TimeoutException e) {
            throw new EvaluationFailedException("Evaluation took too long.");
        } finally {
            future.cancel(true);
        }

    }

    private Expression evaluateInterruptible(Expression expression) throws InterruptedException {
        EvalExpression evalExpr = compile(new HashMap<>(), expression);
        EvalExpression result;
        try {
            result = evaluateRec(evalExpr, new HashSet<>(), true);
        } catch (EvaluationInterruptedException e) {
            // Clear the interrupted flag since we're switching to throwing InterruptedException.
            if (!Thread.interrupted()) {
                Log.w(TAG, "Caught EvaluationInterruptedException but the thread was not interrupted.");
            }
            throw new InterruptedException();
        }
        return toExpression(result);
    }

    /**
     * Special exception for dealing with the interrupted flag. We use an unchecked exception
     * because checked exceptions don't play very well with the visitor pattern.
     */
    private static class EvaluationInterruptedException extends RuntimeException {
    }

    private EvalExpression evaluateRec(EvalExpression evalExpression, Set<Object> freeMarkers, boolean topLevel) {
        if (Thread.currentThread().isInterrupted()) {
            throw new EvaluationInterruptedException();
        }
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
