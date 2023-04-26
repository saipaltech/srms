package org.saipal.srms.excel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public class Reader {
	private Workbook currentWorkbook = null;
	private Sheet currentSheet = null;
	private DataFormatter df = new DataFormatter();

	private void setDefaultSheet() {
		if (currentWorkbook.getNumberOfSheets() == 1) {
			currentSheet = currentWorkbook.getSheetAt(0);
		}

	}

	public int getTotalRows() {
		return currentSheet.getPhysicalNumberOfRows();
	}

	public void read(String filePath) {
		try {
			this.currentWorkbook = WorkbookFactory.create(new File(filePath));
			setDefaultSheet();

		} catch (EncryptedDocumentException | IOException e) {
			e.printStackTrace();
		}
	}

	public void read(String filePath, String sheetName) {
		try {
			this.currentWorkbook = WorkbookFactory.create(new File(filePath));
			this.currentSheet = this.currentWorkbook.getSheet(sheetName);
			setDefaultSheet();
		} catch (EncryptedDocumentException | IOException e) {
			e.printStackTrace();
		}
		this.currentWorkbook = null;
		this.currentSheet = this.currentWorkbook.getSheet(sheetName);
	}

	public void read(MultipartFile file) {
		try {
			this.currentWorkbook = WorkbookFactory.create(file.getInputStream());
			setDefaultSheet();
		} catch (EncryptedDocumentException | IOException e) {
			e.printStackTrace();
		}
	}

	public void read(String filePath, int sheetIndex) {
		try {
			this.currentWorkbook = WorkbookFactory.create(new File(filePath));

			this.currentSheet = this.currentWorkbook.getSheetAt(sheetIndex);
			setDefaultSheet();
		} catch (EncryptedDocumentException | IOException e) {
			e.printStackTrace();
		}
	}

	public Sheet getSheet(int index) {
		currentSheet = currentWorkbook.getSheetAt(index);
		return getSheet();
	}

	public Sheet getSheet(String name) {
		currentSheet = currentWorkbook.getSheet(name);
		return getSheet();
	}

	public int getColIndex(String colName) {
		int index = -1;
		int i = 0;
		String val = "";
		do {
			val = df.formatCellValue(currentSheet.getRow(0).getCell(i));
			if (val.equals(colName)) {
				index = i;
			}
			i++;
		} while (!val.isBlank());
		return index;
	}

	public Sheet getSheet() {
		return currentSheet;
	}

	public String getValue(int row, int col) {
		try {
			Cell cell = currentSheet.getRow(row).getCell(col);
			if (cell.getCellType() == CellType.FORMULA) {

				return df.formatCellValue(cell, currentWorkbook.getCreationHelper().createFormulaEvaluator());
			}
			return df.formatCellValue(cell);
		} catch (Exception ex) {
			//ex.printStackTrace();
			return "";
		}

	}

	public String getValue(int row, String colName) {
		int col = getColIndex(colName);
		if (col == -1) {
			throw new RuntimeException("Cannot find column " + colName + " in the sheet");
		}
		Cell cell = currentSheet.getRow(row).getCell(col);
		return df.formatCellValue(cell);
	}

	public List<Map<String, String>> getData(List<String> cols, List<Integer> skipCols) {
		if (cols != null) {
			List<Map<String, String>> data = new ArrayList<>();
			int totalRows = getTotalRows();
			for (int i = 0; i < totalRows; i++) {
				int index = -1;
				HashMap<String, String> mp = new HashMap<>();
				for (String col : cols) {
					index++;
					if (skipCols != null) {
						if (skipCols.contains(index)) {
							continue;
						}
					}
					mp.put(col, getValue(i, index));
				}
				data.add(mp);
			}
			return data;
		}
		return null;
	}

}
