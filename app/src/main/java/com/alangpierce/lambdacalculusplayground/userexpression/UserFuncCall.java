package com.alangpierce.lambdacalculusplayground.userexpression;

public class UserFuncCall implements UserExpression {
    public final UserExpression func;
    public final UserExpression arg;

    public UserFuncCall(UserExpression func, UserExpression arg) {
        this.func = func;
        this.arg = arg;
    }

    @Override
    public <T> T visit(UserExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserFuncCall that = (UserFuncCall) o;

        if (arg != null ? !arg.equals(that.arg) : that.arg != null) return false;
        if (func != null ? !func.equals(that.func) : that.func != null) return false;

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
        return "UserFuncCall{" +
                "func=" + func +
                ", arg=" + arg +
                '}';
    }
}
