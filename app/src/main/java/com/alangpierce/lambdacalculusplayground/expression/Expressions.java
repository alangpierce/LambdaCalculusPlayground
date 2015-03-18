package com.alangpierce.lambdacalculusplayground.expression;

import com.alangpierce.lambdacalculusplayground.expression.Expression.ExpressionVisitor;

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

                /*
                 * If the lambda parameter exists in the replacement expression, we'll introduce a
                 * name clash. To solve this, bump our variable name to be different by adding a
                 * prime to the end, and do that replacement in the whole expression.
                 */
                String newVarName = lambda.varName;
                while (containsVariableUsage(replacement, newVarName)) {
                    newVarName += "'";
                }
                Expression newBody = lambda.body;
                if (!newVarName.equals(lambda.varName)) {
                    newBody = replaceVariable(newBody, lambda.varName, new Variable(newVarName));
                }
                return new Lambda(newVarName, replaceVariable(newBody, varName, replacement));
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

    public static boolean containsVariableUsage(Expression e, String varName) {
        return e.visit(new ExpressionVisitor<Boolean>() {
            @Override
            public Boolean visit(Lambda lambda) {
                return lambda.varName.equals(varName) ||
                        containsVariableUsage(lambda.body, varName);
            }
            @Override
            public Boolean visit(FuncCall funcCall) {
                return containsVariableUsage(funcCall.func, varName) ||
                        containsVariableUsage(funcCall.arg, varName);
            }
            @Override
            public Boolean visit(Variable variable) {
                return variable.varName.equals(varName);
            }
        });
    }
}
