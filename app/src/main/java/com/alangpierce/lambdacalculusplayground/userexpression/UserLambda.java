package com.alangpierce.lambdacalculusplayground.userexpression;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class UserLambda implements UserExpression {
    public abstract String varName();
    public abstract @Nullable UserExpression body();

    public static UserLambda create(String varName, @Nullable UserExpression body) {
        return new AutoValue_UserLambda(varName, body);
    }

    @Override
    public <T> T visit(UserExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
