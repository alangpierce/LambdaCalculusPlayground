package com.alangpierce.lambdacalculusplayground.evaluator;

import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.expression.FuncCall;
import com.alangpierce.lambdacalculusplayground.expression.Lambda;
import com.alangpierce.lambdacalculusplayground.expression.Variable;

import java.util.HashMap;
import java.util.Map;

public class OptimizedExpressionEvaluator implements ExpressionEvaluator {
    @Override
    public Expression evaluate(Expression expression) {
        EvalExpression evalExpr = compile(new HashMap<>(), expression);
        EvalExpression result = evaluateRec(evalExpr);
        return toExpression(result);
    }

    private EvalExpression evaluateRec(EvalExpression evalExpression) {
        return evalExpression.visit(
                lambda -> EvalLambda.create(
                        lambda.varMarker(), lambda.originalVarName(), evaluateRec(lambda.body())),
                funcCall -> {
                    // TODO: Maybe we don't want to fully evaluate the func if it's a lambda?
                    EvalExpression func = evaluateRec(funcCall.func());
                    if (func instanceof EvalLambda) {
                        EvalLambda lambda = (EvalLambda) func;
                        Slot slot = Slot.create(funcCall.arg());
                        return evaluateRec(bindVariable(
                                lambda.varMarker(), lambda.originalVarName(), slot, lambda.body()));
                    } else {
                        // We can't do anything more with the left side, so we might as well try
                        // evaluating the right side.
                        // TODO: What if the left side is an unbound variable that will later be
                        // bound? It could also be an expression consisting of unbound variables.
                        EvalExpression arg = evaluateRec(funcCall.arg());
                        return EvalFuncCall.create(func, arg);
                    }
                },
                boundVariable -> {
                    Slot slot = boundVariable.slot();
                    if (!slot.isValue) {
                        slot.expr = evaluateRec(slot.expr);
                        slot.isValue = true;
                    }
                    return slot.expr;
                },
                unboundVariable -> unboundVariable,
                freeVariable -> freeVariable
        );
    }

    private EvalExpression bindVariable(Object varMarker, String originalVarName, Slot slot, EvalExpression expression) {
        return expression.visit(
                lambda -> EvalLambda.create(
                        lambda.varMarker(), lambda.originalVarName(),
                        bindVariable(varMarker, originalVarName, slot, lambda.body())),
                funcCall -> EvalFuncCall.create(
                        bindVariable(varMarker, originalVarName, slot, funcCall.func()),
                        bindVariable(varMarker, originalVarName, slot, funcCall.arg())),
                boundVariable -> {
                    if (containsUsage(varMarker, boundVariable.slot().expr)) {
                        return EvalFuncCall.create(
                                EvalLambda.create(varMarker, originalVarName, boundVariable),
                                expression);
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
