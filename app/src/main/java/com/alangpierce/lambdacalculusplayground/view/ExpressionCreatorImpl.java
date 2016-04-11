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
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.google.common.collect.Ordering;

import java.util.List;

public class ExpressionCreatorImpl implements ExpressionCreator {
    private final Context context;
    private final LayoutInflater layoutInflater;
    private final CanvasManager canvasManager;
    private final PointConverter pointConverter;
    private final AppState appState;

    public ExpressionCreatorImpl(Context context, LayoutInflater layoutInflater,
            CanvasManager canvasManager, PointConverter pointConverter, AppState appState) {
        this.context = context;
        this.layoutInflater = layoutInflater;
        this.canvasManager = canvasManager;
        this.pointConverter = pointConverter;
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
        try {
            validateDefinitionName(defName);
        } catch (InvalidNameException e) {
            String message = context.getString(e.getStringRes());
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean alreadyOnCanvas = canvasManager.placeDefinition(defName, newExpressionPoint());
        if (alreadyOnCanvas) {
            String message = context.getString(R.string.showing_definition);
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void validateDefinitionName(String defName) throws InvalidNameException {
        if (defName.length() > 8) {
            throw new InvalidNameException(R.string.bad_def_name_too_long);
        }

        for (char c : defName.toCharArray()) {
            if (Character.isLowerCase(c)) {
                throw new InvalidNameException(R.string.bad_def_name_bad_chars);
            }
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
            String message = context.getString(e.getStringRes());
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            return;
        }

        ScreenPoint screenPoint = pointConverter.toScreenPoint(newExpressionPoint());
        UserLambda expression = UserLambda.create(varName, null);
        canvasManager.createNewExpression(
                expression, screenPoint, false /* placeAbovePalette */);
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

    @Override
    public void promptDeleteDefinition() {
        View inputView = layoutInflater.inflate(R.layout.delete_definition_dialog, null);
        Spinner defNameSpinner = (Spinner) inputView.findViewById(R.id.definition_delete_selection);
        AlertDialog alertDialog = new Builder(context)
                .setTitle(R.string.choose_definition_to_delete)
                .setView(inputView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String selectedItem = (String) defNameSpinner.getSelectedItem();
                    canvasManager.deleteDefinitionIfExists(selectedItem);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        DeletePromptAdapter stringArrayAdapter = new DeletePromptAdapter();
        List<String> defNames = Ordering.natural().sortedCopy(appState.getAllDefinitions().keySet());
        stringArrayAdapter.addAll(defNames);
        defNameSpinner.setAdapter(stringArrayAdapter);
        alertDialog.show();
    }

    private class DeletePromptAdapter extends ArrayAdapter<String> {
        public DeletePromptAdapter() {
            super(context, R.layout.select_definition_item);
            add(context.getString(R.string.definition_prompt));
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (position == 0) {
                TextView textView = new TextView(context);
                textView.setHeight(0);
                textView.setVisibility(View.GONE);
                return textView;
            } else {
                // Disallow reusing views, since we don't want the special-case text view to be
                // reused.
                return super.getDropDownView(position, null, parent);
            }
        }
    }
}
