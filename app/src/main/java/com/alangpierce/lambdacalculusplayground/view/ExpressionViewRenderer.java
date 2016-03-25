package com.alangpierce.lambdacalculusplayground.view;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Utilities for creating native Android views expressions.
 *
 * This is Activity-aware, so this needs to be a real class instead of just a bunch of utility
 * functions.
 */
public interface ExpressionViewRenderer {
    LinearLayout makeLinearLayoutWithChildren(List<View> children);
    TextView makeTextView(String text);
    View makeBracketView(String text);
    LinearLayout styleLayout(final LinearLayout layout);
    LinearLayout makeMissingBodyView();

    /**
     * Create an execute button, although don't attach it to the root. We just need the root so that
     * the inflater can know what kind of LayoutParams to make.
     */
    View makeExecuteButton(RelativeLayout rootView);
}
