package com.alangpierce.lambdacalculusplayground.userexpression;

import javax.annotation.Nullable;

public class UserLambda implements UserExpression {
    public final String varName;
    public final @Nullable UserExpression body;

    public UserLambda(String varName, @Nullable UserExpression body) {
        this.varName = varName;
        this.body = body;
    }

    @Override
    public <T> T visit(UserExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserLambda that = (UserLambda) o;

        if (body != null ? !body.equals(that.body) : that.body != null) return false;
        if (varName != null ? !varName.equals(that.varName) : that.varName != null) return false;

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
        return "UserLambda{" +
                "varName='" + varName + '\'' +
                ", body=" + body +
                '}';
    }
}
