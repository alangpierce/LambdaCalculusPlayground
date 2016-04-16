package com.alangpierce.lambdacalculusplayground.expression;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class FuncCall implements Expression {
    public abstract Expression func();
    public abstract Expression arg();

    public static FuncCall create(Expression func, Expression arg) {
        return new AutoValue_FuncCall(func, arg);
    }

    @Override
    public <T> T visit(Visitor<Lambda, T> lambdaVisitor, Visitor<FuncCall, T> funcCallVisitor,
            Visitor<Variable, T> variableVisitor) {
        return funcCallVisitor.accept(this);
    }

    @Override
    public String toString() {
        return "" + func() + "(" + arg() + ")";
    }
}
