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

import android.graphics.Point;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove;
import static androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow;
import static androidx.test.espresso.assertion.PositionAssertions.isCompletelyLeftOf;
import static androidx.test.espresso.assertion.PositionAssertions.isCompletelyRightOf;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class SimpleActivityTest {

    @Rule
    public ActivityScenarioRule<TestActivity> rule = new ActivityScenarioRule<>(TestActivity.class);

    @Test
    public void testFixedHeaderTableCreate(){
        ActivityScenario<TestActivity> activityScenario = rule.getScenario();

        activityScenario.onActivity(
            activity -> {
                FixedHeaderTableLayout fixedHeaderTableLayout = new FixedHeaderTableLayout(activity.getApplicationContext());
                Assert.assertNotNull(fixedHeaderTableLayout);
            });
    }

    // Test adding the wrong type of view directly
    @Test
    public void testFixedHeaderTableNonSubTable()  {
        ActivityScenario<TestActivity> activityScenario = rule.getScenario();

        activityScenario.onActivity(
            activity -> {
                FixedHeaderTableLayout fixedHeaderTableLayout = new FixedHeaderTableLayout(activity.getApplicationContext());
                TextView textView = new TextView(activity.getApplicationContext());

                UnsupportedOperationException thrown = assertThrows(UnsupportedOperationException.class, () -> fixedHeaderTableLayout.addView(textView));
                assertEquals("Adding children directly is not supported, use addViews method", thrown.getMessage());
            });
    }

    // Test adding the right type of view directly
    @Test
    public void testFixedHeaderTableDirectSubTable()  {
        ActivityScenario<TestActivity> activityScenario = rule.getScenario();

        activityScenario.onActivity(
            activity -> {
                FixedHeaderTableLayout fixedHeaderTableLayout = new FixedHeaderTableLayout(activity.getApplicationContext());
                FixedHeaderSubTableLayout fixedHeaderSubTableLayout = new FixedHeaderSubTableLayout(activity.getApplicationContext());

                UnsupportedOperationException thrown = assertThrows(UnsupportedOperationException.class, () -> fixedHeaderTableLayout.addView(fixedHeaderSubTableLayout));
                assertEquals("Adding children directly is not supported, use addViews method", thrown.getMessage());
            });
    }

    @Test
    public void testFixedHeaderTableSmallTable(){
        ActivityScenario<TestActivity> activityScenario = rule.getScenario();

        activityScenario.onActivity(
            activity -> {
                FixedHeaderTableLayout fixedHeaderTableLayout = new FixedHeaderTableLayout(activity.getApplicationContext());
                FixedHeaderSubTableLayout[] subTables = Helpers.createSubTables(activity.getApplicationContext(), new Point(5,5), new Point(5,1), new Point(1,5), new Point(1,1));
                fixedHeaderTableLayout.addViews(subTables[0], subTables[1], subTables[2], subTables[3]);

                activity.setContentView(fixedHeaderTableLayout);
            });

        // Check a cell of the corner table is displayed
        onView(withText("A1:1")).check(matches(isDisplayed()));
        // Check a cell of the column header table is displayed
        onView(withText("B1:1")).check(matches(isDisplayed()));
        // Check a cell of the row header table is displayed
        onView(withText("C1:1")).check(matches(isDisplayed()));
        // Check a cell of the main table is displayed
        onView(withText("D1:1")).check(matches(isDisplayed()));

        // Check Locations
        onView(withText("A1:1")).check(isCompletelyLeftOf(withText("B1:1")));
        onView(withText("A1:1")).check(isCompletelyAbove(withText("C1:1")));
        onView(withText("B1:1")).check(isCompletelyAbove(withText("D1:1")));
        onView(withText("C1:1")).check(isCompletelyLeftOf(withText("D1:1")));
    }
}
