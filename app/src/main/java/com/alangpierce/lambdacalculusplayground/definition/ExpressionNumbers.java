package com.alangpierce.lambdacalculusplayground.definition;

import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.expression.Expressions;
import com.alangpierce.lambdacalculusplayground.expression.FuncCall;
import com.alangpierce.lambdacalculusplayground.expression.Lambda;
import com.alangpierce.lambdacalculusplayground.expression.Variable;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

import javax.annotation.Nullable;

public class ExpressionNumbers {
    /**
     * If this expression is a number, return a string with that number. For example,
     * L s[S z[s(s(z))]] is converted to "2". Note that the variables must be named "s" and "z".
     *
     * If the expression is not a number, returns null.
     */
    public static String tryResolveToNumber(Expression expression) {
        if (!(expression instanceof Lambda)) {
            return null;
        }
        Lambda lambda = (Lambda) expression;
        if (!lambda.varName().equals("s")) {
            return null;
        }
        expression = lambda.body();
        if (!(expression instanceof Lambda)) {
            return null;
        }
        lambda = (Lambda) expression;
        if (!lambda.varName().equals("z")) {
            return null;
        }
        expression = lambda.body();
        int result = 0;
        while (expression instanceof FuncCall) {
            FuncCall funcCall = ((FuncCall) expression);
            if (!funcCall.func().equals(Variable.create("s"))) {
                return null;
            }
            expression = funcCall.arg();
            result++;
        }
        if (!expression.equals(Variable.create("z"))) {
            return null;
        }
        return Integer.toString(result);
    }

    public static boolean isNumber(String defName) {
        try {
            int value = Integer.parseInt(defName);
            // We only allow natural (non-negative) numbers in canonical form. So -3 and 05 are both
            // disallowed.
            return value >= 0 && Integer.toString(value).equals(defName);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * If the given string is a number, return an expression for it. Otherwise, return null;
     */
    public static @Nullable Expression tryExpressionForNumber(String number) {
        if (!isNumber(number)) {
            return null;
        }
        int value = Integer.parseInt(number);

        Expression result = Variable.create("z");
        for (int i = 0; i < value; i++) {
            result = FuncCall.create(Variable.create("s"), result);
        }
        result = Lambda.create("s", Lambda.create("z", result));
        return result;
    }

    public static @Nullable UserExpression tryUserExpressionForNumber(String number)
            throws ExpressionTooBigException {
        // Refuse to show the user the expanded form for gigantic numbers, since it'll cause
        // performance issues.
        if (isNumber(number) && Integer.parseInt(number) > 30) {
            throw new ExpressionTooBigException();
        }

        @Nullable Expression expression = tryExpressionForNumber(number);
        if (expression == null) {
            return null;
        }
        return Expressions.toUserExpression(expression);
    }
}
