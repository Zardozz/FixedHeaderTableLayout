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
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

import java.util.ArrayList;

public class FixedHeaderTableLayout extends FrameLayout implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener{
    private GestureDetectorCompat detector;

    private ScaleGestureDetector gestureScale;
    private float scaleFactor = 1;
    private float minScale = 0.5f;
    private float maxScale = 2.0f;

    private FixedHeaderSubTableLayout mainTable;
    private FixedHeaderSubTableLayout columnHeaderTable;
    private FixedHeaderSubTableLayout rowHeaderTable;
    private FixedHeaderSubTableLayout cornerTable;

    private final Matrix cornerMatrix = new Matrix();
    private final Matrix columnHeaderMatrix = new Matrix();
    private final Matrix rowHeaderMatrix = new Matrix();
    private final Matrix mainMatrix = new Matrix();

    private float panX = 0;
    private float panY = 0;

    private int rightBound;
    private int bottomBound;
    private float scaledRightBound;
    private float scaledBottomBound;

    private static final String LOG_TAG = FixedHeaderTableLayout.class.getSimpleName();


    public FixedHeaderTableLayout(Context context) {
        super(context);
        init(context);
    }

    public FixedHeaderTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FixedHeaderTableLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        Log.d(LOG_TAG, "mainTable:init");
        Log.d(LOG_TAG, "isHorizontalScrollBarEnabled: " + isHorizontalScrollBarEnabled());
        Log.d(LOG_TAG, "isVerticalScrollBarEnabled: " + isVerticalScrollBarEnabled());
        // As we extend a ViewGroup these won't draw anything by default
        // enable ViewGroup drawing so the scrollbars show
        setWillNotDraw(false);
        gestureScale = new ScaleGestureDetector(context, this);
        detector = new GestureDetectorCompat(context,this);
        this.setOnTouchListener(this);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setMinScale(float minScale){
        this.minScale = minScale;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public float getMinScale() {
        return minScale;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setMaxScale(float maxScale){
        this.maxScale = maxScale;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public float getMaxScale() {
        return maxScale;
    }

    /**
     * Add the four tables that make up the Layout
     *
     * @param mainTable the mainTable
     * @param columnHeaderTable the columnHeaderTable
     * @param rowHeaderTable the rowHeaderTable
     * @param cornerTable the cornerTable
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void addViews(FixedHeaderSubTableLayout mainTable, FixedHeaderSubTableLayout columnHeaderTable,
                         FixedHeaderSubTableLayout rowHeaderTable, FixedHeaderSubTableLayout cornerTable){

        // Store instances for later comparison;
        this.mainTable = mainTable;
        this.columnHeaderTable = columnHeaderTable;
        this.rowHeaderTable = rowHeaderTable;
        this.cornerTable = cornerTable;

        // Need to measure all Tables to full (UNSPECIFIED) size
        int measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        //Log.d(LOG_TAG, "mainTable:preMeasure");
        mainTable.measure(measureSpec, measureSpec);
        //Log.d(LOG_TAG, "columnHeaderTable:preMeasure");
        columnHeaderTable.measure(measureSpec, measureSpec);
        //Log.d(LOG_TAG, "rowHeaderTable:preMeasure");
        rowHeaderTable.measure(measureSpec, measureSpec);
        //Log.d(LOG_TAG, "cornerTable:preMeasure");
        cornerTable.measure(measureSpec, measureSpec);

        // Merge of the widths and height to align all the table rows
        // Get the max column width in mainTable and columnHeaderTable
        ArrayList<Integer> overallRightSideMaxColumnWidth = new ArrayList<>();
        overallRightSideMaxColumnWidth = calculateMaxColumnWidth(overallRightSideMaxColumnWidth, mainTable);
        overallRightSideMaxColumnWidth = calculateMaxColumnWidth(overallRightSideMaxColumnWidth, columnHeaderTable);
        //Log.d(LOG_TAG, "overallRightSideMaxColumnWidth:" + overallRightSideMaxColumnWidth);
        // Set the new max column width in mainTable and columnHeaderTable
        setMaxColumnWidth(overallRightSideMaxColumnWidth, mainTable);
        setMaxColumnWidth(overallRightSideMaxColumnWidth, columnHeaderTable);
        // Get the max column width in cornerTable and rowHeaderTable
        ArrayList<Integer> overallLeftSideMaxColumnWidth = new ArrayList<>();
        overallLeftSideMaxColumnWidth = calculateMaxColumnWidth(overallLeftSideMaxColumnWidth, rowHeaderTable);
        overallLeftSideMaxColumnWidth = calculateMaxColumnWidth(overallLeftSideMaxColumnWidth, cornerTable);
        //Log.d(LOG_TAG, "overallLeftSideMaxColumnWidth:" + overallLeftSideMaxColumnWidth);
        // Set the new max column width in mainTable and columnHeaderTable
        setMaxColumnWidth(overallLeftSideMaxColumnWidth, rowHeaderTable);
        setMaxColumnWidth(overallLeftSideMaxColumnWidth, cornerTable);

        // Get the max row height in mainTable and rowHeaderTable
        ArrayList<Integer> overallBottomSideMaxRowHeights = new ArrayList<>();
        overallBottomSideMaxRowHeights = calculateMaxRowHeight(overallBottomSideMaxRowHeights, mainTable);
        overallBottomSideMaxRowHeights = calculateMaxRowHeight(overallBottomSideMaxRowHeights, rowHeaderTable);
        //Log.d(LOG_TAG, "overallBottomSideMaxRowHeights:" + overallBottomSideMaxRowHeights);
        // Set the max row height in mainTable and rowHeaderTable
        setMaxRowHeight(overallBottomSideMaxRowHeights, mainTable);
        setMaxRowHeight(overallBottomSideMaxRowHeights, rowHeaderTable);

        // Get the max row height in columnHeaderTable and cornerTable
        ArrayList<Integer> overallTopSideMaxRowHeights = new ArrayList<>();
        overallTopSideMaxRowHeights = calculateMaxRowHeight(overallTopSideMaxRowHeights, columnHeaderTable);
        overallTopSideMaxRowHeights = calculateMaxRowHeight(overallTopSideMaxRowHeights, cornerTable);
        //Log.d(LOG_TAG, "overallTopSideMaxRowHeights:" + overallTopSideMaxRowHeights);
        // Set the max row height in mainTable and rowHeaderTable
        setMaxRowHeight(overallTopSideMaxRowHeights, columnHeaderTable);
        setMaxRowHeight(overallTopSideMaxRowHeights, cornerTable);


        // Remeasure Tables using the new set of aligned Heights and widths
        //Log.d(LOG_TAG, "mainTable:fixedMeasure");
        mainTable.measure(measureSpec, measureSpec);
        //Log.d(LOG_TAG, "columnHeaderTable:fixedMeasure");
        columnHeaderTable.measure(measureSpec, measureSpec);
        //Log.d(LOG_TAG, "rowHeaderTable:fixedMeasure");
        rowHeaderTable.measure(measureSpec, measureSpec);
        //Log.d(LOG_TAG, "cornerTable:fixedMeasure");
        cornerTable.measure(measureSpec, measureSpec);

        // mainTable margin is on the Top and Left to make space for the over views
        LayoutParams mainTableLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //Log.d(LOG_TAG, "mainTableLayoutParams:leftMargin:" + rowHeaderTable.getMeasuredWidth());
        //Log.d(LOG_TAG, "mainTableLayoutParams:topMargin:" + columnHeaderTable.getMeasuredHeight());
        mainTableLayoutParams.leftMargin = rowHeaderTable.getMeasuredWidth();
        mainTableLayoutParams.topMargin = columnHeaderTable.getMeasuredHeight();
        mainTable.setLayoutParams(mainTableLayoutParams);

        // columnHeaderTable margin is on the Left to make space for the over views
        LayoutParams columnHeaderTableLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //Log.d(LOG_TAG, "columnHeaderTableLayoutParams:leftMargin:" + cornerTable.getMeasuredWidth());
        columnHeaderTableLayoutParams.leftMargin = cornerTable.getMeasuredWidth();
        columnHeaderTable.setLayoutParams(columnHeaderTableLayoutParams);

        // rowHeaderTable margin is on the Top to make space for the over views
        LayoutParams rowHeaderTableLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //Log.d(LOG_TAG, "rowHeaderTableLayoutParams:topMargin:" + cornerTable.getMeasuredHeight());
        rowHeaderTableLayoutParams.topMargin = cornerTable.getMeasuredHeight();
        rowHeaderTable.setLayoutParams(rowHeaderTableLayoutParams);


        // Add the views
        addView(mainTable);
        addView(columnHeaderTable);
        addView(rowHeaderTable);
        addView(cornerTable);

        // Set Boundaries
        rightBound = cornerTable.getMeasuredWidth() + columnHeaderTable.getMeasuredWidth();
        bottomBound = cornerTable.getMeasuredHeight() + rowHeaderTable.getMeasuredHeight();
        //Log.d(LOG_TAG, "Bounds: = " + rightBound + " , " + bottomBound);
        scaledRightBound = rightBound * scaleFactor;
        scaledBottomBound = bottomBound * scaleFactor;
        //Log.d(LOG_TAG, "Scaled Bounds: = " + scaledRightBound + " , " + scaledBottomBound);
    }

    private ArrayList<Integer> calculateMaxRowHeight(ArrayList<Integer> existHeights, FixedHeaderSubTableLayout table) {
        for (int row = 0; row < table.getChildCount(); row++) {
            FixedHeaderTableRow tableRow = (FixedHeaderTableRow) table.getChildAt(row);
            if (existHeights.size() <= row){
                // Not seen this row number before so add
                existHeights.add(tableRow.getMaxChildHeight());
            } else {
                // Take the max of existing value and new value
                existHeights.set(row, Math.max(existHeights.get(row), tableRow.getMaxChildHeight()));
            }
        }
        return existHeights;
    }

    private void setMaxRowHeight(ArrayList<Integer> newHeights, FixedHeaderSubTableLayout table){
        for (int row = 0; row < table.getChildCount(); row++) {
            FixedHeaderTableRow tableRow = (FixedHeaderTableRow) table.getChildAt(row);
            tableRow.setMaxChildHeight(newHeights.get(row));
        }
    }

    private ArrayList<Integer> calculateMaxColumnWidth(ArrayList<Integer> existWidths, FixedHeaderSubTableLayout table) {
        for (int row = 0; row < table.getChildCount(); row++) {
            FixedHeaderTableRow tableRow = (FixedHeaderTableRow) table.getChildAt(row);
            ArrayList<Integer> rowColumnWidth = tableRow.getColumnWidths();
            for (int column = 0; column < rowColumnWidth.size(); column++) {
                if (existWidths.size() <= column) {
                    // Not seen this column number before so add
                    existWidths.add(rowColumnWidth.get(column));
                } else {
                    // Take the max of existing value and new value
                    existWidths.set(column, Math.max(existWidths.get(column), rowColumnWidth.get(column)));
                }
            }
        }
        return  existWidths;
    }

    private void setMaxColumnWidth(ArrayList<Integer> newWidths, FixedHeaderSubTableLayout table){
        for (int row = 0; row < table.getChildCount(); row++) {
            FixedHeaderTableRow tableRow = (FixedHeaderTableRow) table.getChildAt(row);
            tableRow.setColumnWidths(newWidths);
        }
    }

    /**
     * This method pans and scales the bitmaps of the converted TableLayout
     * @param distanceX X distance to pan the drawn TableLayout
     * @param distanceY Y distance to pan the drawn TableLayout
     * @param newScaleFactor new Factor to scale the drawn TableLayout
     */
    public void calculatePanScale(float distanceX, float distanceY, float newScaleFactor){
        //Log.d(LOG_TAG, "input = " + distanceX + ":" + distanceY + ":" + newScaleFactor);
        int width = getWidth();
        int height = getHeight();
        //Log.d(LOG_TAG, "view size = " + width + " x " + height);

        scaleFactor *= newScaleFactor;
        // Don't let the object get too small or too large.
        scaleFactor = Math.max(minScale, Math.min(scaleFactor, maxScale));
        //Log.d(LOG_TAG, "calculatePanScale: scale factor = " + scaleFactor);

        scaledRightBound = rightBound * scaleFactor;
        scaledBottomBound = bottomBound * scaleFactor;
        //Log.d(LOG_TAG, "calculatePanScale: scaledBounds " + scaledRightBound + ":" + scaledBottomBound);

        float maxPanX = -(scaledRightBound- width);
        float maxPanY = -(scaledBottomBound- height);
        //Log.d(LOG_TAG, "calculatePanScale: maxPan " + maxPanX + ":" + maxPanY);

        panX = Math.min(0, Math.max(maxPanX,(panX - distanceX)));
        panY = Math.min(0, Math.max(maxPanY,(panY - distanceY)));
        //Log.d(LOG_TAG, "calculatePanScale: Pan " + panX + ":" + panY);

        mainMatrix.setScale(scaleFactor, scaleFactor);
        mainMatrix.postTranslate(panX, panY);
        columnHeaderMatrix.setScale(scaleFactor, scaleFactor);
        columnHeaderMatrix.postTranslate(panX, 0);
        rowHeaderMatrix.setScale(scaleFactor, scaleFactor);
        rowHeaderMatrix.postTranslate(0, panY);
        cornerMatrix.setScale(scaleFactor, scaleFactor);

        invalidate();
    }

    // We don't allow adding Views directly use addViews instead
    // So unless we have stored the instance in addViews method don't allow add.
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child == mainTable || child == columnHeaderTable || child == rowHeaderTable || child == cornerTable) {
            super.addView(child, index, params);
        } else {
            throw new UnsupportedOperationException("Adding children directly is not supported, use addViews method");
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean result;
        int save = canvas.save();
        //Log.d(LOG_TAG, "drawChild:" + Integer.toHexString(System.identityHashCode(child)));
        if (child == mainTable) {
            //Log.d(LOG_TAG, "drawChild:mainTable");
            canvas.concat(mainMatrix);
        } else if (child == columnHeaderTable) {
            //Log.d(LOG_TAG, "drawChild:columnHeaderTable");
            canvas.concat(columnHeaderMatrix);
        } else if (child == rowHeaderTable) {
            //Log.d(LOG_TAG, "drawChild:rowHeaderTable");
            canvas.concat(rowHeaderMatrix);
        } else if (child == cornerTable) {
            //Log.d(LOG_TAG, "drawChild:cornerTable");
            canvas.concat(cornerMatrix);
        }

        result = super.drawChild(canvas, child, drawingTime);
        canvas.restoreToCount(save);
        return result;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        // Log.d(LOG_TAG, "onTouch");
        gestureScale.onTouchEvent(event);
        detector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Log.d(LOG_TAG, "onScale");
        // Don't change the pan just scale
        calculatePanScale(0,0 ,detector.getScaleFactor());
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        //Log.d(LOG_TAG, "scale begin");
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        //Log.d(LOG_TAG, "scale end");
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //Log.d(LOG_TAG, "onScroll");
        // Don't change the scale just Pan
        calculatePanScale(distanceX, distanceY , 1);
        return true;

    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    // Length of scrollbar track
    @Override
    protected int computeHorizontalScrollRange() {
        return (int) scaledRightBound;
    }

    // Position from thumb from the left of view
    @Override
    protected int computeHorizontalScrollOffset() {
        return (int) -panX;
    }

    @Override
    protected int computeVerticalScrollRange() {
        return (int) scaledBottomBound;
    }

    @Override
    protected int computeVerticalScrollOffset() {
        return (int) -panY;
    }
}
