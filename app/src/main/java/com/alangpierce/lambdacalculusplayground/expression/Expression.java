package com.alangpierce.lambdacalculusplayground.expression;

public interface Expression {
    <T> T visit(
            Visitor<Lambda, T> lambdaVisitor,
            Visitor<FuncCall, T> funcCallVisitor,
            Visitor<Variable, T> variableVisitor);

    interface Visitor<V, R> {
        R accept(V value);
    }
}
