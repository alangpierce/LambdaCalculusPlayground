package com.alangpierce.lambdacalculusplayground.expression;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Lambda implements Expression {
    public abstract String varName();
    public abstract Expression body();

    public static Lambda create(String varName, Expression body) {
        return new AutoValue_Lambda(varName, body);
    }

    @Override
    public <T> T visit(Visitor<Lambda, T> lambdaVisitor, Visitor<FuncCall, T> funcCallVisitor,
            Visitor<Variable, T> variableVisitor) {
        return lambdaVisitor.accept(this);
    }
}
