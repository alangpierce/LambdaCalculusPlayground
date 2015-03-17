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
        return e.visit(new Expression.ExpressionVisitor<UserExpression>() {
            @Override
            public UserExpression visit(Lambda lambda) {
                return new UserLambda(lambda.varName, fromExpression(lambda.body));
            }
            @Override
            public UserExpression visit(FuncCall funcCall) {
                return new UserFuncCall(
                        fromExpression(funcCall.func),
                        fromExpression(funcCall.arg));
            }
            @Override
            public UserExpression visit(Variable variable) {
                return new UserVariable(variable.varName);
            }
        });
    }

    public static class InvalidExpressionException extends RuntimeException {}

    /**
     * Given a UserExpression, convert to an Expression if possible.
     *
     * throws InvalidExpressionException if there was a problem.
     */
    public static Expression toExpression(UserExpression e) throws InvalidExpressionException {
        return e.visit(new UserExpression.UserExpressionVisitor<Expression>() {
            @Override
            public Expression visit(UserLambda lambda) {
                if (lambda.body == null) {
                    throw new InvalidExpressionException();
                }
                return new Lambda(lambda.varName, toExpression(lambda.body));
            }
            @Override
            public Expression visit(UserFuncCall funcCall) {
                return new FuncCall(toExpression(funcCall.func), toExpression(funcCall.arg));
            }
            @Override
            public Expression visit(UserVariable variable) {
                return new Variable(variable.varName);
            }
        });
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
     *
     * Returns null if we suspect that we're in an infinite loop.
     */
    public static @Nullable UserExpression evaluate(UserExpression userExpression) {
        for (int i = 0; canStep(userExpression); i++) {
            if (i == 100) {
                return null;
            }
            userExpression = step(userExpression);
        }
        return userExpression;
    }

}
