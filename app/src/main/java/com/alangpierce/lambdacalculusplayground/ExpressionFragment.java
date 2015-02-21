package com.alangpierce.lambdacalculusplayground;


import android.animation.LayoutTransition;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * Fragment for an expression on the screen. This may be a self-contained expression, but can really
 * any lambda calculus expression, even one with free variables.
 */
public class ExpressionFragment extends Fragment {
    public ExpressionFragment() {
        // Required empty public constructor
    }

    private LinearLayout layoutView;

    private int xPos;
    private int yPos;
    private List<String> tokens;

    public static ExpressionFragment create(int xPos, int yPos, List<String> tokens) {
        Bundle args = new Bundle();
        args.putInt("xPos", xPos);
        args.putInt("yPos", yPos);
        args.putStringArrayList("tokens", new ArrayList<>(tokens));
        ExpressionFragment result = new ExpressionFragment();
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
        }
        if (bundle != null) {
            xPos = bundle.getInt("xPos");
            yPos = bundle.getInt("yPos");
            tokens = bundle.getStringArrayList("tokens");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("xPos", xPos);
        outState.putInt("yPos", yPos);
        outState.putStringArrayList("tokens", new ArrayList<>(tokens));
    }

    private TextView makeTextView(String text) {
        TextView textView = new TextView(getActivity());
        textView.setText(text);
        textView.setTextSize(30);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        textView.setPadding(35, 20, 35, 20);
        return textView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layoutView = new LinearLayout(getActivity());
        // We should stretch to fit our content, but our position should be configurable.
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = xPos;
        layoutParams.topMargin = yPos;
        layoutView.setLayoutParams(layoutParams);

//        layoutView.setBackgroundResource(R.drawable.expression);
        layoutView.setBackgroundColor(Color.WHITE);
        layoutView.setElevation(40);

        for (int i = 0; i < tokens.size(); i++) {
            final String token = tokens.get(i);
            TextView textView = makeTextView(token);
            if (i == 0) {
                attachExpressionDragHandle(textView);
            }
            if (i > 0 && tokens.get(i - 1).equals("λ")) {
                attachVariableCreator(token, textView);
            }
            layoutView.addView(textView);
        }
        layoutView.setLayoutTransition(new LayoutTransition());
        layoutView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                ExpressionFragment sourceFragment = (ExpressionFragment)event.getLocalState();
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        System.out.printf("View with tokens %s received event DRAG_STARTED%n", tokens);
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        System.out.printf("View with tokens %s received event DRAG_LOCATION%n", tokens);
                        break;
                    case DragEvent.ACTION_DROP:
                        System.out.printf("View with tokens %s received event DROP%n", tokens);
//                        layoutView.setBackgroundColor(Color.GREEN);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        System.out.printf("View with tokens %s received event DRAG_ENDED%n", tokens);
//                        layoutView.setBackgroundColor(Color.GREEN);
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        System.out.printf("View with tokens %s received event DRAG_ENTERED%n", tokens);
//                        layoutView.setBackgroundColor(Color.RED);
//                        sourceFragment.setVisible(false);
                        layoutView.addView(makeTextView("foo"), 1);
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        System.out.printf("View with tokens %s received event DRAG_EXITED%n", tokens);
//                        layoutView.setBackgroundColor(Color.GREEN);
//                        sourceFragment.setVisible(true);
                        layoutView.removeViewAt(1);
                        break;
                }
                return true;
            }
        });

        layoutView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        System.out.printf("Tokens: %s, move action%n", tokens);
                }
                return true;
            }
        });


        return layoutView;
    }

    /**
     * Set the handle as a way to drag the expression itself.
     */
    private void attachExpressionDragHandle(View handle) {
        ViewDragger.attachDragListenerToView(handle, this, layoutView, new ViewDragger.OnDragListener() {
            @Override
            public void onDrag(int dx, int dy) {
                translatePosition(dx, dy);
            }
        });
    }

    /**
     * Set the given handle as a "factory" to create new expressions with the given token.
     */
    private void attachVariableCreator(final String token, final View handle) {
        ViewDragger.attachStartListenerToView(handle, new ViewDragger.OnStartDragListener() {
            @Override
            public @Nullable ViewDragger.OnDragListener onStartDrag() {
                // Find the variable's position relative to the playground view.
                int variableX = xPos + handle.getLeft();
                int variableY = yPos + handle.getTop();

                final ExpressionFragment variableExpression =
                        ExpressionFragment.create(
                                variableX, variableY, ImmutableList.of(token));

                // TODO(alan): Don't assume the parent is the playground_layout. Maybe inject a
                // sibling factory or something.
                getFragmentManager().beginTransaction()
                        .add(R.id.playground_layout, variableExpression).commit();

                return new ViewDragger.OnDragListener() {
                    @Override
                    public void onDrag(int dx, int dy) {
                        variableExpression.translatePosition(dx, dy);
                    }
                };
            }
        });
    }

    public void setPosition(int x, int y) {
        xPos = x;
        yPos = y;
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams)layoutView.getLayoutParams();
        layoutParams.leftMargin = xPos;
        layoutParams.topMargin = yPos;
        layoutView.setLayoutParams(layoutParams);
    }

    /**
     * Update the stored position and redraw the view in the outer layout.
     */
    public void translatePosition(int dx, int dy) {
        xPos += dx;
        yPos += dy;
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams)layoutView.getLayoutParams();
        layoutParams.leftMargin = xPos;
        layoutParams.topMargin = yPos;
        layoutView.setLayoutParams(layoutParams);
    }

    public void setVisible(boolean visible) {
        if (visible) {
            layoutView.setVisibility(View.VISIBLE);
        } else {
            layoutView.setVisibility(View.GONE);
        }
    }
}
