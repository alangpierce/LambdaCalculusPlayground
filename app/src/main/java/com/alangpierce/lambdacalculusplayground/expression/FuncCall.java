package com.alangpierce.lambdacalculusplayground.expression;

import java.io.Serializable;

public class FuncCall implements Expression {
    public final Expression func;
    public final Expression arg;

    public FuncCall(Expression func, Expression arg) {
        this.func = func;
        this.arg = arg;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FuncCall funcCall = (FuncCall) o;

        if (arg != null ? !arg.equals(funcCall.arg) : funcCall.arg != null) return false;
        if (func != null ? !func.equals(funcCall.func) : funcCall.func != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = func != null ? func.hashCode() : 0;
        result = 31 * result + (arg != null ? arg.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FuncCall{" +
                "func=" + func +
                ", arg=" + arg +
                '}';
    }
}
