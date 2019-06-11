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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.BitSet;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.hotels.hde.quibble.RowPair;

@RunWith(MockitoJUnitRunner.class)
public class ReportGeneratorTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock
  private SXSSFRow row;
  @Mock
  private SXSSFCell cell;
  @Mock
  private SXSSFSheet sheet;
  @Mock
  private ReportGenerator.WorkbookFactorys workbookFactorys;
  @Mock
  private SXSSFWorkbook sxssfWorkbook;

  private ReportGenerator reportGenerator = new ReportGenerator();
  private ReportGenerator reportGenerator1;

  @Before
  public void setUp() {
    reportGenerator1 = new ReportGenerator(workbookFactorys,sxssfWorkbook,sheet);
  }

  @Test
  public void testaddRowPairToReport() {

    RowPair mockrowpair = mock(RowPair.class);
    String[] first = new String[] {"first", "second", "third"};
    String[] second = new String[] {"fourth", "fifth", "sixth"};

    when(mockrowpair.getFirstRSRow()).thenReturn(first);
    when(mockrowpair.getSecondRSRow()).thenReturn(second);
    BitSet b = new BitSet();
    b.set(1);

    reportGenerator.addRowPairToReport(mockrowpair, 0, b.get(0, 1));
  }

  @Test
  public void testaddMessageRow() throws IOException,InvalidFormatException {

    when(sxssfWorkbook.createSheet()).thenReturn(sheet);
    when(workbookFactorys.newInstance()).thenReturn(sxssfWorkbook);

    when(cell.getSheet()).thenReturn(sheet);
    when(cell.getStringCellValue()).thenReturn("message");
    when(row.createCell(2)).thenReturn(cell);
    when(sheet.createRow(0)).thenReturn(row);
    CellStyle cellStyle = mock(CellStyle.class);
    Font font = mock(Font.class);

    when(sxssfWorkbook.createFont()).thenReturn(font);
    when(sxssfWorkbook.createCellStyle()).thenReturn(cellStyle);

    reportGenerator1.addMessageRow("message", 0);
    assertThat(cell.getStringCellValue(), is("message"));
  }

  @Test(expected = IOException.class)
  public void closeReportNonExistentFile() throws IOException {
    reportGenerator.closeReport("folder/does/not/exist");
  }

  @Test
  public void testcreateSheetTitle() throws IOException, InvalidFormatException {
    reportGenerator.createSheetTitle("title");
    File output = tempFolder.newFolder("report");
    reportGenerator.closeReport(output.getAbsolutePath());

    File[] files = new File(output.getAbsolutePath()).listFiles();
    assertTrue(files[0].exists());

    Workbook workbook = WorkbookFactory.create(new File(files[0].getAbsolutePath()));
    Sheet sheet = workbook.getSheetAt(0);
    DataFormatter dataFormatter = new DataFormatter();

    for (Row row : sheet) {
      for (Cell cell : row) {
        String cellValue = dataFormatter.formatCellValue(cell);
        assertThat(cellValue, is("title"));
      }
    }
    workbook.close();
  }

  @Test
  public void testaddHeadersToReport() throws SQLException {

    ResultSet rs1 = mock(ResultSet.class);
    ResultSet rs2 = mock(ResultSet.class);

    ResultSetMetaData rsMD1 = mock(ResultSetMetaData.class);
    ResultSetMetaData rsMD2 = mock(ResultSetMetaData.class);

    when(rs1.getMetaData()).thenReturn(rsMD1);
    when(rs2.getMetaData()).thenReturn(rsMD2);

    when(rsMD1.getColumnCount()).thenReturn(3);
    when(rsMD2.getColumnCount()).thenReturn(3);

    when(rs1.next()).thenReturn(true, true, true, false);
    when(rs2.next()).thenReturn(true, true, true, false);

    when(rs1.getString(1)).thenReturn("123");
    when(rs1.getString(2)).thenReturn("1-1-2015");
    when(rs1.getString(3)).thenReturn("GBP");

    when(rs2.getString(1)).thenReturn("123");
    when(rs2.getString(2)).thenReturn("1-1-2015");
    when(rs2.getString(3)).thenReturn("GBP");

    reportGenerator.addHeadersToReport(rsMD1, rsMD2);
  }
}
