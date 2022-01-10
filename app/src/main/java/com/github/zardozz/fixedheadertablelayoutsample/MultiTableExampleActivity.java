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

package com.github.zardozz.fixedheadertablelayoutsample;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.zardozz.FixedHeaderTableLayout.FixedHeaderSubTableLayout;
import com.github.zardozz.FixedHeaderTableLayout.FixedHeaderTableRow;
import com.github.zardozz.FixedHeaderTableLayout.Utils;

import java.util.ArrayList;

public class MultiTableExampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_table_example);

        LinearLayout frame = findViewById(R.id.Frame);
        LinearLayout l1 = new LinearLayout(getApplicationContext());
        l1.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        l1.setLayoutParams(params);
        frame.addView(l1);

        // Create 2 people with contact details
        Person person1 = new Person();
        person1.Name = "Joe Blogs";
        person1.ContactType1 = "Mobile";
        person1.ContactDetails1 = "07123 123456";
        person1.ContactType2 = "Email";
        person1.ContactDetails2 = "joe-blogs1234@test.com";

        Person person2 = new Person();
        person2.Name = "Frank Stein";
        person2.ContactType1 = "Landline";
        person2.ContactDetails1 = "1234 123456";
        person2.ContactType2 = "Mobile";
        person2.ContactDetails2 = "07123 654321";

        Person person3 = new Person();
        person3.Name = "Betty Boo";
        person3.ContactType1 = "SMS";
        person3.ContactDetails1 = "07123 777777";
        person3.ContactType2 = "Mobile";
        person3.ContactDetails2 = "07123 777777";


        // Create 4 independent sub tables, 1 header plus 2 modular
        FixedHeaderSubTableLayout headerTable = createHeaderTable();
        FixedHeaderSubTableLayout person1Table = createModularTable(person1);
        FixedHeaderSubTableLayout person2Table = createModularTable(person2);
        FixedHeaderSubTableLayout person3Table = createModularTable(person3);

        // Need to measure all Tables to full (UNSPECIFIED) size
        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        headerTable.measure(measureSpec, measureSpec);
        person1Table.measure(measureSpec, measureSpec);
        person2Table.measure(measureSpec, measureSpec);
        person3Table.measure(measureSpec, measureSpec);

        // As we have direct access to get and set the column sizes between the tables
        // including spanning the second column of the header row.
        // We'll leave the row heights as natural as they don't need aligning

        // This is normally done by the FixedHeaderTableLayout class that does the panning and
        // scrolling, but as we are not using that we have to do it ourselves
        ArrayList<Integer> maxColumnWidth = new ArrayList<>();
        maxColumnWidth = Utils.calculateMaxColumnWidth(maxColumnWidth, person1Table);
        maxColumnWidth = Utils.calculateMaxColumnWidth(maxColumnWidth, person2Table);
        maxColumnWidth = Utils.calculateMaxColumnWidth(maxColumnWidth, person3Table);

        // Now the header spanning
        ArrayList<Integer> maxHeaderColumnWidth = new ArrayList<>();
        // Hack going to assume the headers are short than the actual columns to simplify the logic
        // So we only have to expand the header columns
        maxHeaderColumnWidth.add(maxColumnWidth.get(0));
        // Only have 2 columns in header so it's width needs to be second column + third width
        maxHeaderColumnWidth.add(maxColumnWidth.get(1)+maxColumnWidth.get(2));

        // Now set all the tables to the new widths
        Utils.setMaxColumnWidth(maxColumnWidth, person1Table);
        Utils.setMaxColumnWidth(maxColumnWidth, person2Table);
        Utils.setMaxColumnWidth(maxColumnWidth, person3Table);
        Utils.setMaxColumnWidth(maxHeaderColumnWidth, headerTable);

        // Remeasure Tables using the new set of aligned widths (MeasureSpec does not matter)
        headerTable.measure(measureSpec, measureSpec);
        person1Table.measure(measureSpec, measureSpec);
        person2Table.measure(measureSpec, measureSpec);
        person3Table.measure(measureSpec, measureSpec);

        // Add the tables to the main LinearLayout
        l1.addView(headerTable);
        l1.addView(person1Table);
        l1.addView(person2Table);

        // Set the last outer border around all modularTable
        l1.setPadding(2,2,2,2);
        l1.setBackgroundResource(R.drawable.thick_border);

        // Add a textview to add a gap on far right to show it's not a cell of the tables
        TextView textViewGap = new TextView(getApplicationContext());
        textViewGap.setText("Gap");
        textViewGap.setGravity(Gravity.END);
        frame.addView(textViewGap);

        // Add an independent Table aligned with the other
        LinearLayout l3 = new LinearLayout(getApplicationContext());
        l3.setOrientation(LinearLayout.VERTICAL);
        l3.setLayoutParams(params);
        frame.addView(l3);
        // Set the last outer border around all modularTable
        l3.setPadding(2,2,2,2);
        l3.setBackgroundResource(R.drawable.thick_border);
        l3.addView(person3Table);


    }

    private FixedHeaderSubTableLayout createHeaderTable(){
        FixedHeaderSubTableLayout headerTable = new FixedHeaderSubTableLayout(getApplicationContext());

        // Header is only 2 columns as we will stretch the second column
        // over the second and third of the modularTables
        FixedHeaderTableRow tableHeaderRow = new FixedHeaderTableRow(getApplicationContext());
        TextView textViewHeader1 = new TextView(getApplicationContext());
        textViewHeader1.setText("Name");
        textViewHeader1.setBackgroundResource(R.drawable.list_border);
        textViewHeader1.setTypeface(textViewHeader1.getTypeface(), Typeface.BOLD);
        textViewHeader1.setPadding(5 ,5,5,5);
        tableHeaderRow.addView(textViewHeader1);
        TextView textViewHeader2 = new TextView(getApplicationContext());
        textViewHeader2.setText("Contact Details");
        textViewHeader2.setGravity(Gravity.CENTER);
        textViewHeader2.setBackgroundResource(R.drawable.list_border);
        textViewHeader2.setTypeface(textViewHeader2.getTypeface(), Typeface.BOLD);
        textViewHeader2.setPadding(5 ,5,5,5);
        tableHeaderRow.addView(textViewHeader2);

        headerTable.addView(tableHeaderRow);

        return headerTable;
    }

    private FixedHeaderSubTableLayout createModularTable(Person data){
        FixedHeaderSubTableLayout modularTable = new FixedHeaderSubTableLayout(getApplicationContext());

        // The Sub table has 2 rows of 3 columns
        FixedHeaderTableRow tableRow1 = new FixedHeaderTableRow(getApplicationContext());
        FixedHeaderTableRow tableRow2 = new FixedHeaderTableRow(getApplicationContext());

        // Add 3 cells to first row
        TextView textViewName = new TextView(getApplicationContext());
        textViewName.setText(data.Name);
        textViewName.setBackgroundResource(R.drawable.list_border);
        textViewName.setPadding(5 ,5,5,5);
        tableRow1.addView(textViewName);
        TextView textViewContactType1 = new TextView(getApplicationContext());
        textViewContactType1.setText(data.ContactType1);
        textViewContactType1.setBackgroundResource(R.drawable.list_border);
        textViewContactType1.setTypeface(textViewContactType1.getTypeface(), Typeface.BOLD);
        textViewContactType1.setPadding(5 ,5,5,5);
        tableRow1.addView(textViewContactType1);
        TextView textViewContactType2 = new TextView(getApplicationContext());
        textViewContactType2.setText(data.ContactType2);
        textViewContactType2.setBackgroundResource(R.drawable.list_border);
        textViewContactType2.setTypeface(textViewContactType2.getTypeface(), Typeface.BOLD);
        textViewContactType2.setPadding(5 ,5,5,5);
        tableRow1.addView(textViewContactType2);

        // Add 3 cells to second row
        TextView textViewBlank = new TextView(getApplicationContext());
        textViewBlank.setBackgroundResource(R.drawable.list_border);
        tableRow2.addView(textViewBlank);
        TextView textViewContactDetails1 = new TextView(getApplicationContext());
        textViewContactDetails1.setText(data.ContactDetails1);
        textViewContactDetails1.setBackgroundResource(R.drawable.list_border);
        textViewContactDetails1.setPadding(5 ,5,5,5);
        tableRow2.addView(textViewContactDetails1);
        TextView textViewContactDetails2 = new TextView(getApplicationContext());
        textViewContactDetails2.setText(data.ContactDetails2);
        textViewContactDetails2.setBackgroundResource(R.drawable.list_border);
        textViewContactDetails2.setPadding(5 ,5,5,5);
        tableRow2.addView(textViewContactDetails2);

        modularTable.addView(tableRow1);
        modularTable.addView(tableRow2);

        // Add border around table
        modularTable.setBackgroundResource(R.drawable.thick_border);

        return modularTable;
    }

}

// holds 5 items of data to be grouped together in a sub table
class Person {
    public String Name;
    public String ContactType1;
    public String ContactType2;
    public String ContactDetails1;
    public String ContactDetails2;
}