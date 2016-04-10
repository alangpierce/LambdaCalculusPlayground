package com.alangpierce.lambdacalculusplayground;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpressionParser;
import com.alangpierce.lambdacalculusplayground.userexpression.UserFuncCall;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.alangpierce.lambdacalculusplayground.userexpression.UserVariable;

import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;


public class PlaygroundActivity extends AppCompatActivity {
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
        if (savedInstanceState == null) {
            AppState initialState = new AppStateImpl();
            populateInitialDefinitions(initialState);
            Fragment fragment = PlaygroundFragment.create(initialState);
            getFragmentManager().beginTransaction().add(R.id.playground_layout, fragment).commit();
        }
    }

    private void populateInitialDefinitions(AppState state) {
        state.setDefinition("+", UserExpressionParser.parse("L n[L m[L s[L z[n(s)(m(s)(z))]]]]"));
        state.setDefinition("TRUE", UserExpressionParser.parse("L t[L f[t]]"));
        state.setDefinition("FALSE", UserExpressionParser.parse("L t[L f[f]]"));

        for (int i = 0; i < 3; i++) {
            UserExpression body = UserVariable.create("z");
            for (int j = 0; j < i; j++) {
                body = UserFuncCall.create(UserVariable.create("s"), body);
            }
            state.setDefinition(
                    Integer.toString(i),
                    UserLambda.create(
                            "s",
                            UserLambda.create(
                                    "z",
                                    body
                            )
                    )
            );
        }
    }
}
