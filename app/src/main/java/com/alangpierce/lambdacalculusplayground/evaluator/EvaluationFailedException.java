package com.alangpierce.lambdacalculusplayground.evaluator;

/**
 * A checked exception that indicates that the evaluation failed. The message is a user-facing
 * string with the problem.
 */
public class EvaluationFailedException extends Exception {
    public EvaluationFailedException(String detailMessage) {
        super(detailMessage);
    }
}
