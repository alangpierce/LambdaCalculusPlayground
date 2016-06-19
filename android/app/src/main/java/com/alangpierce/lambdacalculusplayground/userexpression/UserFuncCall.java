package com.alangpierce.lambdacalculusplayground.userexpression;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UserFuncCall implements UserExpression {
    public abstract UserExpression func();
    public abstract UserExpression arg();

    public static UserFuncCall create(UserExpression func, UserExpression arg) {
        return new AutoValue_UserFuncCall(func, arg);
    }

    @Override
    public <T> T visit(Visitor<UserLambda, T> lambdaVisitor,
            Visitor<UserFuncCall, T> funcCallVisitor,
            Visitor<UserVariable, T> variableVisitor) {
        return funcCallVisitor.accept(this);
    }

    @Override
    public String toString() {
        return "" + func() + "(" + arg() + ")";
    }
}
