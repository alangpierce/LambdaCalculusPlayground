package com.alangpierce.lambdacalculusplayground.userexpression;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UserVariable implements UserExpression {
    public abstract String varName();

    public static UserVariable create(String varName) {
        return new AutoValue_UserVariable(varName);
    }

    @Override
    public <T> T visit(Visitor<UserLambda, T> lambdaVisitor,
            Visitor<UserFuncCall, T> funcCallVisitor,
            Visitor<UserVariable, T> variableVisitor) {
        return variableVisitor.accept(this);
    }

    @Override
    public String toString() {
        return varName();
    }
}
