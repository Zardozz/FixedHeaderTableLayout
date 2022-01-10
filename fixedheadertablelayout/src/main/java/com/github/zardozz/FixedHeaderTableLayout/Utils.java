/*
 *  MIT License
 *
 * Copyright (c) 2022 Andrew Beck
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

import java.util.ArrayList;

public class Utils {

    public static ArrayList<Integer> calculateMaxRowHeight(ArrayList<Integer> existHeights, FixedHeaderSubTableLayout table) {
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

    public static void setMaxRowHeight(ArrayList<Integer> newHeights, FixedHeaderSubTableLayout table){
        for (int row = 0; row < table.getChildCount(); row++) {
            FixedHeaderTableRow tableRow = (FixedHeaderTableRow) table.getChildAt(row);
            tableRow.setMaxChildHeight(newHeights.get(row));
        }
    }

    public static ArrayList<Integer> calculateMaxColumnWidth(ArrayList<Integer> existWidths, FixedHeaderSubTableLayout table) {
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

    public static void setMaxColumnWidth(ArrayList<Integer> newWidths, FixedHeaderSubTableLayout table){
        for (int row = 0; row < table.getChildCount(); row++) {
            FixedHeaderTableRow tableRow = (FixedHeaderTableRow) table.getChildAt(row);
            tableRow.setColumnWidths(newWidths);
        }
    }
}
