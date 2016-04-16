package com.alangpierce.lambdacalculusplayground.userexpression;

import com.google.auto.value.AutoValue;

/**
 * A reference is like a variable, but refers to a particular definition.
 */
@AutoValue
public abstract class UserReference implements UserExpression {
    public abstract String defName();

    public static UserReference create(String defName) {
        return new AutoValue_UserReference(defName);
    }

    @Override
    public <T> T visit(Visitor<UserLambda, T> lambdaVisitor,
            Visitor<UserFuncCall, T> funcCallVisitor, Visitor<UserVariable, T> variableVisitor,
            Visitor<UserReference, T> referenceVisitor) {
        return referenceVisitor.accept(this);
    }

    @Override
    public String toString() {
        return defName();
    }
}
