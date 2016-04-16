package com.alangpierce.lambdacalculusplayground.expression;

import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserFuncCall;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.alangpierce.lambdacalculusplayground.userexpression.UserVariable;

public class Expressions {
    public static boolean canStep(Expression e) {
        return e.visit(
                lambda -> canStep(lambda.body()),
                funcCall -> funcCall.func() instanceof Lambda ||
                        canStep(funcCall.func()) ||
                        canStep(funcCall.arg()),
                variable -> false
        );
    }

    /**
     * Given an expression, convert to a UserExpression. This will always succeed.
     */
    public static UserExpression toUserExpression(Expression e) {
        return e.visit(
                lambda -> UserLambda.create(lambda.varName(), toUserExpression(lambda.body())),
                funcCall -> UserFuncCall.create(
                        toUserExpression(funcCall.func()),
                        toUserExpression(funcCall.arg())),
                variable -> UserVariable.create(variable.varName())
        );
    }
}
