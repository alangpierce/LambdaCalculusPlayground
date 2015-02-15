package com.alangpierce.lambdacalculusplayground;

import android.annotation.SuppressLint;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Arrays;


public class PlaygroundActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        @SuppressLint("InflateParams") View layoutView =
                getLayoutInflater().inflate(R.layout.activity_playground, null /* root */);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.playground_layout, ExpressionFragment.create(100, 300, Arrays.asList("foo")))
                    .commit();
            getFragmentManager().beginTransaction()
                    .add(R.id.playground_layout, ExpressionFragment.create(300, 100, Arrays.asList("bar")))
                    .commit();
        }

        setContentView(layoutView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playground, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
