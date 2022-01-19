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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class FixedHeaderTableLayout extends FrameLayout implements ScaleGestureDetector.OnScaleGestureListener{

    private ScaleGestureDetector gestureScale;
    private float scaleFactor = 1;
    private float minScale = 0.5f;
    private float maxScale = 2.0f;

    private float mLastTouchX;
    private float mLastTouchY;
    private float mFirstTouchX;
    private float mFirstTouchY;
    private static final int INVALID_POINTER_ID = -1;

    // The ‘active pointer’ is the one currently moving our object.
    private int mActivePointerId = INVALID_POINTER_ID;

    private int mTouchSlop;
    private boolean isScrolling = false;

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
        initialDefaultValues(null);
        init(context);
    }

    public FixedHeaderTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialDefaultValues(attrs);
        init(context);
    }

    public FixedHeaderTableLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialDefaultValues(null);
        init(context);
    }

    private void initialDefaultValues(@Nullable AttributeSet attrs) {
        if (attrs == null) {
            // That means the class is created programmatically.
            return;
        }

        // Get values from xml attributes
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable
                .FixedHeaderTableLayout, 0, 0);

        try {
            minScale = a.getFloat(R.styleable.FixedHeaderTableLayout_fhtl_min_scale, minScale);
            maxScale = a.getFloat(R.styleable.FixedHeaderTableLayout_fhtl_max_scale, maxScale);
        } finally {
            a.recycle();
        }
    }

    private void init(Context context){
       //Log.d(LOG_TAG, "mainTable:init");

        // Get our current View slop for Scrolling
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        //Log.d(LOG_TAG, "mTouchSlop:" + mTouchSlop);

        // As we extend a ViewGroup these won't draw anything by default
        // enable ViewGroup drawing so the scrollbars show
        setWillNotDraw(false);
        gestureScale = new ScaleGestureDetector(context, this);
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

        // Set some View Id's if not already set to help with identification
        if (mainTable.getId() == NO_ID) {
            mainTable.setId(R.id.MainTable);
        }
        if (columnHeaderTable.getId() == NO_ID) {
            columnHeaderTable.setId(R.id.ColumnHeaderTable);
        }
        if (rowHeaderTable.getId() == NO_ID) {
            rowHeaderTable.setId(R.id.RowHeaderTable);
        }
        if (cornerTable.getId() == NO_ID) {
            cornerTable.setId(R.id.CornerTable);
        }

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
        overallRightSideMaxColumnWidth = Utils.calculateMaxColumnWidth(overallRightSideMaxColumnWidth, mainTable);
        overallRightSideMaxColumnWidth = Utils.calculateMaxColumnWidth(overallRightSideMaxColumnWidth, columnHeaderTable);
        //Log.d(LOG_TAG, "overallRightSideMaxColumnWidth:" + overallRightSideMaxColumnWidth);
        // Set the new max column width in mainTable and columnHeaderTable
        Utils.setMaxColumnWidth(overallRightSideMaxColumnWidth, mainTable);
        Utils.setMaxColumnWidth(overallRightSideMaxColumnWidth, columnHeaderTable);
        // Get the max column width in cornerTable and rowHeaderTable
        ArrayList<Integer> overallLeftSideMaxColumnWidth = new ArrayList<>();
        overallLeftSideMaxColumnWidth = Utils.calculateMaxColumnWidth(overallLeftSideMaxColumnWidth, rowHeaderTable);
        overallLeftSideMaxColumnWidth = Utils.calculateMaxColumnWidth(overallLeftSideMaxColumnWidth, cornerTable);
        //Log.d(LOG_TAG, "overallLeftSideMaxColumnWidth:" + overallLeftSideMaxColumnWidth);
        // Set the new max column width in mainTable and columnHeaderTable
        Utils.setMaxColumnWidth(overallLeftSideMaxColumnWidth, rowHeaderTable);
        Utils.setMaxColumnWidth(overallLeftSideMaxColumnWidth, cornerTable);

        // Get the max row height in mainTable and rowHeaderTable
        ArrayList<Integer> overallBottomSideMaxRowHeights = new ArrayList<>();
        overallBottomSideMaxRowHeights = Utils.calculateMaxRowHeight(overallBottomSideMaxRowHeights, mainTable);
        overallBottomSideMaxRowHeights = Utils.calculateMaxRowHeight(overallBottomSideMaxRowHeights, rowHeaderTable);
        //Log.d(LOG_TAG, "overallBottomSideMaxRowHeights:" + overallBottomSideMaxRowHeights);
        // Set the max row height in mainTable and rowHeaderTable
        Utils.setMaxRowHeight(overallBottomSideMaxRowHeights, mainTable);
        Utils.setMaxRowHeight(overallBottomSideMaxRowHeights, rowHeaderTable);

        // Get the max row height in columnHeaderTable and cornerTable
        ArrayList<Integer> overallTopSideMaxRowHeights = new ArrayList<>();
        overallTopSideMaxRowHeights = Utils.calculateMaxRowHeight(overallTopSideMaxRowHeights, columnHeaderTable);
        overallTopSideMaxRowHeights = Utils.calculateMaxRowHeight(overallTopSideMaxRowHeights, cornerTable);
        //Log.d(LOG_TAG, "overallTopSideMaxRowHeights:" + overallTopSideMaxRowHeights);
        // Set the max row height in mainTable and rowHeaderTable
        Utils.setMaxRowHeight(overallTopSideMaxRowHeights, columnHeaderTable);
        Utils.setMaxRowHeight(overallTopSideMaxRowHeights, cornerTable);


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



    /**
     * This method pans and scales the bitmaps of the converted TableLayout
     * @param distanceX X distance to pan the drawn TableLayout
     * @param distanceY Y distance to pan the drawn TableLayout
     * @param centerX X center of scale
     * @param centerY Y center of scale
     * @param newScaleFactor new Factor to scale the drawn TableLayout
     */
    public void calculatePanScale(float distanceX, float distanceY, float centerX, float centerY, float newScaleFactor){
        Log.d(LOG_TAG, "input = " + distanceX + ":" + distanceY + ":" + centerX + ":" + centerY + ":" + newScaleFactor);
        Log.d(LOG_TAG, "existing = " + panX + ":" + panY + ":" + scaleFactor);
        int width = getWidth();
        int height = getHeight();
        //Log.d(LOG_TAG, "view size = " + width + " x " + height);

        // Map the center point from drawn location to laid out location
        // which is the inverse of the laid out location to drawn location matrix
        Matrix oldMatrix = new Matrix();
        mainMatrix.invert(oldMatrix);

        float[] mappedCenter = new float[2];
        mappedCenter[0] = centerX;
        mappedCenter[1] = centerY;
        oldMatrix.mapPoints(mappedCenter);
        Log.d(LOG_TAG, "mappedCenter = " + mappedCenter[0] + ":" + mappedCenter[1]);


        scaleFactor *= newScaleFactor;
        // Don't let the object get too small or too large.
        scaleFactor = Math.max(minScale, Math.min(scaleFactor, maxScale));
        Log.d(LOG_TAG, "calculatePanScale: scale factor = " + scaleFactor);

        mainMatrix.setScale(scaleFactor, scaleFactor);
        columnHeaderMatrix.setScale(scaleFactor, scaleFactor);
        rowHeaderMatrix.setScale(scaleFactor, scaleFactor);
        cornerMatrix.setScale(scaleFactor, scaleFactor);

        if (scaleFactor < maxScale && scaleFactor > minScale  && newScaleFactor != 1.0f) {

            // Map the mappedCenter to the new drawn location using the updated mainMatrix
            float[] centerPoint = new float[2];
            centerPoint[0] = mappedCenter[0] * scaleFactor;
            centerPoint[1] = mappedCenter[1] * scaleFactor;
            //mainMatrix.mapPoints(centerPoint);
            //Log.d(LOG_TAG, "centerPoint = " + centerPoint[0] + ":" + centerPoint[1]);

            float adjustDiffX = (centerPoint[0] - mappedCenter[0]);
            float adjustDiffY = (centerPoint[1] - mappedCenter[1]);
            //Log.d(LOG_TAG, "adjustDiff = " + adjustDiffX + "," + adjustDiffY);

            distanceX = distanceX + (adjustDiffX * scaleFactor);
            distanceY = distanceY + (adjustDiffY * scaleFactor);
            //Log.d(LOG_TAG, "adjustDistance = " + distanceX + "," + distanceY);
        }

        scaledRightBound = rightBound * scaleFactor;
        scaledBottomBound = bottomBound * scaleFactor;
        //Log.d(LOG_TAG, "calculatePanScale: scaledBounds " + scaledRightBound + ":" + scaledBottomBound);

        float maxPanX = -(scaledRightBound- width);
        float maxPanY = -(scaledBottomBound- height);
        //Log.d(LOG_TAG, "calculatePanScale: maxPan " + maxPanX + ":" + maxPanY);

        panX = Math.min(0, Math.max(maxPanX,(panX - distanceX)));
        panY = Math.min(0, Math.max(maxPanY,(panY - distanceY)));
        Log.d(LOG_TAG, "calculatePanScale: Pan " + panX + ":" + panY);

        float scaledPanX = panX * scaleFactor;
        float scaledPanY = panY * scaleFactor;


        mainMatrix.postTranslate(panX, panY);
        columnHeaderMatrix.postTranslate(panX, 0);
        rowHeaderMatrix.postTranslate(0, panY);


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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        /* Work out if this ViewGroup needs the event to scroll/scale
         *  This has to be done here instead of onInterceptTouchEvent
         * as all other events need to be mapped and must not be disabled by any child
         */
        //Log.d(LOG_TAG, "dispatchTouchEvent = " + ev.getX() + ":" + ev.getY());

        // Let the ScaleGestureDetector inspect all events
        gestureScale.onTouchEvent(ev);

        if (gestureScale.isInProgress()) {
            // Need to cancel anything we have sent to the children
            MotionEvent transformEvent = MotionEvent.obtain(ev);
            transformEvent.setAction(MotionEvent.ACTION_CANCEL);
            super.dispatchTouchEvent(transformEvent);
            transformEvent.recycle();
            return true;
        }


        final int action = (ev.getAction() & MotionEvent.ACTION_MASK);

        switch(action) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();

                mLastTouchX = x;
                mLastTouchY = y;
                mFirstTouchX = x;
                mFirstTouchY = y;
                mActivePointerId = ev.getPointerId(0);

                // Need to send this to our children but mapped for scale and pan;
                //Log.d(LOG_TAG, "dispatchTouchEvent Down Action");
                MotionEvent event = mapMotionEvent(ev);
                super.dispatchTouchEvent(event);
                event.recycle();
                break;
            }

            case MotionEvent.ACTION_MOVE: {

                //Log.d(LOG_TAG, "dispatchTouchEvent Move Action");

                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!gestureScale.isInProgress()) {

                    // Have we moved enough to Scroll?
                    final float absx = Math.abs(x - mFirstTouchX);
                    final float absy = Math.abs(y - mFirstTouchY);
                    //Log.d(LOG_TAG, "dispatchTouchEvent absx:absy = " + absx + ":" + absy);
                    if (!isScrolling && (absx > mTouchSlop || absy > mTouchSlop)) {
                        isScrolling = true;
                    }

                    final float dx = mLastTouchX - x;
                    final float dy = mLastTouchY - y;

                    if (isScrolling) {
                        //Log.d(LOG_TAG, "dispatchTouchEvent scrolling = " + dx + ":" + dy);
                        awakenScrollBars();
                        calculatePanScale(dx, dy, 0, 0, 1);
                        
                        // Need to cancel anything we have sent to the children
                        MotionEvent transformEvent = MotionEvent.obtain(ev);
                        transformEvent.setAction(MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(transformEvent);
                        transformEvent.recycle();
                    }
                } else {
                    // Doing scale so
                    // Need to cancel anything we have sent to the children
                    MotionEvent transformEvent = MotionEvent.obtain(ev);
                    transformEvent.setAction(MotionEvent.ACTION_CANCEL);
                    super.dispatchTouchEvent(transformEvent);
                    transformEvent.recycle();
                }

                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                    //Log.d(LOG_TAG, "dispatchTouchEvent Cancel Action");
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                isScrolling = false;
                // Need to send this to our children but mapped for scale and pan;
                //Log.d(LOG_TAG, "dispatchTouchEvent Cancel Action");
                MotionEvent event = mapMotionEvent(ev);
                super.dispatchTouchEvent(event);
                event.recycle();
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {

                //Log.d(LOG_TAG, "dispatchTouchEvent Pointer Up Action");
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }

        return true;
    }

    private MotionEvent mapMotionEvent(MotionEvent ev){
        Matrix mappingMatrix = new Matrix();

        // May be we have not generated the cornerTable Yet so nothing to map event with
        if (cornerTable == null || cornerTable.getWidth() == 0 || cornerTable.getHeight() == 0) {
            // Return the original transformed new event
            return MotionEvent.obtain(ev);
        }

        // Work out which matrix to use to map the click
        // Find the corner point
        float[] cornerPoint = new float[2];
        cornerPoint[0] = cornerTable.getWidth();
        cornerPoint[1] = cornerTable.getHeight();
        //Log.d(LOG_TAG, "cornerPoint = " + cornerPoint[0] + ":" + cornerPoint[1]);
        cornerMatrix.mapPoints(cornerPoint);
        //Log.d(LOG_TAG, "Mapped cornerPoint = " + cornerPoint[0] + ":" + cornerPoint[1]);
        if (ev.getY() <= cornerPoint[1]) {
            // If event Y is less than mapped cornerPoint height it is either corner or column header matrix
            if (ev.getX() <= cornerPoint[0]){
                // It's corner matrix
                //Log.d(LOG_TAG, "corner Matrix");
                cornerMatrix.invert(mappingMatrix);
            } else {
                // It's column header matrix
                //Log.d(LOG_TAG, "column header Matrix");
                columnHeaderMatrix.invert(mappingMatrix);
            }
        } else {
            // It is either row header or main matrix
            if (ev.getX() <= cornerPoint[0]){
                // It's row header matrix
                //Log.d(LOG_TAG, "row header Matrix");
                rowHeaderMatrix.invert(mappingMatrix);
            } else {
                // It's main matrix
                //Log.d(LOG_TAG, "main Matrix");
                mainMatrix.invert(mappingMatrix);
            }
        }

        MotionEvent transformEvent = MotionEvent.obtain(ev);
        transformEvent.transform(mappingMatrix);

        //Log.d(LOG_TAG, "mappedEvent = " + transformEvent.getX() + ":" + transformEvent.getY());

        return transformEvent;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        //Log.d(LOG_TAG, "onScale");
        // Don't change the pan just scale
        calculatePanScale( 0, 0, detector.getFocusX(), detector.getFocusY(),detector.getScaleFactor());
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
