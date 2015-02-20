package com.bingo.fibonacci.bingo;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    private boolean[] table = new boolean[25];
   private Context context;
    private boolean first_bingo = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));
        context = getApplicationContext();

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
                if (table[position]) {
                    ((ImageView) v).clearColorFilter();
                    table[position] = false;
                } else {
                    ((ImageView) v).setColorFilter(0xffa40000, PorterDuff.Mode.OVERLAY);
                    table[position] = true;
                    if (isBingo() && first_bingo) {
                        Toast.makeText(MainActivity.this, "BINGO!", Toast.LENGTH_SHORT).show();
                        first_bingo = false;
                    }
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_newgame) {
            startActivity(new Intent(getBaseContext(), MainActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isBingo() {
        boolean win = false;
        for (int i = 0; i <= 20; i = i + 5) {
            win |= table[i] && table[1 + i] && table[2 + i] && table[3 + i] && table[4 + i];
            if (win)
                return win;

        }
        for (int i = 0; i < 5; i++) {
            win |= table[i] && table[5 + i] && table[10 + i] && table[15 + i] && table[20 + i];
            if (win)
                return win;

        }
        win |= table[0] && table[6] && table[12] && table[18] && table[24];
        win |= table[4] && table[8] && table[12] && table[16] && table[20];
        return win;
    }
}