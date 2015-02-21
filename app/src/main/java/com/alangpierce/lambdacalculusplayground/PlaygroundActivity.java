package com.alangpierce.lambdacalculusplayground;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Arrays;


public class PlaygroundActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        @SuppressLint("InflateParams") final View layoutView =
                getLayoutInflater().inflate(R.layout.activity_playground, null /* root */);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.playground_layout, ExpressionFragment.create(100, 300,
                            Arrays.asList("λ", "x", "x")))
                    .commit();
            getFragmentManager().beginTransaction()
                    .add(R.id.playground_layout, ExpressionFragment.create(300, 100,
                            Arrays.asList("λ", "t", "λ", "f", "t")))
                    .commit();
            getFragmentManager().beginTransaction()
                    .add(R.id.playground_layout, ExpressionFragment.create(200, 200,
                            Arrays.asList("λ", "t")))
                    .commit();
            getFragmentManager().beginTransaction()
                    .add(R.id.playground_layout, ExpressionFragment.create(300, 600,
                            Arrays.asList("λ", "t", "λ", "f", "f")))
                    .commit();
        }

        layoutView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                ExpressionFragment sourceFragment = (ExpressionFragment)event.getLocalState();
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        System.out.printf("Root view received event DRAG_STARTED%n");
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        System.out.printf("Root view received event DRAG_LOCATION%n");
                        break;
                    case DragEvent.ACTION_DROP:
                        System.out.printf("Root view received event DROP%n");
                        System.out.printf("x: %s, y: %s%n", event.getX(), event.getY());
                        sourceFragment.setPosition((int)event.getX(), (int)event.getY());
                        sourceFragment.setVisible(true);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        System.out.printf("Root view received event DRAG_ENDED%n");
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        System.out.printf("Root view received event DRAG_ENTERED%n");
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        System.out.printf("Root view received event DRAG_EXITED%n");
                        break;
                }
                return true;
            }
        });

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
