package com.alangpierce.lambdacalculusplayground.definition;

import com.alangpierce.lambdacalculusplayground.expression.Expression;

public interface DefinitionManager {
    Expression resolveDefinition(String definitionName);
}
