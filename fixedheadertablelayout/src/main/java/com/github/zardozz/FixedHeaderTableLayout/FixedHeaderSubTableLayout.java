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

package com.github.zardozz.FixedHeaderTableLayout;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class FixedHeaderSubTableLayout extends LinearLayout {

    private static final String LOG_TAG = FixedHeaderSubTableLayout.class.getSimpleName();

    public FixedHeaderSubTableLayout(Context context) {
        super(context);
        init();
    }

    public FixedHeaderSubTableLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FixedHeaderSubTableLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FixedHeaderSubTableLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        // Log.d(LOG_TAG, "init: " + Integer.toHexString(System.identityHashCode(this)) );
        // Row are always vertical
        super.setOrientation(LinearLayout.VERTICAL);
        // set background to be white as transparent shows the main table panning underneath the others
        setBackgroundColor(Color.WHITE);
    }

    /**
     * Changing the Orientation of this class is not supported.
     * Tables are always vertical
     * @param orientation Ignored
     * @throws RuntimeException Throws exception
     */
    @Override
    public void setOrientation(int orientation) throws RuntimeException{
        throw new RuntimeException();
    }

    /**
     * Adds only FixedHeaderTableRow child views
     *
     * @param child the child view to add
     * @param index the position at which to add the child
     * @param params the layout parameters to set on the child
     * @throws RuntimeException Throws exception if no a FixedHeaderTableRow child
     */
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof FixedHeaderTableRow) {
            super.addView(child, index, params);
        } else {
            throw new UnsupportedOperationException("Adding non FixedHeaderTableRow children is not supported");
        }
    }

    /**
     * Measure the Table
     * The Table is always measure at (UNSPECIFIED) so the full table is drawn
     * so that Pan and Scale work
     * @param widthMeasureSpec Ignored
     * @param heightMeasureSpec Ignored
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Log.d(LOG_TAG, "onMeasure: " + Integer.toHexString(System.identityHashCode(this)) );
        // Always measure the tables to full size so pan and Scale works
        int measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        super.onMeasure(measureSpec, measureSpec);
    }
}
