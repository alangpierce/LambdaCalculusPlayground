package com.alangpierce.lambdacalculusplayground.expression;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Variable implements Expression {
    public abstract String varName();

    public static Variable create(String varName) {
        return new AutoValue_Variable(varName);
    }

    @Override
    public <T> T visit(Visitor<Lambda, T> lambdaVisitor, Visitor<FuncCall, T> funcCallVisitor,
            Visitor<Variable, T> variableVisitor) {
        return variableVisitor.accept(this);
    }
}
