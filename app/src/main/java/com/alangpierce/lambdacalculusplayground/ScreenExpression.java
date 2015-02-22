package com.alangpierce.lambdacalculusplayground;

import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

import java.io.Serializable;

/**
 * Data object for a top-level expression visible on the screen.
 */
public class ScreenExpression implements Serializable {
    public final UserExpression expr;
    public final int x;
    public final int y;

    public ScreenExpression(UserExpression expr, int x, int y) {
        this.expr = expr;
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScreenExpression that = (ScreenExpression) o;

        if (x != that.x) return false;
        if (y != that.y) return false;
        if (expr != null ? !expr.equals(that.expr) : that.expr != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = expr != null ? expr.hashCode() : 0;
        result = 31 * result + x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "ScreenExpression{" +
                "expr=" + expr +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
