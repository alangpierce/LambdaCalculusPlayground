package com.alangpierce.lambdacalculusplayground.userexpression;

import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.expression.FuncCall;
import com.alangpierce.lambdacalculusplayground.expression.Lambda;
import com.alangpierce.lambdacalculusplayground.expression.Variable;

public class UserExpressions {
    public static UserExpression fromExpression(Expression e) {
        return e.visit(new Expression.ExpressionVisitor<UserExpression>() {
            @Override
            public UserExpression visit(Lambda lambda) {
                return new UserLambda(lambda.varName, fromExpression(lambda.body));
            }
            @Override
            public UserExpression visit(FuncCall funcCall) {
                return new UserFuncCall(fromExpression(funcCall.arg),
                        fromExpression(funcCall.func));
            }
            @Override
            public UserExpression visit(Variable variable) {
                return new UserVariable(variable.varName);
            }
        });
    }
}
