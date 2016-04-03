package com.alangpierce.lambdacalculusplayground.definition;

import com.alangpierce.lambdacalculusplayground.expression.Expression;

import javax.annotation.Nullable;

public interface DefinitionManager {
    @Nullable Expression resolveDefinition(String definitionName);

    /**
     * Returns the name of the given expression, or null if the expression doesn't match a
     * definition (as far as we can tell). Note that the current implementation requires all
     * variable names to match up.
     */
    @Nullable String tryResolveExpression(Expression expression);

    void updateDefinition(String name, @Nullable Expression expression);
}
