package com.alangpierce.lambdacalculusplayground;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
        if (savedInstanceState == null) {
            AppState initialState = new AppStateImpl();
            // TODO: When we move minSdk to API 17, we can probably switch to full fragments. For
            // now, we need to use the support library fragment since it handles
            // onViewStateRestored correctly.
            Fragment fragment = PlaygroundFragment.create(initialState);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.playground_layout, fragment)
                    .commit();
        }
    }
}
