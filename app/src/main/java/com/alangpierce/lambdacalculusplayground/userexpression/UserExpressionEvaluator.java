package com.alangpierce.lambdacalculusplayground.userexpression;

import com.alangpierce.lambdacalculusplayground.expression.Expression;

import javax.annotation.Nullable;

public interface UserExpressionEvaluator {
    boolean canStep(UserExpression userExpression);
    @Nullable UserExpression evaluate(UserExpression userExpression);

    /**
     * Convert a UserExpression to an Expression, or null if the expression isn't valid.
     */
    @Nullable Expression convertToExpression(@Nullable UserExpression userExpression);
}
