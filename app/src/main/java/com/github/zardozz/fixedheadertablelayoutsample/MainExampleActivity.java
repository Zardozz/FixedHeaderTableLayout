/*
 *  MIT License
 *
 * Copyright (c) 2021 Andrew Beck
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.zardozz.fixedheadertablelayoutsample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.zardozz.FixedHeaderTableLayout.*;

import java.lang.ref.SoftReference;
import java.util.Locale;
import java.util.Random;

public class MainExampleActivity extends AppCompatActivity {

    private FixedHeaderTableLayout fixedHeaderTableLayout;
    private ProgressBar pgsBar;

    FixedHeaderSubTableLayout mainTable;
    FixedHeaderSubTableLayout columnHeaderTable;
    FixedHeaderSubTableLayout rowHeaderTable;
    FixedHeaderSubTableLayout cornerTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_example);
        fixedHeaderTableLayout = findViewById(R.id.FixedHeaderTableLayout);
        pgsBar = findViewById(R.id.pBar);

        // Really this should be done in the background as generating such a big layout takes time
        GenerateTables generateTables = new GenerateTables(this);
        generateTables.execute();


    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Redraw screen calculating the new boundaries without new pan or scale
            fixedHeaderTableLayout.calculatePanScale(0,0, 0, 0, 1f);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            // Redraw screen calculating the new boundaries without new pan or scale
            fixedHeaderTableLayout.calculatePanScale(0,0, 0, 0, 1f);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GenerateTables extends AsyncTask<Void, Integer, Void> {

        private final SoftReference<MainExampleActivity> activityReference;
        private final Context mContext;

        // only retain a soft reference to the activity
        GenerateTables(MainExampleActivity context) {
            mContext = context;
            activityReference = new SoftReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // get a reference to the activity if it is still there
            MainExampleActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            activity.pgsBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            createTable(mContext);
            return null;
        }


        @Override
        protected void onPostExecute(Void param) {

            // get a reference to the activity if it is still there
            MainExampleActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            // Setup FixHeader Table
            fixedHeaderTableLayout.addViews(mainTable, columnHeaderTable, rowHeaderTable, cornerTable);

            activity.pgsBar.setVisibility(View.GONE);
        }

    }


    private static final String ALLOWED_CHARACTERS ="qwertyuiopasdfghjklzxcvbnm";

    private static String getRandomString(int maxLength)
    {
        final Random random=new Random();
        final int sizeOfRandomString = random.nextInt(maxLength);
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    private void createTable(Context mContext){
        // Create our 4 Sub Tables
        mainTable = new FixedHeaderSubTableLayout(mContext);
        // 25 x 5 in size
        float textSize = 20.0f;
        for (int i = 1; i <=25; i++) {
            FixedHeaderTableRow tableRowData = new FixedHeaderTableRow(mContext);
            // Add some data
            for (int j = 1; j <= 25; j++) {
                // Add a Textview
                TextView textView = new TextView(mContext);
                textView.setGravity(Gravity.CENTER);
                textView.setText(String.format(Locale.ROOT,"C%d:R%d:%s", j, i, getRandomString(j)));
                textView.setBackgroundResource(R.drawable.list_border);
                textView.setPadding(5 ,5,5,5);
                textView.setTextSize(textSize * 1.5f);
                textView.setTextColor(getResources().getColor(R.color.colorText));
                textView.setOnClickListener(v -> {
                    if (v.isSelected()){
                        v.setSelected(false);
                        v.setBackgroundResource(R.drawable.list_border);
                    } else {
                        v.setSelected(true);
                        v.setBackgroundResource(R.drawable.selected_border);
                    }
                });
                tableRowData.addView(textView);
            }
            mainTable.addView(tableRowData);
        }

        columnHeaderTable = new FixedHeaderSubTableLayout(mContext);
        // 2 x 5 in size
        for (int i = 1; i <= 2; i++) {
            FixedHeaderTableRow tableRowData = new FixedHeaderTableRow(mContext);
            // Add some data
            for (int j = 1; j <= 25; j++) {
                // Add a Textview
                TextView textView = new TextView(mContext);
                textView.setGravity(Gravity.CENTER);
                textView.setText(String.format(Locale.ROOT,"C%d:%d", i, j));
                textView.setBackgroundResource(R.drawable.list_border);
                textView.setPadding(5 ,5,5,5);
                textView.setTextSize(textSize);
                textView.setTextColor(getResources().getColor(R.color.colorText));
                textView.setOnClickListener(v -> {
                    if (v.isSelected()){
                        v.setSelected(false);
                        v.setBackgroundResource(R.drawable.list_border);
                    } else {
                        v.setSelected(true);
                        v.setBackgroundResource(R.drawable.selected_border);
                    }
                });
                tableRowData.addView(textView);
            }
            columnHeaderTable.addView(tableRowData);
        }
        columnHeaderTable.setBackgroundResource(R.drawable.bottom_border);

        rowHeaderTable = new FixedHeaderSubTableLayout(mContext);
        // 25 x 1 in size
        for (int i = 1; i <= 25; i++) {
            FixedHeaderTableRow tableRowData = new FixedHeaderTableRow(mContext);
            // Add some data
            for (int j = 1; j <= 1; j++) {
                // Add a Textview
                TextView textView = new TextView(mContext);
                textView.setGravity(Gravity.CENTER);
                textView.setText(String.format(Locale.ROOT,"R%d", i));
                textView.setBackgroundResource(R.drawable.list_border);
                textView.setPadding(5 ,5,5,5);
                textView.setTextSize(textSize);
                textView.setTextColor(getResources().getColor(R.color.colorText));
                textView.setOnClickListener(v -> {
                    if (v.isSelected()){
                        v.setSelected(false);
                        v.setBackgroundResource(R.drawable.list_border);
                    } else {
                        v.setSelected(true);
                        v.setBackgroundResource(R.drawable.selected_border);
                    }
                });
                tableRowData.addView(textView);
            }
            rowHeaderTable.addView(tableRowData);
        }
        rowHeaderTable.setBackgroundResource(R.drawable.right_border);

        cornerTable = new FixedHeaderSubTableLayout(mContext);
        // 2 x 1 in size
        for (int i = 1; i <= 2; i++) {
            FixedHeaderTableRow tableRowData = new FixedHeaderTableRow(mContext);
            // Add some data
            for (int j = 1; j <= 1; j++) {
                // Add a Textview
                TextView textView = new TextView(mContext);
                textView.setGravity(Gravity.CENTER);
                textView.setText(String.format(Locale.ROOT,"A%d:%d",i , j));
                textView.setBackgroundResource(R.drawable.list_border);
                textView.setPadding(5 ,5,5,5);
                textView.setTextSize(textSize * 1.5f);
                textView.setTextColor(getResources().getColor(R.color.colorText));
                textView.setOnClickListener(v -> {
                    if (v.isSelected()){
                        v.setSelected(false);
                        v.setBackgroundResource(R.drawable.list_border);
                    } else {
                        v.setSelected(true);
                        v.setBackgroundResource(R.drawable.selected_border);
                    }
                });
                tableRowData.addView(textView);
            }
            cornerTable.addView(tableRowData);
        }
        cornerTable.setBackgroundResource(R.drawable.corner_border);

        fixedHeaderTableLayout.setMinScale(0.1f);
    }
}