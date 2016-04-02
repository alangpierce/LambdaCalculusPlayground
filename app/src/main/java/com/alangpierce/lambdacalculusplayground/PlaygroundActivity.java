package com.alangpierce.lambdacalculusplayground;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.playground_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            // TODO: Consider starting with some initial expressions.
            TopLevelExpressionState initialState = new TopLevelExpressionStateImpl();
            // TODO: This is just here for testing for now. Create a way to actually create these.
//            initialState.addScreenDefinition(
//                    ScreenDefinition.create("TRUE", null, CanvasPoint.create(200, 200)));

            Fragment fragment = PlaygroundFragment.create(initialState);
            getFragmentManager().beginTransaction().add(R.id.playground_layout, fragment).commit();
        }
    }
}
