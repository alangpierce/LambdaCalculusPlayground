package com.alangpierce.lambdacalculusplayground.expression;

public class Variable implements Expression {
    public final String varName;

    public Variable(String varName) {
        this.varName = varName;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Variable variable = (Variable) o;

        if (varName != null ? !varName.equals(variable.varName) : variable.varName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return varName != null ? varName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Variable{" +
                "varName='" + varName + '\'' +
                '}';
    }
}