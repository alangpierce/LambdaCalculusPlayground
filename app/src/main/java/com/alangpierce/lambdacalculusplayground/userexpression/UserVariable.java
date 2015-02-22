package com.alangpierce.lambdacalculusplayground.userexpression;

public class UserVariable implements UserExpression {
    public final String varName;

    public UserVariable(String varName) {
        this.varName = varName;
    }

    @Override
    public <T> T visit(UserExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserVariable that = (UserVariable) o;

        if (varName != null ? !varName.equals(that.varName) : that.varName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return varName != null ? varName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UserVariable{" +
                "varName='" + varName + '\'' +
                '}';
    }
}