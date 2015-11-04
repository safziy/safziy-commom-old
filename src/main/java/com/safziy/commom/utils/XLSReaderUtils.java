package com.safziy.commom.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.safziy.service.log.LogUtil;

public class XLSReaderUtils {
	
	public static List<List<Object>> getData(File excelFile) {
		return getData(excelFile, 0);
	}

	public static List<List<Object>> getData(File excelFile, int sheetIndex) {
		try {
			Workbook wb = WorkbookFactory.create(excelFile);
			Sheet sheet = wb.getSheetAt(sheetIndex);
			return getData(sheet);
		} catch (EncryptedDocumentException e) {
			LogUtil.error(e);
		} catch (InvalidFormatException e) {
			LogUtil.error(e);
		} catch (IOException e) {
			LogUtil.error(e);
		}
		return null;
	}

	public static List<List<Object>> getData(Sheet sheet) {
		if (sheet == null) {
			return null;
		}
		int rowCount = sheet.getLastRowNum() + 1;
		List<List<Object>> result = new ArrayList<List<Object>>(rowCount);
		for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			int cellCount = row.getLastCellNum();
			List<Object> list = new ArrayList<Object>(cellCount);
			for (int cellIndex = 0; cellIndex < cellCount; cellIndex++) {
				Cell cell = row.getCell(cellIndex);
				list.add(getCellValue(cell));
			}
			result.add(list);
		}
		return result;
	}

	public static Object getCellValue(Cell cell) {
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_NUMERIC:
			return cell.getNumericCellValue();
		case Cell.CELL_TYPE_STRING:
			return cell.getStringCellValue();
		case Cell.CELL_TYPE_FORMULA:
			return cell.getNumericCellValue();
		case Cell.CELL_TYPE_BLANK:
			return "";
		case Cell.CELL_TYPE_BOOLEAN:
			return cell.getBooleanCellValue();
		case Cell.CELL_TYPE_ERROR:
			return null;
		default:
			return null;
		}

	}
}
