package com.alangpierce.lambdacalculusplayground.evaluator;

import android.support.annotation.StringRes;

/**
 * A checked exception that indicates that the evaluation failed. The message is a user-facing
 * string with the problem.
 */
public class EvaluationFailedException extends Exception {
    private final int stringRes;

    public EvaluationFailedException(@StringRes int stringRes) {
        this.stringRes = stringRes;
    }

    public int getStringRes() {
        return stringRes;
    }
}
