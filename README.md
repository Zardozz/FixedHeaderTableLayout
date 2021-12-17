<div align="center">
    <h2>Fixed Header Table Layout for Android</h2>
</div>

FixedHeaderTableLayout is a powerful Android library for displaying complex data structures and rendering tabular data composed of rows, columns and cells with multi direction scrolling and zooming.

This repository also contains a sample app that is designed to show you how to create your own FixedHeaderTableLayout in your application.

FixedHeaderTableLayout is similar in construction and use as to Android's TableLayout  

<p align="center">
      <img src="https://raw.githubusercontent.com/Zardozz/FixedHeaderTableLayout/master/art/FixedHeaderTableLayout.gif">
</p>

# Note
<h1>This Library is current in development and is considered in an Alpha state</h1>

## Features
  - [x] 1 to X number of rows can be fixed as column headers at the top of the table.
  - [x] 1 to X number of rows can be fixed as row headers at the left of the table.
  - [x] Multi direction scrolling is available if the table is larger than the screen.
  - [x] Pinch Zoom is available.
  - [x] Standard scrollbars are available.
  - [x] Clicks are passed to children views.
  - [x] Each column width value will be automatically adjusted to fit the largest cell in the column.
  - [x] Each row height value will be automatically adjusted to fit the largest cell in the row.
  - [x] Support for API 16 upwards

## Feature TODO list
  - [x] Scale around pinch center.
  - [x] Corner layout location and layout direction to support Right to Left Languages.
  - [x] Some type of column span (Nested Tables) support.
  - [x] Making the fixed headers optional (at least one of each is required at the moment).
  - [x] Documentation.
  - [x] Automated Tests.
  - [x] Probably lots more.

## Limitations
  - [x] As per Android's TableLayout constructing/drawing very large tables takes some time.


## What's new

You can check new implementations of `TableView` on the [release page](https://github.com/Zardozz/FixedHeaderTableLayout/releases).

## Installation

To use this library in your Android project

Add Maven Central to the project's `build.gradle` :
```
allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}
```

Add the following dependency into your module's `build.gradle`:
```
dependencies {
    implementation 'com.github.Zardozz:fixedheadertablelayout:0.0.0.2'
}
```

## Documentation

Please check out the [project's wiki](https://github.com/Zardozz/FixedHeaderTableLayout/wiki).

## Contributors

Contributions of any kind are welcome!

If you wish to contribute to this project, please refer to our [contributing guide](.github/CONTRIBUTING.md).

## License

```
MIT License

Copyright (c) 2021 Andrew Beck

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
