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

package com.github.zardozz.FixedHeaderTableLayout.test;

import com.github.zardozz.FixedHeaderTableLayout.*;

import android.content.Context;
import android.graphics.Point;
import android.view.Gravity;
import android.widget.TextView;

import java.util.Locale;

public class Helpers {
    public static FixedHeaderSubTableLayout[] createSubTables(Context mContext, Point mainTableShape, Point columnHeaderTableShape, Point rowHeaderTableShape, Point cornerTableShape){
        FixedHeaderSubTableLayout[] subTableLayouts = new FixedHeaderSubTableLayout[4];

        // Create our 4 Sub Tables
        subTableLayouts[0] = new FixedHeaderSubTableLayout(mContext);
        // 25 x 5 in size
        float textSize = 20.0f;
        for (int i = 1; i <= mainTableShape.y; i++) {
            FixedHeaderTableRow tableRowData = new FixedHeaderTableRow(mContext);
            // Add some data
            for (int j = 1; j <= mainTableShape.x; j++) {
                // Add a Textview
                TextView textView = new TextView(mContext);
                textView.setGravity(Gravity.CENTER);
                textView.setText(String.format(Locale.ROOT,"D%d:%d", j, i));
                textView.setPadding(5 ,5,5,5);
                textView.setTextSize(textSize * 1.5f);
                tableRowData.addView(textView);
            }
            subTableLayouts[0].addView(tableRowData);
        }

        subTableLayouts[1] = new FixedHeaderSubTableLayout(mContext);
        // 2 x 5 in size
        for (int i = 1; i <= columnHeaderTableShape.y; i++) {
            FixedHeaderTableRow tableRowData = new FixedHeaderTableRow(mContext);
            // Add some data
            for (int j = 1; j <= columnHeaderTableShape.x; j++) {
                // Add a Textview
                TextView textView = new TextView(mContext);
                textView.setGravity(Gravity.CENTER);
                textView.setText(String.format(Locale.ROOT,"B%d:%d", i, j));
                textView.setPadding(5 ,5,5,5);
                textView.setTextSize(textSize);
                tableRowData.addView(textView);
            }
            subTableLayouts[1].addView(tableRowData);
        }

        subTableLayouts[2] = new FixedHeaderSubTableLayout(mContext);
        // 25 x 1 in size
        for (int i = 1; i <= rowHeaderTableShape.y; i++) {
            FixedHeaderTableRow tableRowData = new FixedHeaderTableRow(mContext);
            // Add some data
            for (int j = 1; j <= rowHeaderTableShape.x; j++) {
                // Add a Textview
                TextView textView = new TextView(mContext);
                textView.setGravity(Gravity.CENTER);
                textView.setText(String.format(Locale.ROOT,"C%d:%d", i, j));
                textView.setPadding(5 ,5,5,5);
                textView.setTextSize(textSize);
                tableRowData.addView(textView);
            }
            subTableLayouts[2].addView(tableRowData);
        }

        subTableLayouts[3] = new FixedHeaderSubTableLayout(mContext);
        // 2 x 1 in size
        for (int i = 1; i <= cornerTableShape.y; i++) {
            FixedHeaderTableRow tableRowData = new FixedHeaderTableRow(mContext);
            // Add some data
            for (int j = 1; j <= cornerTableShape.x; j++) {
                // Add a Textview
                TextView textView = new TextView(mContext);
                textView.setGravity(Gravity.CENTER);
                textView.setText(String.format(Locale.ROOT,"A%d:%d",i , j));
                textView.setPadding(5 ,5,5,5);
                textView.setTextSize(textSize * 1.5f);
                tableRowData.addView(textView);
            }
            subTableLayouts[3].addView(tableRowData);
        }
        return subTableLayouts;
    }
}
