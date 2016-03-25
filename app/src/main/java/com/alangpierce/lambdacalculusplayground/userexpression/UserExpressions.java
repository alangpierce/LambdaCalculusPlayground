package com.alangpierce.lambdacalculusplayground.userexpression;

import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.expression.Expressions;
import com.alangpierce.lambdacalculusplayground.expression.FuncCall;
import com.alangpierce.lambdacalculusplayground.expression.Lambda;
import com.alangpierce.lambdacalculusplayground.expression.Variable;

import javax.annotation.Nullable;

public class UserExpressions {
    /**
     * Given an expression, convert to a UserExpression. This will always succeed.
     */
    public static UserExpression fromExpression(Expression e) {
        return e.visit(
                lambda -> UserLambda.create(lambda.varName(), fromExpression(lambda.body())),
                funcCall -> UserFuncCall.create(
                        fromExpression(funcCall.func()),
                        fromExpression(funcCall.arg())),
                variable -> UserVariable.create(variable.varName())
        );
    }

    public static class InvalidExpressionException extends RuntimeException {
    }

    /**
     * Given a UserExpression, convert to an Expression if possible.
     * <p>
     * throws InvalidExpressionException if there was a problem.
     */
    public static Expression toExpression(UserExpression e) throws InvalidExpressionException {
        return e.visit(
                lambda -> {
                    if (lambda.body() == null) {
                        throw new InvalidExpressionException();
                    }
                    return Lambda.create(lambda.varName(), toExpression(lambda.body()));
                },
                funcCall -> FuncCall.create(toExpression(funcCall.func()), toExpression(funcCall.arg())),
                variable -> Variable.create(variable.varName())
        );
    }

    public static UserExpression step(UserExpression userExpression) {
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

    public static boolean canStep(UserExpression userExpression) {
        return step(userExpression) != null;
    }

    /**
     * Run an expression to completion.
     * <p>
     * Returns null if we suspect that we're in an infinite loop.
     */
    public static
    @Nullable
    UserExpression evaluate(UserExpression userExpression) {
        for (int i = 0; canStep(userExpression); i++) {
            if (i == 100) {
                return null;
            }
            userExpression = step(userExpression);
        }
        // TODO: Move this type of logic to just be in Expressions. I think this null check is
        // unnecessary, but I'm not 100% sure.
        if (userExpression != null) {
            userExpression =
                    fromExpression(Expressions.normalizeNames(toExpression(userExpression)));
        }
        return userExpression;
    }

}
