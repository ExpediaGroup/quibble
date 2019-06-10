/**
 * Copyright (C) 2015-2019 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hotels.hde.quibble.report;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotels.hde.quibble.RowPair;
import com.hotels.hde.quibble.TestSuite;

public class ReportGenerator {

  private final Logger logger = LoggerFactory.getLogger(TestSuite.class);

  private static final int TITLE_ROW_NUMBER_START = 1;
  private static final int TITLE_ROW_NUMBER_END = 2;
  private static final int TITLE_CELL_NUMBER_START = 2;
  private static final int TITLE_CELL_NUMBER_END = 12;
  private static final short TITLE_FONT_HEIGHT = 20;
  private static final int MESSAGE_ROW_CELL_NUMBER_START = 2;
  private static final int MESSAGE_ROW_CELL_NUMBER_END = 12;
  private static final int ROWS_TO_KEEP_IN_MEMORY = 100;
  private static final double COMPRESSED_FILE_INFLATE_RATIO = 0;
  private static final int ROW_HEADER_POSITION_FIRST = 5;
  private static final int ROW_HEADER_POSITION_SECOND = ROW_HEADER_POSITION_FIRST + 1;
  public static final int FIRST_DATA_ROW_NUMBER = ROW_HEADER_POSITION_FIRST + 3;
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

  private SXSSFWorkbook workbook;
  private SXSSFSheet sheet;

  static class WorkbookFactorys {
    SXSSFWorkbook newInstance() {
      return new SXSSFWorkbook(ROWS_TO_KEEP_IN_MEMORY);
    }
  }

  private WorkbookFactorys workbookFactory;

  ReportGenerator(WorkbookFactorys factorys,SXSSFWorkbook workbook,SXSSFSheet sheet){
    this.workbookFactory = factorys;
    this.workbook = workbook;
    this.sheet = sheet;
  }

  public ReportGenerator() {
    workbook = new WorkbookFactorys().newInstance();
    //workbook = new SXSSFWorkbook(ROWS_TO_KEEP_IN_MEMORY); // keep these rows in memory, exceeding rows will be flushed
    // to disk
    workbook.setCompressTempFiles(true);
    ZipSecureFile.setMinInflateRatio(COMPRESSED_FILE_INFLATE_RATIO);
    sheet = workbook.createSheet();
  }

  public void createSheetTitle(String title) {
    Row reportTitleRow = sheet.createRow(TITLE_ROW_NUMBER_START);
    Cell testNameCell = reportTitleRow.createCell(TITLE_CELL_NUMBER_START);
    testNameCell.setCellValue(title);

    testNameCell.setCellStyle(getTitleStyle());
    CellRangeAddress cellRange = new CellRangeAddress(TITLE_ROW_NUMBER_START, TITLE_ROW_NUMBER_END,
        TITLE_CELL_NUMBER_START, TITLE_CELL_NUMBER_END);
    sheet.addMergedRegion(cellRange);
    cellRange.formatAsString();
  }

  private CellStyle getTitleStyle() {
    CellStyle titleStyle = workbook.createCellStyle();
    Font titleFont = workbook.createFont();
    titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
    titleFont.setFontHeightInPoints(TITLE_FONT_HEIGHT);
    titleStyle.setFont(titleFont);
    return titleStyle;
  }

  private CellStyle getBottomBorderStyle() {
    CellStyle bottomBorder = workbook.createCellStyle();
    bottomBorder.setBorderBottom(CellStyle.BORDER_THICK);
    return bottomBorder;

  }

  private CellStyle getRedStyle() {
    CellStyle redStyle = workbook.createCellStyle();
    redStyle.cloneStyleFrom(getBottomBorderStyle());
    redStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
    redStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
    return redStyle;
  }

  private CellStyle getHeaderStyle() {
    CellStyle headerStyle = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBoldweight(Font.BOLDWEIGHT_BOLD);
    headerStyle.setFont(font);
    return headerStyle;
  }

  public void autoSizeColumns(int columnCount) {
    for (int i = 0; i < columnCount; i++) {
      sheet.autoSizeColumn(i);
    }
  }

  public void addHeadersToReport(ResultSetMetaData firstResultSetMetaData, ResultSetMetaData secondResultSetMetaData)
    throws SQLException {
    int numberOfColumns = firstResultSetMetaData.getColumnCount();

    String[] firstRowHeader = new String[numberOfColumns];
    String[] secondRowHeader = new String[numberOfColumns];

    for (int index = 0; index < numberOfColumns; index++) {
      firstRowHeader[index] = firstResultSetMetaData.getColumnName(index + 1);
      secondRowHeader[index] = secondResultSetMetaData.getColumnName(index + 1);
    }

    CellStyle headerStyle = getHeaderStyle();
    createRow(firstRowHeader, ROW_HEADER_POSITION_FIRST, headerStyle);
    createRow(secondRowHeader, ROW_HEADER_POSITION_SECOND, headerStyle);
  }

  private void createRow(String[] rowData, int rowPosition, CellStyle style) {
    Row row = sheet.createRow(rowPosition);

    for (int cellNum = 0; cellNum < rowData.length; cellNum++) {
      Cell cell = row.createCell(cellNum);
      cell.setCellValue(rowData[cellNum]);
      cell.setCellStyle(style);
    }

  }

  public void addRowPairToReport(RowPair rowPair, int startPosition, BitSet differences) {

    String[] firstDataRow = rowPair.getFirstRSRow();
    String[] secondDataRow = rowPair.getSecondRSRow();

    CellStyle noStyle = workbook.createCellStyle();
    createRow(firstDataRow, startPosition, noStyle);

    CellStyle redStyle = getRedStyle();
    CellStyle bottomBorderStyle = getBottomBorderStyle();
    addSecondRowToReport(secondDataRow, startPosition + 1, differences, redStyle, bottomBorderStyle);

  }

  private void addSecondRowToReport(
      String[] resultSetRow,
      int rowPosition,
      BitSet differences,
      CellStyle redStyle,
      CellStyle bottomBorderStyle) {
    Row row = sheet.createRow(rowPosition);

    for (int cellNum = 0; cellNum < resultSetRow.length; cellNum++) {
      Cell cell = row.createCell(cellNum);
      cell.setCellValue(resultSetRow[cellNum]);
      if (differences.get(cellNum)) {
        cell.setCellStyle(redStyle);
      } else {
        cell.setCellStyle(bottomBorderStyle);
      }
    }
  }

  public void addMessageRow(String message, int rowPosition) {
    Row row = sheet.createRow(rowPosition);
    Cell cell = row.createCell(MESSAGE_ROW_CELL_NUMBER_START);
    cell.setCellValue(message);
    cell.setCellStyle(getHeaderStyle());
    CellRangeAddress cellRange = new CellRangeAddress(rowPosition, rowPosition, MESSAGE_ROW_CELL_NUMBER_START,
        MESSAGE_ROW_CELL_NUMBER_END);
    sheet.addMergedRegion(cellRange);
    cellRange.formatAsString();
  }

  public void closeReport(String reportPath) throws IOException {
    String reportFile = "";
    Date date = new Date();
    reportFile = reportPath + "/" + "Data_Match_Report" + DATE_FORMAT.format(date) + ".xlsx";
    try (FileOutputStream out = new FileOutputStream(reportFile)) {
      if (workbook != null) {
        workbook.write(out);
        // dispose of temporary files backing this workbook on disk
        workbook.dispose();
        logger.info("Successfully saved and closed report file: " + reportFile);
      }
    }
  }

}
