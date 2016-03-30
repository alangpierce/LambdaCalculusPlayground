package com.alangpierce.lambdacalculusplayground.userexpression;

public interface UserExpressionEvaluator {
    boolean canStep(UserExpression userExpression);
    UserExpression evaluate(UserExpression userExpression);
}
