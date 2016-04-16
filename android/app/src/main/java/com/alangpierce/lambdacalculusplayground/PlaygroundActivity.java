package com.alangpierce.lambdacalculusplayground;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.common.logging.FLog;
import com.facebook.react.common.ReactConstants;

import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;


public class PlaygroundActivity extends AppCompatActivity {
    private static final String TAG = "PlaygroundActivity";

    private static final String REDBOX_PERMISSION_MESSAGE =
            "Overlay permissions needs to be granted in order for react native apps to run in dev mode";

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

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= 23) {
            // Get permission to show redbox in dev builds.
            if (!Settings.canDrawOverlays(this)) {
                Intent serviceIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(serviceIntent);
                FLog.w(ReactConstants.TAG, REDBOX_PERMISSION_MESSAGE);
                Toast.makeText(this, REDBOX_PERMISSION_MESSAGE, Toast.LENGTH_LONG).show();
            }
        }
    }
}
