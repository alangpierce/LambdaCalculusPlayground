package com.alangpierce.lambdacalculusplayground;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.common.collect.ImmutableList;

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
            Fragment fragment = PlaygroundFragment.create(ImmutableList.of());
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
