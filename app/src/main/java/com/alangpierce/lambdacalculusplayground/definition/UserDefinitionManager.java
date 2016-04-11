package com.alangpierce.lambdacalculusplayground.definition;

import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Class for managing the active set of user-specified definitions (which may or may not be valid).
 */
public interface UserDefinitionManager {
    /**
     * Return a sorted array of all definition names.
     */
    List<String> getSortedDefinitionNames();

    /**
     * Called when the user wants to create a definition with this name. It either returns an
     * existing expression to use or returns null if a blank expression should be used.
     *
     * In particular, if automatic numbers are enabled, this might create a number expression even
     * if the number wasn't defined in our expression state.
     */
    @Nullable UserExpression resolveDefinitionForCreation(String defName);
}
