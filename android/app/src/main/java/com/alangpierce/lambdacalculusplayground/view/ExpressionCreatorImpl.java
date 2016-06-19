package com.alangpierce.lambdacalculusplayground.view;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alangpierce.lambdacalculusplayground.AppState;
import com.alangpierce.lambdacalculusplayground.CanvasManager;
import com.alangpierce.lambdacalculusplayground.ExpressionCreator;
import com.alangpierce.lambdacalculusplayground.R;
import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverter;
import com.alangpierce.lambdacalculusplayground.reactnative.ReactNativeManager;
import com.google.common.collect.Ordering;

import java.util.List;

public class ExpressionCreatorImpl implements ExpressionCreator {
    private final Context context;
    private final LayoutInflater layoutInflater;
    private final CanvasManager canvasManager;
    private final ReactNativeManager reactNativeManager;
    private final AppState appState;

    public ExpressionCreatorImpl(Context context, LayoutInflater layoutInflater,
            CanvasManager canvasManager, ReactNativeManager reactNativeManager, AppState appState) {
        this.context = context;
        this.layoutInflater = layoutInflater;
        this.canvasManager = canvasManager;
        this.reactNativeManager = reactNativeManager;
        this.appState = appState;
    }

    private static class InvalidNameException extends Exception {
        private final @StringRes int stringRes;

        public InvalidNameException(@StringRes int stringRes) {
            this.stringRes = stringRes;
        }

        public int getStringRes() {
            return stringRes;
        }
    }

    @Override
    public void promptCreateLambda() {
        View inputView = layoutInflater.inflate(R.layout.lambda_name_dialog, null);
        EditText nameEditText = (EditText) inputView.findViewById(R.id.lambda_name);
        AlertDialog alertDialog = new Builder(context)
                .setTitle(R.string.create_lambda)
                .setView(inputView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    addLambdaWithName(nameEditText.getText().toString());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        nameEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                return true;
            }
            return false;
        });

        alertDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
    }

    private void addLambdaWithName(String varName) {
        try {
            validateVariableName(varName);
        } catch (InvalidNameException e) {
            Toast.makeText(context, e.getStringRes(), Toast.LENGTH_SHORT).show();
            return;
        }

        reactNativeManager.createLambda(varName);
//        ScreenPoint screenPoint = pointConverter.toScreenPoint(newExpressionPoint());
//        UserLambda expression = UserLambda.create(varName, null);
//        canvasManager.createNewExpression(
//                expression, screenPoint, false /* placeAbovePalette */);
    }

    private void validateVariableName(String varName) throws InvalidNameException {
        if (varName.length() > 8) {
            throw new InvalidNameException(R.string.bad_var_name_too_long);
        }

        for (char c : varName.toCharArray()) {
            if (!Character.isLowerCase(c)) {
                throw new InvalidNameException(R.string.bad_var_name_bad_chars);
            }
        }
    }

    private DrawableAreaPoint newExpressionPoint() {
        // Create the view at (50dp, 50dp).
        int shiftPixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 50f, context.getResources().getDisplayMetrics());

        // We also need to shift down to account for action bar height.
        TypedValue actionBarSize = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.actionBarSize, actionBarSize, true);
        int actionBarHeightPixels =
                (int) actionBarSize.getDimension(context.getResources().getDisplayMetrics());

        return DrawableAreaPoint.create(shiftPixels, shiftPixels + actionBarHeightPixels);
    }
}
