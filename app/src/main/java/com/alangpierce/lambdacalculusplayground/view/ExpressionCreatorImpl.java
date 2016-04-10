package com.alangpierce.lambdacalculusplayground.view;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.alangpierce.lambdacalculusplayground.ExpressionCreator;
import com.alangpierce.lambdacalculusplayground.R;
import com.alangpierce.lambdacalculusplayground.TopLevelExpressionManager;
import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverter;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;

public class ExpressionCreatorImpl implements ExpressionCreator {
    private final Context context;
    private final LayoutInflater layoutInflater;
    private final TopLevelExpressionManager expressionManager;
    private final PointConverter pointConverter;

    public ExpressionCreatorImpl(Context context, LayoutInflater layoutInflater,
            TopLevelExpressionManager expressionManager,
            PointConverter pointConverter) {
        this.context = context;
        this.layoutInflater = layoutInflater;
        this.expressionManager = expressionManager;
        this.pointConverter = pointConverter;
    }

    @Override
    public void promptCreateDefinition() {
        View inputView = layoutInflater.inflate(R.layout.definition_name_dialog, null);
        EditText nameEditText = (EditText) inputView.findViewById(R.id.definition_name);
        AlertDialog alertDialog = new Builder(context)
                .setTitle(R.string.create_definition)
                .setView(inputView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    addDefinitionWithName(nameEditText.getText().toString());
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

    private void addDefinitionWithName(String defName) {
        if (!isValidDefinitionName(defName)) {
            Toast.makeText(context,
                    "Definition names can only contain capital letters and symbols.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the view at (50dp, 50dp).
        int shiftPixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 50f, context.getResources().getDisplayMetrics());
        boolean alreadyOnCanvas = expressionManager.placeDefinition(
                defName, DrawableAreaPoint.create(shiftPixels, shiftPixels));
        if (alreadyOnCanvas) {
            Toast.makeText(context, "Showing existing definition.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidDefinitionName(String string) {
        for (char c : string.toCharArray()) {
            if (Character.isLowerCase(c)) {
                return false;
            }
        }
        return true;
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
        if (!isValidVariableName(varName)) {
            Toast.makeText(context, "Variable names can only contain lower-case letters.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the view at (50dp, 50dp).
        int shiftPixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 50f, context.getResources().getDisplayMetrics());
        DrawableAreaPoint drawableAreaPoint = DrawableAreaPoint.create(shiftPixels, shiftPixels);
        ScreenPoint screenPoint = pointConverter.toScreenPoint(drawableAreaPoint);
        UserLambda expression = UserLambda.create(varName, null);
        expressionManager.createNewExpression(
                expression, screenPoint, false /* placeAbovePalette */);
    }

    private boolean isValidVariableName(String string) {
        for (char c : string.toCharArray()) {
            if (!Character.isLowerCase(c)) {
                return false;
            }
        }
        return true;
    }
}
