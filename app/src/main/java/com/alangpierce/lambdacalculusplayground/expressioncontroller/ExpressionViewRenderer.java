package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.widget.LinearLayout;

import javax.annotation.Nullable;

public interface ExpressionViewRenderer {
    LinearLayout makeVariableView(String varName);

    LinearLayout makeLambdaView(String varName, @Nullable LinearLayout body);

    LinearLayout makeFuncCallView(LinearLayout func, LinearLayout arg);
}
