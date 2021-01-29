package com.lanternsoftware.util.excel;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.csv.CSV;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.SheetUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ExcelWriter {
	private static final Logger LOG = LoggerFactory.getLogger(ExcelWriter.class);

	public static Workbook toExcel(CSV _csv, String _sSheetName) {
		try {
			Workbook wb = new XSSFWorkbook();
			addSheet(wb, _csv, _sSheetName);
			return wb;
		} catch (Throwable t) {
			LOG.error("Failed to convert CSV to Excel Workbook", t);
			return null;
		}
	}

	public static Sheet addSheet(Workbook _wb, CSV _csv, String _sSheetName) {
		Font font = _wb.createFont();
		font.setBold(true);
		font.setFontName("Arial");

		CellStyle header = _wb.createCellStyle();
		header.setWrapText(false);
		header.setBorderBottom(BorderStyle.THIN);
		header.setFont(font);

		DataFormatter formatter = new DataFormatter();
		int defaultCharWidth = SheetUtil.getDefaultCharWidth(_wb);

		Map<Integer, Integer> mapHeaderWidths = new HashMap<>();
		Set<Integer> setAutoSizedCols = new HashSet<>();

		Sheet sh = _wb.createSheet(_sSheetName);
		int iHeader = 0;
		if (!CollectionUtils.isEmpty(_csv.getHeaders())) {
			Row r = sh.createRow(0);
			for (int iCol = 0; iCol < _csv.columns; iCol++) {
				String sContent = _csv.getHeader(iCol);
				Cell cell = r.createCell(iCol);
				cell.setCellValue(sContent);
				cell.setCellStyle(header);
				int iWidth = (int) (SheetUtil.getCellWidth(cell, defaultCharWidth, formatter, false) * 256) + 10;
				if (iWidth > 0) {
					sh.setColumnWidth(iCol, iWidth);
					mapHeaderWidths.put(iCol, iWidth);
				}
			}
			iHeader = 1;
			sh.createFreezePane(0, 1);
		}

		for (int iRow = 0; iRow < _csv.rows; iRow++) {
			Row r = sh.createRow(iRow + iHeader);
			for (int iCol = 0; iCol < _csv.columns; iCol++) {
				String sContent = _csv.cell(iRow, iCol);
				Cell cell = r.createCell(iCol);
				cell.setCellValue(sContent);
				if (NullUtils.isNotEmpty(sContent)) {
					if (!setAutoSizedCols.contains(iCol)) {
						int iWidth = (int) (SheetUtil.getCellWidth(cell, defaultCharWidth, formatter, false) * 256) + 10;
						Integer headerWidth = mapHeaderWidths.get(iCol);
						if (headerWidth == null)
							headerWidth = 0;
						if (iWidth > headerWidth) {
							sh.setColumnWidth(iCol, iWidth);
						}
						setAutoSizedCols.add(iCol);
					}
				}
			}
		}
		return sh;
	}

	public static CSV fromExcelFile(String _sPath) {
		return fromExcelFile(_sPath, 0);
	}

	public static CSV fromExcelFile(String _sPath, int _iTabIdx) {
		FileInputStream is = null;
		try {
			is = new FileInputStream(_sPath);
			Workbook wb = new XSSFWorkbook(is);
			return fromExcel(wb, _iTabIdx);
		} catch (Exception e) {
			LOG.error("Failed to open Excel Workbook", e);
			return null;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public static CSV fromExcel(Workbook _xls, int _iTabIdx) {
		Sheet sh = _xls.getSheetAt(_iTabIdx);
		if (sh == null)
			return null;
		DataFormatter formatter = new DataFormatter();
		int iMaxColumns = 0;
		List<List<String>> data = new ArrayList<List<String>>();
		for (int i = 0; i <= sh.getLastRowNum(); i++) {
			Row r = sh.getRow(i);
			if (r == null)
				continue;
			List<String> listColumns = new ArrayList<String>();
			for (int c = 0; c < r.getLastCellNum(); c++) {
				String sCell = "";
				Cell cell = r.getCell(c);
				try {
					sCell = formatter.formatCellValue(cell);
				} catch (Exception e) {
				}
				listColumns.add(sCell);
			}
			if (listColumns.size() > iMaxColumns)
				iMaxColumns = listColumns.size();
			data.add(listColumns);
		}
		return new CSV(null, data, iMaxColumns);
	}

	public static byte[] toByteArray(Workbook _wb) {
		if (_wb == null)
			return null;
		ByteArrayOutputStream os = null;
		try {
			os = new ByteArrayOutputStream();
			_wb.write(os);
			return os.toByteArray();
		} catch (Throwable t) {
			LOG.error("Failed to convert Excel Workbook to byte array", t);
			return null;
		} finally {
			IOUtils.closeQuietly(os);
		}
	}

	public static void writeToDisk(Workbook _wb, String _sFileName) {
		if (_wb == null)
			return;
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(_sFileName);
			_wb.write(os);
		} catch (Throwable t) {
			LOG.error("Failed to write Excel Workbook to disk", t);
		} finally {
			IOUtils.closeQuietly(os);
		}
	}
}
