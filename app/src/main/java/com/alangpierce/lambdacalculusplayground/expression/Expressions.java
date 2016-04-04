package com.alangpierce.lambdacalculusplayground.expression;

import javax.annotation.Nullable;

public class Expressions {
    /**
     * Evaluate an expression for a single step.
     *
     * @return An expression, or null if this expression cannot be computed any further.
     */
    public static @Nullable Expression step(Expression e) {
        return e.visit(
                lambda -> {
                    Expression newBody = step(lambda.body());
                    if (newBody == null) {
                        return null;
                    }
                    return Lambda.create(lambda.varName(), newBody);
                },
                funcCall -> {
                    if (funcCall.func() instanceof Lambda) {
                        Lambda func = (Lambda) funcCall.func();
                        return replaceVariable(func.body(), func.varName(), funcCall.arg());
                    }
                    @Nullable Expression steppedArg = step(funcCall.arg());
                    if (steppedArg != null) {
                        return FuncCall.create(funcCall.func(), steppedArg);
                    }
                    @Nullable Expression steppedFunc = step(funcCall.func());
                    if (steppedFunc != null) {
                        return FuncCall.create(steppedFunc, funcCall.arg());
                    }
                    return null;
                },
                variable -> null
        );
    }

    private static Expression replaceVariable(Expression e, final String varName,
                                             final Expression replacement) {
        return e.visit(
                lambda -> {
                    // Lambda variable shadows outer variable name.
                    if (lambda.varName().equals(varName)) {
                        return lambda;
                    }

                    /*
                     * If the lambda parameter exists in the replacement expression, we'll introduce
                     * a name clash. To solve this, bump our variable name to be different by adding
                     * a prime to the end, and do that replacement in the whole expression.
                     */
                    String newVarName = lambda.varName();
                    while (containsVariableUsage(replacement, newVarName)) {
                        newVarName += "'";
                    }
                    Expression newBody = lambda.body();
                    if (!newVarName.equals(lambda.varName())) {
                        newBody =
                                replaceVariable(newBody, lambda.varName(),
                                        Variable.create(newVarName));
                    }
                    return Lambda
                            .create(newVarName, replaceVariable(newBody, varName, replacement));
                },
                funcCall -> FuncCall.create(
                        replaceVariable(funcCall.func(), varName, replacement),
                        replaceVariable(funcCall.arg(), varName, replacement)),
                variable -> {
                    if (variable.varName().equals(varName)) {
                        return replacement;
                    } else {
                        return variable;
                    }
                }
        );
    }

    private static boolean containsVariableUsage(Expression e, String varName) {
        return e.visit(
                lambda -> lambda.varName().equals(varName) ||
                        containsVariableUsage(lambda.body(), varName),
                funcCall -> containsVariableUsage(funcCall.func(), varName) ||
                        containsVariableUsage(funcCall.arg(), varName),
                variable -> variable.varName().equals(varName)
        );
    }

    /**
     * Since we sometimes can get variable names with primes in them, try removing them if they're
     * no longer needed.
     */
    public static Expression normalizeNames(Expression e) {
        return e.visit(
                lambda -> {
                    String varName = lambda.varName();
                    Expression body = lambda.body();
                    if (varName.endsWith("'")) {
                        String newVarName = varName;
                        while (newVarName.endsWith("'")) {
                            newVarName = newVarName.substring(0, newVarName.length() - 1);
                        }
                        if (!containsVariableUsage(lambda.body(), newVarName)) {
                            body = replaceVariable(body, varName, Variable.create(newVarName));
                            varName = newVarName;
                        }
                    }
                    return Lambda.create(varName, normalizeNames(body));
                },
                funcCall -> FuncCall.create(
                        normalizeNames(funcCall.func()), normalizeNames(funcCall.arg())),
                variable -> variable
        );
    }
}
