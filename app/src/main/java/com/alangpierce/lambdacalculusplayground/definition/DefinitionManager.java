package com.alangpierce.lambdacalculusplayground.definition;

import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

import java.util.Set;

import javax.annotation.Nullable;

public interface DefinitionManager {
    @Nullable UserExpression getUserDefinition(String definitionName);

    /**
     * Returns the name of the given expression, or null if the expression doesn't match a
     * definition (as far as we can tell). Note that the current implementation requires all
     * variable names to match up.
     */
    @Nullable String tryResolveExpression(Expression expression);

    void updateDefinition(String name, @Nullable UserExpression userExpression);

    Set<String> getDefinitionNames();

    class InvalidExpressionException extends RuntimeException {
    }

    /**
     * Resolve the UserExpression to an expression, expanding any definitions if necessary.
     */
    Expression toExpression(UserExpression userExpression) throws InvalidExpressionException;
}
