package com.alangpierce.lambdacalculusplayground.definition;

import com.alangpierce.lambdacalculusplayground.AppState;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.google.common.collect.Ordering;

import java.util.List;

import javax.annotation.Nullable;

public class UserDefinitionManagerImpl implements UserDefinitionManager {
    private final AppState appState;

    public UserDefinitionManagerImpl(AppState appState) {
        this.appState = appState;
    }

    @Override
    public List<String> getSortedDefinitionNames() {
        return Ordering.natural().sortedCopy(appState.getAllDefinitions().keySet());
    }

    @Override
    public @Nullable UserExpression resolveDefinitionForCreation(String defName) {
        // If the definition exists (even if it's empty), just return it.
        if (appState.getAllDefinitions().containsKey(defName)) {
            return appState.getAllDefinitions().get(defName);
        }
        if (appState.isAutomaticNumbersEnabled()) {
            return ExpressionNumbers.tryUserExpressionForNumber(defName);
        }
        return null;
    }
}
