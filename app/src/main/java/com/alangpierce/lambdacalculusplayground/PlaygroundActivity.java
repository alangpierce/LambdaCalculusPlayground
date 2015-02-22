package com.alangpierce.lambdacalculusplayground;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.expression.FuncCall;
import com.alangpierce.lambdacalculusplayground.expression.Lambda;
import com.alangpierce.lambdacalculusplayground.expression.Variable;
import com.google.common.collect.ImmutableList;

import java.util.List;


public class PlaygroundActivity extends ActionBarActivity {
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

            List<ScreenExpression> expressions = ImmutableList.of(
                    new ScreenExpression(
                            new Lambda("t", new Lambda("f", new Variable("t"))), 200, 200),
                    new ScreenExpression(yCombinator, 100, 500));
            Fragment fragment = PlaygroundFragment.create(expressions);
            getFragmentManager().beginTransaction().add(R.id.playground_layout, fragment).commit();
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
