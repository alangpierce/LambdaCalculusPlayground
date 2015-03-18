package com.alangpierce.lambdacalculusplayground.expression;

import javax.annotation.Nullable;

public class Expressions {
    /**
     * Evaluate an expression for a single step.
     *
     * @return An expression, or null if this expression cannot be computed any further.
     */
    public static @Nullable Expression step(Expression e) {
        return e.visit(new Expression.ExpressionVisitor<Expression>() {
            @Override
            public @Nullable Expression visit(Lambda lambda) {
                Expression newBody = step(lambda.body);
                if (newBody == null) {
                    return null;
                }
                return new Lambda(lambda.varName, newBody);
            }
            @Override
            public @Nullable Expression visit(final FuncCall funcCall) {
                if (funcCall.func instanceof Lambda) {
                    Lambda func = (Lambda)funcCall.func;
                    return replaceVariable(func.body, func.varName, funcCall.arg);
                }
                @Nullable Expression steppedFunc = step(funcCall.func);
                if (steppedFunc != null) {
                    return new FuncCall(steppedFunc, funcCall.arg);
                }
                @Nullable Expression steppedArg = step(funcCall.arg);
                if (steppedArg != null) {
                    return new FuncCall(funcCall.func, steppedArg);
                }
                return null;
            }
            @Override
            public @Nullable Expression visit(Variable variable) {
                return null;
            }
        });
    }

    public static Expression replaceVariable(Expression e, final String varName,
                                             final Expression replacement) {
        return e.visit(new Expression.ExpressionVisitor<Expression>() {
            @Override
            public Expression visit(Lambda lambda) {
                // Lambda variable shadows outer variable name.
                if (lambda.varName.equals(varName)) {
                    return lambda;
                }
                return new Lambda(lambda.varName,
                        replaceVariable(lambda.body, varName, replacement));
            }
            @Override
            public Expression visit(FuncCall funcCall) {
                return new FuncCall(replaceVariable(funcCall.func, varName, replacement),
                        replaceVariable(funcCall.arg, varName, replacement));
            }
            @Override
            public Expression visit(Variable variable) {
                if (variable.varName.equals(varName)) {
                    return replacement;
                } else {
                    return variable;
                }
            }
        });
    }
}
