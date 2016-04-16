package com.alangpierce.lambdacalculusplayground.userexpression;

import android.util.Log;

import com.alangpierce.lambdacalculusplayground.R;
import com.alangpierce.lambdacalculusplayground.definition.DefinitionManager;
import com.alangpierce.lambdacalculusplayground.definition.DefinitionManager.InvalidExpressionException;
import com.alangpierce.lambdacalculusplayground.evaluator.EvaluationFailedException;
import com.alangpierce.lambdacalculusplayground.evaluator.ExpressionEvaluator;
import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.expression.Expressions;

import javax.annotation.Nullable;

public class UserExpressionEvaluatorImpl implements UserExpressionEvaluator {
    private static final String TAG = "UserExpressionEvaluator";

    private final DefinitionManager definitionManager;
    private final ExpressionEvaluator expressionEvaluator;

    public UserExpressionEvaluatorImpl(DefinitionManager definitionManager,
            ExpressionEvaluator expressionEvaluator) {
        this.definitionManager = definitionManager;
        this.expressionEvaluator = expressionEvaluator;
    }

    @Override
    public boolean canStep(UserExpression userExpression) {
        try {
            Expression expression = definitionManager.toExpression(userExpression);
            return Expressions.canStep(expression);
        } catch (InvalidExpressionException e) {
            return false;
        }
    }

    /**
     * Run an expression to completion. If there was any kind of problem, instead throw an exception
     * with a user-facing message with a description of the problem.
     */
    @Override
    public UserExpression evaluate(UserExpression userExpression) throws EvaluationFailedException {
        try {
            Expression expression = definitionManager.toExpression(userExpression);
            expression = expressionEvaluator.evaluate(expression);
            UserExpression result = collapseDefinedTerms(Expressions.toUserExpression(expression));
            if (expressionSize(result) > 30) {
                throw new EvaluationFailedException(R.string.error_result_too_big);
            }
            return result;
        } catch (InvalidExpressionException e) {
            // This should never happen, so give a generic error message.
            Log.e(TAG, "Unexpected invalid expression.", e);
            throw new EvaluationFailedException(R.string.error_something_went_wrong);
        }
    }

    /**
     * Determine a rough measure of how big an expression is from a user's perspective. An
     * expression that's too big will be ignored because it's too unwieldy.
     */
    private int expressionSize(UserExpression userExpression) {
        return userExpression.visit(
                lambda -> 1 + expressionSize(lambda.body()),
                funcCall -> expressionSize(funcCall.func()) + expressionSize(funcCall.arg()),
                variable -> 1,
                reference -> 1);
    }

    /**
     * Make a best-effort attempt at cleaning up the expression to use definition references where
     * possible.
     */
    private UserExpression collapseDefinedTerms(UserExpression userExpression) {
        // TODO: Maybe just resolve the UserExpression?
        String defName = definitionManager.tryResolveExpression(
                definitionManager.toExpression(userExpression));
        if (defName != null) {
            return UserReference.create(defName);
        }
        return userExpression.visit(
                lambda -> UserLambda.create(lambda.varName(), collapseDefinedTerms(lambda.body())),
                funcCall -> UserFuncCall.create(collapseDefinedTerms(funcCall.func()), collapseDefinedTerms(funcCall.arg())),
                variable -> variable,
                reference -> reference
        );
    }

    @Override
    public @Nullable Expression convertToExpression(@Nullable UserExpression userExpression) {
        if (userExpression == null) {
            return null;
        }
        try {
            return definitionManager.toExpression(userExpression);
        } catch (InvalidExpressionException e) {
            return null;
        }
    }
}
