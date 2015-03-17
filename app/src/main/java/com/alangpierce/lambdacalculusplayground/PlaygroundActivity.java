package com.alangpierce.lambdacalculusplayground;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.expression.FuncCall;
import com.alangpierce.lambdacalculusplayground.expression.Lambda;
import com.alangpierce.lambdacalculusplayground.expression.Variable;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpressions;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.google.common.collect.ImmutableList;

import java.util.List;

import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;


public class PlaygroundActivity extends ActionBarActivity {
    static {
        RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
            @Override
            public void handleError(Throwable e) {
                System.err.println("Exception thrown from observable.");
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        @SuppressLint("InflateParams") final View layoutView =
                getLayoutInflater().inflate(R.layout.activity_playground, null /* root */);

        if (savedInstanceState == null) {
            Expression yCombinatorSegment = new Lambda("x", new FuncCall(new Variable("f"),
                            new FuncCall(new Variable("x"), new Variable("x"))));
            Expression yCombinator = new Lambda("f",
                    new FuncCall(yCombinatorSegment, yCombinatorSegment));
            Expression trueExpr = new Lambda("t", new Lambda("f", new Variable("t")));
            Expression idExpr = new Lambda("x", new Variable("x"));
            Expression simpleExecutableExpr = new FuncCall(idExpr, new Variable("y"));

            List<ScreenExpression> expressions = ImmutableList.of(
                    ScreenExpression.create(
                            UserExpressions.fromExpression(trueExpr), Point.create(200, 200)),
                    ScreenExpression.create(
                            UserExpressions.fromExpression(yCombinator),Point.create(100, 400)),
                    ScreenExpression.create(new UserLambda("x", null), Point.create(150, 600)),
                    ScreenExpression.create(
                            UserExpressions.fromExpression(simpleExecutableExpr),
                            Point.create(50, 50)));
            Fragment fragment = PlaygroundFragment.create(expressions);
            getFragmentManager().beginTransaction().add(R.id.playground_layout, fragment).commit();
        }
        setContentView(layoutView);
    }
}
