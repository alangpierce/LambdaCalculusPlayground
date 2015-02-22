package com.alangpierce.lambdacalculusplayground.expression;

import java.io.Serializable;

public interface Expression extends Serializable {
    <T> T visit(ExpressionVisitor<T> visitor);

    public interface ExpressionVisitor<T> {
        T visit(Lambda lambda);
        T visit(FuncCall funcCall);
        T visit(Variable variable);
    }
}
