package com.alangpierce.lambdacalculusplayground.userexpression;

import android.util.Log;

import com.alangpierce.lambdacalculusplayground.definition.DefinitionManager;
import com.alangpierce.lambdacalculusplayground.evaluator.ExpressionEvaluator;
import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.expression.Expressions;
import com.alangpierce.lambdacalculusplayground.expression.FuncCall;
import com.alangpierce.lambdacalculusplayground.expression.Lambda;
import com.alangpierce.lambdacalculusplayground.expression.Variable;

import javax.annotation.Nullable;

public class UserExpressionEvaluatorImpl implements UserExpressionEvaluator {
    private static final String TAG = "UserExpressionEvaluator";

    private final DefinitionManager definitionManager;
    private final ExpressionEvaluator expressionEvaluator;

    public UserExpressionEvaluatorImpl(DefinitionManager definitionManager,
            ExpressionEvaluator expressionEvaluator) {
        this.definitionManager = definitionManager;
        this.expressionEvaluator = expressionEvaluator;
    }

    @Override
    public boolean canStep(UserExpression userExpression) {
        return step(userExpression) != null;
    }

    /**
     * Run an expression to completion.
     *
     * If it takes more than 1000 steps to finish, just run it 1000 steps.
     */
    @Override
    public @Nullable UserExpression evaluate(UserExpression userExpression) {
        try {
            Expression expression = toExpression(userExpression);
            expression = expressionEvaluator.evaluate(expression);
            return collapseDefinedTerms(fromExpression(expression));
        } catch (InvalidExpressionException e) {
            // If there's a problem, just ignore the operation. This isn't great, but is better than
            // crashing.
            Log.e(TAG, "Evaluated an invalid expression.");
            return null;
        }
    }

    private UserExpression step(UserExpression userExpression) {
        try {
            Expression expression = toExpression(userExpression);
            @Nullable Expression steppedExpression = Expressions.step(expression);
            if (steppedExpression == null) {
                return null;
            }
            return fromExpression(steppedExpression);
        } catch (InvalidExpressionException e) {
            return null;
        }
    }

    /**
     * Make a best-effort attempt at cleaning up the expression to use definition references where
     * possible.
     */
    private UserExpression collapseDefinedTerms(UserExpression userExpression) {
        String defName = definitionManager.tryResolveExpression(toExpression(userExpression));
        if (defName != null) {
            return UserReference.create(defName);
        }
        return userExpression.visit(
                lambda -> UserLambda.create(lambda.varName(), collapseDefinedTerms(lambda.body())),
                funcCall -> UserFuncCall.create(collapseDefinedTerms(funcCall.func()), collapseDefinedTerms(funcCall.arg())),
                variable -> variable,
                reference -> reference
        );
    }

    /**
     * Given an expression, convert to a UserExpression. This will always succeed.
     */
    private static UserExpression fromExpression(Expression e) {
        return e.visit(
                lambda -> UserLambda.create(lambda.varName(), fromExpression(lambda.body())),
                funcCall -> UserFuncCall.create(
                        fromExpression(funcCall.func()),
                        fromExpression(funcCall.arg())),
                variable -> UserVariable.create(variable.varName())
        );
    }

    private static class InvalidExpressionException extends RuntimeException {
    }

    @Override
    public @Nullable Expression convertToExpression(@Nullable UserExpression userExpression) {
        if (userExpression == null) {
            return null;
        }
        try {
            return toExpression(userExpression);
        } catch (InvalidExpressionException e) {
            return null;
        }
    }

    /**
     * Given a UserExpression, convert to an Expression if possible.
     * <p>
     * throws InvalidExpressionException if there was a problem.
     */
    private Expression toExpression(UserExpression e) throws InvalidExpressionException {
        return e.visit(
                lambda -> {
                    if (lambda.body() == null) {
                        throw new InvalidExpressionException();
                    }
                    return Lambda.create(lambda.varName(), toExpression(lambda.body()));
                },
                funcCall -> FuncCall.create(toExpression(funcCall.func()), toExpression(funcCall.arg())),
                variable -> Variable.create(variable.varName()),
                reference -> {
                    Expression expression =
                            definitionManager.resolveDefinition(reference.defName());
                    if (expression == null) {
                        throw new InvalidExpressionException();
                    }
                    return expression;
                }
        );
    }
}
