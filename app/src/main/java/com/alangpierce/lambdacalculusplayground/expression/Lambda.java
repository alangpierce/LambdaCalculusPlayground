package com.alangpierce.lambdacalculusplayground.expression;

public class Lambda implements Expression {
    public final String varName;
    public final Expression body;

    public Lambda(String varName, Expression body) {
        this.varName = varName;
        this.body = body;
    }

    @Override
    public <T> T visit(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lambda lambda = (Lambda) o;

        if (body != null ? !body.equals(lambda.body) : lambda.body != null) return false;
        if (varName != null ? !varName.equals(lambda.varName) : lambda.varName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = varName != null ? varName.hashCode() : 0;
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Lambda{" +
                "varName='" + varName + '\'' +
                ", body=" + body +
                '}';
    }
}
