package com.alangpierce.lambdacalculusplayground;

import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.expression.FuncCall;
import com.alangpierce.lambdacalculusplayground.expression.Lambda;
import com.alangpierce.lambdacalculusplayground.expression.Variable;
import com.google.common.collect.ImmutableList;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;


public class PlaygroundActivity extends ActionBarActivity {
    private static final String TAG = "PlaygroundActivity";

    static {
        RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
            @Override
            public void handleError(Throwable e) {
                Log.e(TAG, "Exception thrown from observable.", e);
            }
        });
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Thread had uncaught exception.", throwable);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        @SuppressLint("InflateParams") final View layoutView =
                getLayoutInflater().inflate(R.layout.activity_playground, null /* root */);
        setContentView(layoutView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.playground_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Expression yCombinatorSegment = Lambda.create("x", FuncCall.create(Variable.create("f"),
                    FuncCall.create(Variable.create("x"), Variable.create("x"))));
            Expression yCombinator = Lambda.create("f",
                    FuncCall.create(yCombinatorSegment, yCombinatorSegment));
            Expression trueExpr = Lambda.create("t", Lambda.create("f", Variable.create("t")));
            Expression idExpr = Lambda.create("x", Variable.create("x"));
            Expression simpleExecutableExpr = FuncCall.create(idExpr, Variable.create("y"));

            List<ScreenExpression> expressions = ImmutableList.of(
//                    ScreenExpression.create(
//                            UserExpressions.fromExpression(trueExpr), Point.create(200, 200)),
//                    ScreenExpression.create(
//                            UserExpressions.fromExpression(yCombinator),Point.create(100, 400)),
//                    ScreenExpression.create(new UserLambda("x", null), Point.create(150, 600)),
//                    ScreenExpression.create(
//                            UserExpressions.fromExpression(simpleExecutableExpr),
//                            Point.create(50, 50))
            );
            Fragment fragment = PlaygroundFragment.create(expressions);
            getFragmentManager().beginTransaction().add(R.id.playground_layout, fragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_playground, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_palette) {
            DrawerLayout drawerRoot = (DrawerLayout) findViewById(R.id.drawer_root_view);
            if (drawerRoot != null) {
                if (drawerRoot.isDrawerOpen(GravityCompat.END)) {
                    drawerRoot.closeDrawer(GravityCompat.END);
                } else {
                    drawerRoot.openDrawer(GravityCompat.END);
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
