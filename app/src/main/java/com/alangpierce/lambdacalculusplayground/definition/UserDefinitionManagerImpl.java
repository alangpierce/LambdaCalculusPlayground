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
        return appState.getAllDefinitions().get(defName);
    }
}
