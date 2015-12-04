package com.feaoes.gesturesvg;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.feaoes.gesturesvg.widget.MyView;


public class MainActivity extends AppCompatActivity {

    private MyView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = (MyView) findViewById(R.id.vv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("start SVG draw");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this,"Click Option !",0).show();
        view.clickDrawPath();
        return super.onOptionsItemSelected(item);
    }
}
