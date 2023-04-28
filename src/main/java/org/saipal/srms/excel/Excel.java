package org.saipal.srms.excel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.core.io.ClassPathResource;

public class Excel {

	public String title = "";
	public String subtitle = "";
	public String fy = "";
	public String fyYear = "";
	public String anusuchi = "";
	public String criteria = "";
	public String filename = "";
	public String orgid = "";
	public String figurein = "";
	public String inlineCss = "";
	public String cssFiles = "tblstyle_print.css";

	// private Map<Integer, Integer> skipCol = new HashMap<>();
	public String defaultSheetName = "Sheet1";
	public Workbook wb = new XSSFWorkbook();
	private ArrayList<sheet> mSheets = new ArrayList<sheet>();
	private Map<String, Integer> mSheetsIndex = new HashMap<String, Integer>();

	private int activeIndex = 0;

	private Map<Integer, Map<String, String>> figureIn = new HashMap<Integer, Map<String, String>>();
	private boolean isNumeric(String strNum) {
		if (strNum == null) {
	        return false;
	    }
	    try {
	        double d = Double.parseDouble(strNum);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	public sheet Sheets(int index) {
		return mSheets.get(index);
	}

	public sheet Sheets(String sheetname) {
		int index = mSheetsIndex.get(sheetname);
		return mSheets.get(index);
	}

	public ArrayList<sheet> Sheets() {
		return mSheets;
	}

	public sheet ActiveSheet() {
		return Sheets(activeIndex);
	}

	public int getTotalColumns() {
		return ActiveSheet().getTotalColumns();
	}

	public class sheet {
		public String title = "";
		public String subtitle = "";
		public String anusuchi = "";
		public String figurein = "";
		public String criteria = "";

		public String id = "";
		public String clas = "";
		public String dataOption = "";
		public String tableTitle = "";
		public String toolbar = "";

		private String SheetName = "Sheet1";
		private int sheetIndex = 0;

		public int headRowIndex = 0;
		public int skipRow = 0;

		public List<Integer> hiddenColumnIndex = new ArrayList<>();
		public Map<Integer, Integer> indexMap = new HashMap<>();
		public Map<Integer, Integer> skipCol = new HashMap<>();

		public int totalColumns;
		public int headRowCount = 0;
		public List<excelRow> tr = new ArrayList<>();
		public List<excelRow> htr = new ArrayList<>();

		public int getTotalColumns() {
			if (totalColumns <= 1) {
				if (tr.size() > 0) {
					return tr.get(0).cellCount();
				} else {
					if (htr.size() > 0) {
						return htr.get(htr.size() - 1).cellCount();
					} else {
						return 0;
					}
				}
			} else {
				return totalColumns;
			}
		}

		public sheet() {
			sheetIndex = 0;
		}

		public sheet(int index) {
			sheetIndex = index;
		}

		public int getSheetIndex() {
			return sheetIndex;
		}

		public void setSheetName(String sheetname) {
			if (sheetname.length() > 30) {
				sheetname = sheetname.substring(0, 30);
			}
			if (sheetname.isBlank()) {
				return;
			}
			String tsheetname = Excel.this.wb.getSheetName(sheetIndex);
			if (!tsheetname.equalsIgnoreCase(sheetname)) {
				Excel.this.wb.setSheetName(sheetIndex, sheetname);
				if (Excel.this.activeIndex == sheetIndex) {
					Excel.this.defaultSheetName = sheetname;
				}
			}
			SheetName = sheetname;
		}

		public String getSheetName() {
			return SheetName;
		}

		public List<Integer> hiddenColumnIndex() {
			return hiddenColumnIndex;
		}

		public Map<Integer, Integer> indexMap() {
			return indexMap;
		}

		public Map<Integer, Integer> skipCol() {
			return skipCol;
		}

		public void select() {
			Excel.this.activeIndex = sheetIndex;
			Excel.this.defaultSheetName = this.getSheetName();
			// Excel.this.wb.getSheet(Excel.this.defaultSheetName).setSelected(true);
			// System.out.println(this.getSheetName());
			Excel.this.wb.setActiveSheet(Excel.this.wb.getSheetIndex(defaultSheetName));
		}

		public void addHiddenColumnIndex(int colIndex) {
			hiddenColumnIndex.add(colIndex);
		}

		public Excel addRow(excelRow trs) {
			tr.add(trs);
			return Excel.this;
		}

		public Excel addHeadRow(excelRow trs) {
			htr.add(trs);
			return Excel.this;
		}

		public excelRow insertRow(int index) {
			int hrow = ActiveSheet().htr.size();
			int drow = ActiveSheet().tr.size();
			int tindex = index;
			excelRow trs = new excelRow();
			if (index < 0) {
				return null;
			}

			if (index < hrow - 1) {
				ActiveSheet().htr.add(index, trs);
			} else {
				index = index - (hrow);
				if (index < drow) {
					ActiveSheet().tr.add(index, trs);
				} else {
					return null;
				}
			}
			return row(tindex);
		}

		public void deleteRow(int index) {
			if (index < 0) {
				return;
			}
			if (index < htr.size()) {
				htr.remove(index);
			} else {
				index = index - (htr.size());
				if (index < tr.size()) {
					tr.remove(index);
				}
			}

		}

		public excelRow row(int index) {
			int totalRow = htr.size() + tr.size();
			if (totalRow <= index || index < 0) {
				return null;
			} else {
				if (index < htr.size()) {
					return htr.get(index);
				} else {
					index = index - htr.size();
					return tr.get(index);
				}
			}
		}

		public int rowCount() {
			return (htr.size() + tr.size());
		}

		public excelCell cell(int rowIndex, int colIndex) {
			excelRow r = row(rowIndex);
			if (r == null) {
				return null;
			} else {
				return r.cell(colIndex);
			}
		}

		private boolean hasNext(int colIndex) {
			if (skipCol.get(colIndex) == null) {
				return false;
			} else if (skipCol.get(colIndex) < 1) {
				return false;
			} else {
				return true;
			}
		}
	}

	public void addHiddenColumnIndex(int colIndex) {
		ActiveSheet().hiddenColumnIndex().add(colIndex);
	}

	public Excel(int totlColumns) {
		sheet s = new sheet(mSheets.size());
		mSheets.add(s);
		activeIndex = mSheets.size() - 1;
		defaultSheetName = "Sheet" + (mSheets.size());
		mSheetsIndex.put(defaultSheetName, activeIndex);

		this.wb.createSheet(defaultSheetName);
		ActiveSheet().setSheetName(defaultSheetName);
		ActiveSheet().totalColumns = totlColumns;
	}

	public Excel() {
		sheet s = new sheet(mSheets.size());
		mSheets.add(s);
		activeIndex = mSheets.size() - 1;
		defaultSheetName = "Sheet" + (mSheets.size());
		mSheetsIndex.put(defaultSheetName, activeIndex);

		this.wb.createSheet(defaultSheetName);
		ActiveSheet().setSheetName(defaultSheetName);
		ActiveSheet().totalColumns = 1;

	}

	public String getFigureIn(int figure, String lang) {
		lang = lang.toUpperCase();
		// System.out.println(figure+":"+lang);
		return figureIn.get(figure).get(lang).toString();
	}

	public String getFigureIn(String figure, String lang) {
		if (figure.isBlank()) {
			figure = "1";
		}
		if (isNumeric(figure)) {
			return getFigureIn(Integer.parseInt(figure), lang);
		} else {
			return figure;
		}
	}

	public Sheet getSheet() {
		return wb.getSheet(defaultSheetName);
	}

	public int getRowCount() {
		return wb.getSheet(defaultSheetName).getPhysicalNumberOfRows();
	}

	public void addSheet() {
		sheet s = new sheet(mSheets.size());
		mSheets.add(s);
		activeIndex = mSheets.size() - 1;
		defaultSheetName = "Sheet" + (mSheets.size());
		mSheetsIndex.put(defaultSheetName, activeIndex);

		wb.createSheet(defaultSheetName);
		ActiveSheet().setSheetName(defaultSheetName);
		ActiveSheet().totalColumns = 1;

	}

	public void addSheet(int totlColumns) {
		sheet s = new sheet(mSheets.size());
		mSheets.add(s);
		activeIndex = mSheets.size() - 1;
		defaultSheetName = "Sheet" + (mSheets.size());
		mSheetsIndex.put(defaultSheetName, activeIndex);
		this.wb.createSheet(defaultSheetName);

		ActiveSheet().setSheetName(defaultSheetName);
		ActiveSheet().totalColumns = totlColumns;

	}

	public Excel addRow(excelRow tr) {
		ActiveSheet().tr.add(tr);
		return this;
	}

	public Excel addHeadRow(excelRow tr) {
		ActiveSheet().htr.add(tr);
		return this;
	}

	public excelRow insertRow(int index) {
		int hrow = ActiveSheet().htr.size();
		int drow = ActiveSheet().tr.size();
		int tindex = index;
		excelRow tr = new excelRow();
		if (index < 0) {
			return null;
		}

		if (index < hrow - 1) {
			ActiveSheet().htr.add(index, tr);
		} else {
			index = index - (hrow);
			if (index < drow) {
				ActiveSheet().tr.add(index, tr);
			} else {
				return null;
			}
		}
		return this.row(tindex);
	}

	public void deleteRow(int index) {
		if (index < 0) {
			return;
		}
		if (index < ActiveSheet().htr.size()) {
			ActiveSheet().htr.remove(index);
		} else {
			index = index - (ActiveSheet().htr.size());
			if (index < ActiveSheet().tr.size()) {
				ActiveSheet().tr.remove(index);
			}
		}

	}

	public Workbook getExcel() {
		return getExcel(true);
	}

	// Added on 11 Feb 20201 Begin
	public excelRow row(int index) {
		int totalRow = ActiveSheet().htr.size() + ActiveSheet().tr.size();
		if (totalRow <= index || index < 0) {
			return null;
		} else {
			if (index < ActiveSheet().htr.size()) {
				return ActiveSheet().htr.get(index);
			} else {
				index = index - ActiveSheet().htr.size();
				return ActiveSheet().tr.get(index);
			}
		}
	}

	public int rowCount() {
		return (ActiveSheet().htr.size() + ActiveSheet().tr.size());
	}

	public excelCell cell(int rowIndex, int colIndex) {
		excelRow r = this.row(rowIndex);
		if (r == null) {
			return null;
		} else {
			return r.cell(colIndex);
		}
	}

	// Added on 11 Feb 20201 End
	public Workbook getExcel(boolean headerRequired) {
		for (int i = 0; i < Sheets().size(); i++) {
			Sheets(i).select();
			if (headerRequired) {
				makeExcelHeader();
			}
			if (ActiveSheet().htr.size() > 0) {
				this.prepareHeadRows();
			}
			if (ActiveSheet().tr.size() > 0) {
				this.prepareRows();
			}
			this.setBorder();
		}

		return wb;
	}

	public Workbook getExcelTest() {
		// makeExcelHeader();

		this.prepareRowsTest();

		return wb;
	}

	private boolean hasNext(int colIndex) {
		if (ActiveSheet().skipCol.get(colIndex) == null) {
			return false;
		} else if (ActiveSheet().skipCol.get(colIndex) < 1) {
			return false;
		} else {
			return true;
		}
	}

	private void prepareRows() {
		Sheet sh = getSheet();
		if (ActiveSheet().hiddenColumnIndex.size() > 0) {
			for (int i : ActiveSheet().hiddenColumnIndex) {
				sh.setColumnHidden(i, true);
			}
		}

		int rowIndex = ActiveSheet().htr.size(), colIndex = 0;
		for (excelRow trs : ActiveSheet().tr) {
			if (trs.data.size() > 0) {
				Row row = sh.createRow(rowIndex);
				if (trs.hidden) {
					row.setZeroHeight(true);
				}
				int cellIndex = 0;
				for (excelCell tc : trs.data) {
					for (int i = cellIndex; i < ActiveSheet().getTotalColumns(); i++) {
						if (hasNext(i)) {
							colIndex++;
						} else {
							cellIndex++;
							break;
						}
					}
					Cell cell = row.createCell(colIndex);
					// data cleansing
					tc.data = cleanData(tc.data);
					if (!tc.formula.isBlank()) {
						cell.setCellFormula(tc.formula);
					} else {
						if (isNumeric(tc.data)) {
							cell.setCellValue(Double.parseDouble(tc.data));
						} else {
							cell.setCellValue(tc.data);
						}
					}
					if (tc.rowspan > 1 && tc.colspan > 1) {
						CellRangeAddress cra = new CellRangeAddress(rowIndex, rowIndex + tc.rowspan - 1, colIndex,
								colIndex + tc.colspan - 1);
						sh.addMergedRegion(cra);
						rowIndex += tc.rowspan - 1;
						colIndex += tc.colspan - 1;
					} else if (tc.rowspan > 1 && tc.colspan < 2) {
						CellRangeAddress cra = new CellRangeAddress(rowIndex, rowIndex + tc.rowspan - 1, colIndex,
								colIndex);
						sh.addMergedRegion(cra);
						rowIndex += tc.rowspan - 1;
					} else if (tc.colspan > 1 && tc.rowspan < 2) {
						CellRangeAddress cra = new CellRangeAddress(rowIndex, rowIndex, colIndex,
								colIndex + tc.colspan - 1);
						sh.addMergedRegion(cra);
						colIndex += tc.colspan - 1;
					}
					colIndex++;
					cellIndex++;
				}
				colIndex = 0;
				rowIndex++;
			}
		}
	}

	public String cleanData(String data) {
		if (data.contains("<div")) {
			data = data.substring(data.indexOf('>') + 1);
		}
		data = data.replace("</div>", "");
		if (data.contains("<span")) {
			data = data.substring(data.indexOf('>') + 1);
		}
		data = data.replace("</span>", "");
		if (data.contains("<a")) {
			data = data.substring(data.indexOf('>') + 1);
		}
		data = data.replace("</a>", "");
		return data;
	}

	private void prepareRowsTest() {

		Sheet sh = getSheet();
		if (ActiveSheet().hiddenColumnIndex.size() > 0) {
			for (int i : ActiveSheet().hiddenColumnIndex) {
				sh.setColumnHidden(i, true);
			}
		}
		int rowIndex = ActiveSheet().htr.size(), colIndex = 0;
		for (excelRow trs : ActiveSheet().tr) {
			if (trs.data.size() > 0) {
				Row row = sh.createRow(rowIndex);
				if (trs.hidden) {
					row.setZeroHeight(true);
				}
				int cellIndex = 0;
				for (excelCell tc : trs.data) {
					for (int i = cellIndex; i < ActiveSheet().getTotalColumns(); i++) {
						if (hasNext(i)) {
							colIndex++;
						} else {
							cellIndex++;
							break;
						}
					}
					colIndex = getNextEmptyIndex(colIndex, rowIndex);
					Cell cell = row.createCell(colIndex);

					if (!tc.formula.isBlank()) {
						cell.setCellFormula(tc.formula);
					} else {
						if (isNumeric(tc.data)) {
							cell.setCellValue(Double.parseDouble(tc.data));
						} else {
							cell.setCellValue(tc.data);
						}
					}
					if (tc.rowspan > 1 && tc.colspan > 1) {
						CellRangeAddress cra = new CellRangeAddress(rowIndex, rowIndex + tc.rowspan - 1, colIndex,
								colIndex + tc.colspan - 1);
						for (int i = colIndex; i <= colIndex + tc.colspan - 1; i++)
							ActiveSheet().indexMap.put(i, tc.rowspan + rowIndex - 1);
						sh.addMergedRegion(cra);
						colIndex += tc.colspan - 1;
					} else if (tc.rowspan > 1) {
						ActiveSheet().indexMap.put(colIndex, tc.rowspan + rowIndex - 1);
						CellRangeAddress cra = new CellRangeAddress(rowIndex, rowIndex + tc.rowspan - 1, colIndex,
								colIndex);
						sh.addMergedRegion(cra);
					} else if (tc.colspan > 1) {
						CellRangeAddress cra = new CellRangeAddress(rowIndex, rowIndex, colIndex,
								colIndex + tc.colspan - 1);
						sh.addMergedRegion(cra);
						colIndex += tc.colspan - 1;
					}
					colIndex++;
					cellIndex++;
				}
				colIndex = 0;
				rowIndex++;
			}
		}
	}

	public int getNextEmptyIndex(int index, int rowindex) {
		while (ActiveSheet().indexMap.get(index) != null && ActiveSheet().indexMap.get(index) >= rowindex) {
			index++;
		}
		return index;
	}

	private void prepareHeadRows() {
		Sheet sh = getSheet();
		int rowIndex = ActiveSheet().headRowIndex, colIndex = 0;
		Map<String, Integer> RowSpan = new HashMap<String, Integer>();
		for (excelRow trs : ActiveSheet().htr) {
			if (trs.data.size() > 0) {
				Row row = sh.createRow(rowIndex);
				int maxRowSpan = 0;
				for (excelCell tc : trs.data) {
					// RowSpan Begin
					int trspan = tc.rowspan;
					int tcspan = tc.colspan;
					if (trspan <= 0) {
						trspan = 1;
					}
					if (tcspan <= 0) {
						tcspan = 1;
					}
					int i = 0;

					if (rowIndex > ActiveSheet().headRowIndex) {
						i = colIndex;
						int mapIndex = 1;
						try {
							mapIndex = RowSpan.get("R" + colIndex);
						} catch (Exception e) {
							mapIndex = 1;
						}
						while (mapIndex > 1) {
							RowSpan.put("R" + colIndex, (mapIndex - 1));
							colIndex++;
							try {
								mapIndex = RowSpan.get("R" + colIndex);
							} catch (Exception e) {
								mapIndex = 1;
							}
						}
					}
					// RowSpan.put("R"+colIndex, tc.rowspan);

					Cell cell = row.createCell(colIndex);
					String text = tc.data;
					text = cleanData(text);

					for (i = 0; i < tcspan; i++) {
						RowSpan.put("R" + (i + colIndex), trspan);
					}

					/*
					 * if (!util.request("viewtype").equals("3")) { // htmlreport
					 * html=html.replace("<div class=\"figurein\">", "").replace("</div>", ""); }
					 */

					if (trs.clas.isBlank()) {
						if (tc.style.isBlank()) {
							tc.style = "text-align:center;vertical-align:center; background:#ccc";
						}
					}
					CellStyle cs = wb.createCellStyle();
					cs = tc.calcStyles(cs);

					if (cs.getWrapText()) {
						int count = (tc.data.split("<br>", -1).length);
						text = tc.data.replaceAll("<br>", "\n");

						row.setHeightInPoints((count * getSheet().getDefaultRowHeightInPoints()));
					}
					cell.setCellStyle(cs);
					text = text.replace("<div class=\"figurein\">", "");
					cell.setCellValue(text);
					if (tc.rowspan > 1 && tc.colspan > 1) {
						CellRangeAddress cra = new CellRangeAddress(rowIndex, rowIndex + tc.rowspan - 1, colIndex,
								colIndex + tc.colspan - 1);
						sh.addMergedRegion(cra);
						if (maxRowSpan < tc.rowspan) {
							maxRowSpan = tc.rowspan;
						}
						colIndex += tc.colspan - 1;
					} else if (tc.rowspan > 1 && tc.colspan < 2) {
						CellRangeAddress cra = new CellRangeAddress(rowIndex, rowIndex + tc.rowspan - 1, colIndex,
								colIndex);
						sh.addMergedRegion(cra);
						// rowIndex += tc.rowspan - 1;
						if (maxRowSpan < tc.rowspan) {
							maxRowSpan = tc.rowspan;
						}
					} else if (tc.colspan > 1 && tc.rowspan < 2) {
						CellRangeAddress cra = new CellRangeAddress(rowIndex, rowIndex, colIndex,
								colIndex + tc.colspan - 1);
						sh.addMergedRegion(cra);
						colIndex += tc.colspan - 1;
					}
					colIndex++;
				}
				// Added for RowSpan Management Begin
				if (colIndex <= this.getTotalColumns() - 1) {
					for (int i = colIndex; i < this.getTotalColumns(); i++) {
						int mapIndex = 1;
						try {
							mapIndex = RowSpan.get("R" + i);
						} catch (Exception e) {
							mapIndex = 1;
						}
						mapIndex--;
						if (mapIndex < 1) {
							mapIndex = 1;
						}
						RowSpan.put("R" + colIndex, mapIndex);
					}
				}
				// Added for RowSpan Management End
				colIndex = 0;
				if (maxRowSpan > 1) {
					rowIndex++;// = maxRowSpan;
					ActiveSheet().skipRow += maxRowSpan;
				} else {
					rowIndex++;
				}
			}
		}
	}

	public String getHtmlTable() {
		makeHtmlHeader();

		/*
		 * public String id=""; public String clas=""; public String dataOption="";
		 * public String tableTitle="";
		 * 
		 */
		String tblSetting = "";
		if (ActiveSheet().id.isBlank()) {
			tblSetting = " id=\"row_head\"";
		} else {
			tblSetting = " id=\"" + ActiveSheet().id + "\"";
		}

		if (ActiveSheet().clas.isBlank()) {
			tblSetting = tblSetting + " class=\"rtable\"";
		} else {
			tblSetting = tblSetting + " class=\"" + ActiveSheet().clas + "\"";
		}

		if (!ActiveSheet().dataOption.isBlank()) {
			tblSetting = tblSetting + " data-options=\"" + ActiveSheet().dataOption + "\"";
		}

		if (!ActiveSheet().tableTitle.isBlank()) {
			tblSetting = tblSetting + " title=\"" + ActiveSheet().tableTitle + "\"";
		}

		if (!ActiveSheet().toolbar.isBlank()) {
			tblSetting = tblSetting + " toolbar=\"" + ActiveSheet().toolbar + "\"";
		}

		// String html = "<table calss=\"rtable\" id=\"row_head\">\n";
		String html = "<table " + tblSetting + ">\n";
		html += prepareHtmlHead();
		html += prepareHtmlBody();
		html += "</table>";
		return html;
	}

	public String getHtmlTableNoHeader() {
		StringBuilder html = new StringBuilder();
		String tblSetting = "";
		if (ActiveSheet().id.isBlank()) {
			tblSetting = " id=\"row_head\"";
		} else {
			tblSetting = " id=\"" + ActiveSheet().id + "\"";
		}

		if (ActiveSheet().clas.isBlank()) {
			tblSetting = tblSetting + " class=\"rtable\"";
		} else {
			tblSetting = tblSetting + " class=\"" + ActiveSheet().clas + "\"";
		}

		if (!ActiveSheet().dataOption.isBlank()) {
			tblSetting = tblSetting + " data-options=\"" + ActiveSheet().dataOption + "\"";
		}

		if (!ActiveSheet().tableTitle.isBlank()) {
			tblSetting = tblSetting + " title=\"" + ActiveSheet().tableTitle + "\"";
		}

		if (!ActiveSheet().toolbar.isBlank()) {
			tblSetting = tblSetting + " toolbar=\"" + ActiveSheet().toolbar + "\"";
		}
		// html.append("<table calss=\"rtable\" id=\"row_head\">\n");
		html.append("<table " + tblSetting + ">\n");
		html.append(prepareHtmlHead());
		html.append(prepareHtmlBody());
		html.append("</table >\n");
		return html.toString();
	}

	private String prepareHtmlHead() {
		String html = "";
		if (ActiveSheet().htr.size() > 0) {
			html += "\t<thead>\n";
			for (excelRow trs : ActiveSheet().htr) {
				if (trs.data.size() > 0) {
					html += "\t\t" + trs.trOpening() + "\n\t\t\t";
					for (excelCell tc : trs.data) {
						html += tc.getTh();
					}
					html += "\n\t\t" + trs.trClosing() + "\n";
				}
			}
			html += "\t</thead>\n";
		}

		return html;
	}

	private String prepareHtmlBody() {
		StringBuilder html = new StringBuilder();
		if (ActiveSheet().tr.size() > 0) {
			for (excelRow trs : ActiveSheet().tr) {
				if (trs.data.size() > 0) {
					html.append("\t" + trs.trOpening() + "\n\t\t");
					for (excelCell tc : trs.data) {
						html.append(tc.getTd());
					}
					html.append("\n\t" + trs.trClosing() + "\n");
				}
			}
		}
		return html.toString();
	}

	private void makeHtmlHeader() {
		String orgname = "";
		excelRow firstRow = new excelRow("LetterHead");
		excelRow secondRow = new excelRow("LetterHead");
		excelRow thirdRow = new excelRow("LetterHead");

		// New Code Begin
		String mAnusuchi = ActiveSheet().anusuchi;
		if (mAnusuchi.isBlank()) {
			mAnusuchi = anusuchi;
		}
		String mTitle = ActiveSheet().title;
		if (mTitle.isBlank()) {
			mTitle = title;
		}
		String mSubTitle = ActiveSheet().subtitle;
		if (mSubTitle.isBlank()) {
			mSubTitle = subtitle;
		}

		// New COde End

		StringBuilder logoInfo = new StringBuilder();

		logoInfo.append("<div class=\"logo\">")
		//.append("<img src=\"")
				// .append(session.session("baseUrl"))
				.append("</div>").append("<div class=\"anusuchi\" contenteditable='true'>")
				// .append(lService.wds(mAnusuchi))
				.append("</div>").append("<div class=\"orginfo\">").append(orgname).append("</div>");

		StringBuilder reportTitle = new StringBuilder();
		reportTitle.append("<div contenteditable='true'>").append("<div calss=\"title\">").append(mTitle)
				.append("</div></div>");
		if (!mSubTitle.isBlank())
			reportTitle.append("<div contenteditable='true'>").append("<div style=\"font-size: large;\" calss=\"subtitle\">").append(mSubTitle)
					.append("</div></div>");

		firstRow.addColumn(new excelCell(logoInfo.toString(), ActiveSheet().getTotalColumns()));
		secondRow.addColumn(new excelCell(reportTitle.toString(), ActiveSheet().getTotalColumns()));

		String mFigureIn = ActiveSheet().figurein;
		if (mFigureIn.isBlank()) {
			mFigureIn = figurein;
		}
		String mCriteria = ActiveSheet().criteria;
		if (mCriteria.isBlank()) {
			mCriteria = criteria;
		}
		if (mFigureIn.isBlank()) {
			thirdRow.addColumn(new excelCell(mCriteria, ActiveSheet().getTotalColumns(), "text-align:left"));
		} else {
			thirdRow.addColumn(new excelCell(mCriteria, ActiveSheet().getTotalColumns() - 1, "text-align:left"));
			thirdRow.addColumn(new excelCell(mFigureIn, ActiveSheet().getTotalColumns() - 1, "text-align:right"));
		}
		ActiveSheet().htr.add(0, firstRow);
		ActiveSheet().htr.add(1, secondRow);
		ActiveSheet().htr.add(2, thirdRow);

	}

	private void makeExcelHeader() {
		String orgname = "";
		orgname = orgname.replace("<span style='font-size:14px;'>", "").replace("</span >", "")
				.replace("<span style='font-size:13px;'>", "").replace("</span>", "");
		InputStream is;
		/*try {
			ClassPathResource keyResource = new ClassPathResource("static/Emblem_of_Nepal_2020.svg");
			is = keyResource.getInputStream();
			// is = new FileInputStream("src/main/resources/static/images/logo_gov1.gif");
			byte[] bytes = org.apache.commons.compress.utils.IOUtils.toByteArray(is);
			int pictureIdx = wb.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
			is.close();
			CreationHelper helper = wb.getCreationHelper();
			Drawing drawing = getSheet().createDrawingPatriarch();
			ClientAnchor anchor = helper.createClientAnchor();
			anchor.setCol1(0);
			anchor.setRow1(0);
			Picture pict = drawing.createPicture(anchor, pictureIdx);
			pict.resize(1, 1);
			// pict.resize();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		excelRow firstRow = new excelRow("LetterHead");
		excelRow secondRow = new excelRow("LetterHead");
		excelRow thirdRow = new excelRow();

		String mAnusuchi = ActiveSheet().anusuchi;
		if (mAnusuchi.isBlank()) {
			mAnusuchi = anusuchi;
		}
		String mTitle = ActiveSheet().title;
		if (mTitle.isBlank()) {
			mTitle = title;
		}
		String mSubTitle = ActiveSheet().subtitle;
		if (mSubTitle.isBlank()) {
			mSubTitle = subtitle;
		}

		if (mTitle.isBlank()) {
			mTitle = mSubTitle;
		} else {
			if (!mSubTitle.isBlank()) {
				mTitle = mTitle + "<br>" + mSubTitle;
			}
		}

		if (mAnusuchi.isBlank()) {
			firstRow.addColumn(new excelCell(orgname, ActiveSheet().getTotalColumns(), "text-align:center"));
			secondRow.addColumn(new excelCell(mTitle, ActiveSheet().getTotalColumns(), "text-align:center"));

		} else {
			firstRow.addColumn(new excelCell(orgname, ActiveSheet().getTotalColumns() - 1, "text-align:center"));
			firstRow.addColumn(new excelCell(mAnusuchi, 1, 2, "text-align:right;vertical-align:top;"));
			secondRow.addColumn(new excelCell(mTitle, ActiveSheet().getTotalColumns() - 1, "text-align:center"));

		}

		String mFigureIn = ActiveSheet().figurein;
		if (mFigureIn.isBlank()) {
			mFigureIn = figurein;
		}
		String mCriteria = ActiveSheet().criteria;
		if (mCriteria.isBlank()) {
			mCriteria = criteria;
		}
		if (mFigureIn.isBlank()) {
			thirdRow.addColumn(new excelCell(mCriteria, ActiveSheet().getTotalColumns(), "text-align:left"));
		} else {
			thirdRow.addColumn(new excelCell(mCriteria, ActiveSheet().getTotalColumns() - 1, "text-align:left"));
			thirdRow.addColumn(new excelCell(mFigureIn, 1, "text-align:right"));
		}

		ActiveSheet().htr.add(0, firstRow);
		ActiveSheet().htr.add(1, secondRow);
		ActiveSheet().htr.add(2, thirdRow);

	}

	public void insertImage(String filepath, int row, int col, double scaleX, double scaleY) {
		InputStream is;
		try {
			ClassPathResource keyResource = new ClassPathResource(filepath);
			is = keyResource.getInputStream();
			byte[] bytes = org.apache.commons.compress.utils.IOUtils.toByteArray(is);
			int pictureIdx = wb.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
			is.close();
			CreationHelper helper = wb.getCreationHelper();
			Drawing drawing = getSheet().createDrawingPatriarch();
			ClientAnchor anchor = helper.createClientAnchor();
			anchor.setCol1(row);
			anchor.setRow1(col);
			Picture pict = drawing.createPicture(anchor, pictureIdx);
			// test begin

			// text end
			pict.resize(scaleX, scaleY);
			// pict.resize();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String cssTag(String filename) {
		if (filename.isBlank())
			filename = "tblstyle_print.css";
		return "<link href=\"" + "/css/" + filename + "\" rel=\"stylesheet\" type=\"text/css\">";
	}

	private String getCssFiles() {
		StringBuilder sts = new StringBuilder();
		if (cssFiles.contains(",")) {
			String[] styles = cssFiles.split(",");
			for (String file : styles) {
				sts.append(cssTag(file) + "\n");
			}
			return sts.toString();
		}
		return cssTag(cssFiles);
	}

	private String getInlineCss() {
		if (!inlineCss.isBlank()) {
			return "<style>\n" + inlineCss.replace("\"", "\\\"") + "</style>\n";
		}
		return "";
	}

	public String getHtmlDocument() {
		StringBuilder sb = new StringBuilder("<html>\n<head>\n<title>");
		// sb.append(sysService.getInfo().get("Appname"));
		// sb.append("::");
		// sb.append(lService.wds(anusuchi));
		sb.append("</title>\n");
		sb.append(getCssFiles());
		sb.append(getInlineCss());
		sb.append("</head>\n");
		sb.append("<body>\n");
		for (int i = 0; i < Sheets().size(); i++) {
			Sheets(i).select();
			sb.append(getHtmlTable());
		}

		sb.append("</body>\n");
		sb.append("</html>\n");
		return sb.toString();
	}

	public String getHtmlTableOnly() {
		StringBuilder sb = new StringBuilder("<html>\n<head>");

		sb.append("</head>\n");
		sb.append("<body>\n");
		for (int i = 0; i < Sheets().size(); i++) {
			Sheets(i).select();
			sb.append(getHtmlTableNoHeader());
		}
		sb.append("</body>\n");
		sb.append("</html>\n").toString();

		return sb.toString();

	}

	public String getJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONArray header = new JSONArray();
			JSONArray rows = new JSONArray();
			if (ActiveSheet().htr.size() > 0) {
				for (excelRow trs : ActiveSheet().htr) {
					if (trs.data.size() > 0) {
						JSONArray head = new JSONArray();
						for (excelCell tc : trs.data) {
							head.put(tc.data);
						}
						header.put(head);
					}
				}
			}
			if (ActiveSheet().tr.size() > 0) {
				for (excelRow trs : ActiveSheet().tr) {
					if (trs.data.size() > 0) {
						JSONArray row = new JSONArray();
						for (excelCell tc : trs.data) {
							row.put(tc.data);
						}
						rows.put(row);
					}
				}
			}
			obj.put("header", header);
			obj.put("rows", rows);
			return obj.toString();
		} catch (JSONException e) {
			// return e.getMessage();
			return "";
			// e.printStackTrace();
		}
	}

	/**
	 * Creates New Blank Row
	 * 
	 * @return excelRow
	 */

	public excelRow ExcelRow() {
		return new excelRow();
	}

	/**
	 * Creates New Blank Row
	 * 
	 * @return excelRow
	 */
	public excelRow ExcelRow(boolean hidden) {
		return new excelRow(hidden);
	}

	public excelRow ExcelRow(String clas) {
		return new excelRow(clas);
	}

	public excelRow ExcelRow(String clas, String style) {
		return new excelRow(clas, style);
	}

	public class excelRow {
		public String id = "";
		public String clas = "";
		public String style = "";
		public List<excelCell> data = new ArrayList<>();
		public boolean hidden;

		public excelRow() {
			clas = "";
			style = "";
			hidden = false;
		}

		public excelRow(boolean hidden) {
			clas = "";
			style = "";
			this.hidden = hidden;
		}

		public excelRow setId(String id) {
			this.id = id;
			return this;
		}

		public excelRow setClass(String clas) {
			this.clas = clas;
			return this;
		}

		public excelRow setStyle(String style) {
			this.style = style;
			return this;
		}

		public excelRow(String clas) {
			this.clas = clas;
		}

		public excelRow(String clas, String style) {
			this.clas = clas;
			this.style = style;
		}

		public excelRow addColumn(excelCell col) {
			this.data.add(col);
			return this;
		}

		public excelRow addColumn() {
			excelCell col = new excelCell("");
			this.data.add(col);
			return this;
		}

		public excelCell cell(int index) {// Starts from 0
			if (this.data.size() <= index || index < 0) {
				return null;
			} else {
				return this.data.get(index);
			}
		}

		public int cellCount() {
			return this.data.size();
		}

		public String trOpening() {
			return "<tr" + ((!id.equals("")) ? " id=\"" + id + "\"" : "")
					+ ((!clas.equals("")) ? " class=\"" + clas + "\"" : "")
					+ ((!style.equals("")) ? " style=\"" + style.replace("\"", "'") + "\"" : "") + ">";
		}

		public String trClosing() {
			return "</tr>";
		}

	}

	/**
	 * Creates New Balnk Excel Cell
	 * 
	 * @return excelCell
	 */
	public excelCell ExcelCell() {
		return new excelCell();
	}

	/**
	 * Creates New Balnk Excel Cell
	 * 
	 * @return excelCell
	 */
	public excelCell ExcelCell(String data) {
		return new excelCell(data);
	}

	/**
	 * Creates New Balnk Excel Cell
	 * 
	 * @return excelCell
	 */
	public excelCell ExcelCell(String data, String style) {
		return new excelCell(data, style);
	}

	/**
	 * Creates New Balnk Excel Cell
	 * 
	 * @return excelCell
	 */
	public excelCell ExcelCell(String data, int colspan) {
		return new excelCell(data, colspan);
	}

	/**
	 * Creates New Balnk Excel Cell
	 * 
	 * @return excelCell
	 */
	public excelCell ExcelCell(String data, int colspan, int rowspan) {
		return new excelCell(data, colspan, rowspan);
	}

	/**
	 * Creates New Balnk Excel Cell
	 * 
	 * @return excelCell
	 */
	public excelCell ExcelCell(String data, int colspan, String style) {
		return new excelCell(data, colspan, style);
	}

	/**
	 * Creates New Balnk Excel Cell
	 * 
	 * @return excelCell
	 */
	public excelCell ExcelCell(String data, int colspan, int rowspan, String style) {
		return new excelCell(data, colspan, rowspan, style);
	}

	public class excelCell {
		public String id = "";
		public String clas = "";
		public String data = "";
		public String style = "";
		public String formula = "";
		public String dataOption = "";
		public int rowspan = 0;
		public int colspan = 0;

		public excelCell setId(String id) {
			this.id = id;
			return this;
		}

		public excelCell setClass(String clas) {
			this.clas = clas;
			return this;
		}

		public excelCell setStyle(String style) {
			this.style = style;
			return this;
		}

		public excelCell() {
			this.data = "";
		}

		public excelCell(String data) {
			this.data = data;
		}

		public excelCell(String data, String style) {
			this.data = data;
			this.style = style;
		}

		public excelCell(String data, int colspan) {
			this.data = data;
			this.colspan = colspan;
		}

		public excelCell(String data, int colspan, int rowspan) {
			this.data = data;
			this.colspan = colspan;
			this.rowspan = rowspan;
		}

		public excelCell(String data, int colspan, String style) {
			this.data = data;
			this.colspan = colspan;
			this.style = style;
		}

		public excelCell(String data, int colspan, int rowspan, String style) {
			this.data = data;
			this.style = style;
			this.rowspan = rowspan;
			this.colspan = colspan;
		}

		private String cellInternal() {
			return (new StringBuilder()).append((colspan > 1 ? " colspan=\"" + colspan + "\"" : ""))
					.append((rowspan > 1 ? " rowspan=\"" + rowspan + "\"" : ""))
					.append(((!id.equals("")) ? " id=\"" + id + "\"" : ""))
					.append(((!clas.equals("")) ? " class=\"" + clas + "\"" : ""))
					.append(((!style.equals("")) ? " style=\"" + style.replace("\"", "'") + "\"" : "")).toString();
		}

		public String getTh() {
			if (this.dataOption.isBlank()) {
				return "<th" + cellInternal() + ">" + data + "</th>";
			} else {
				// System.out.println(this.dataOption);
				return "<th" + cellInternal() + " data-options=\"" + this.dataOption + "\">" + data + "</th>";
			}
		}

		public String getTd() {
			if (this.dataOption.isBlank()) {
				return "<td" + cellInternal() + ">" + data + "</td>";
			} else {
				return "<td" + cellInternal() + " data-options=\"" + this.dataOption + "\">" + data + "</td>";
			}

			// return "<td" + cellInternal() + ">" + data + "</td>";
		}

		public CellStyle calcStyles(CellStyle cs) {
			if (data.contains("<br>")) {
				cs.setWrapText(true);
			}
			if (style.contains("text-align:center")) {
				cs.setAlignment(HorizontalAlignment.CENTER);
			}
			if (style.contains("text-align:left")) {
				cs.setAlignment(HorizontalAlignment.LEFT);
			}
			if (style.contains("text-align:right")) {
				cs.setAlignment(HorizontalAlignment.RIGHT);
			}
			if (style.contains("text-align:justify")) {
				cs.setAlignment(HorizontalAlignment.JUSTIFY);
			}

			// v-align

			if (style.contains("vertical-align:top")) {
				cs.setVerticalAlignment(VerticalAlignment.TOP);
			}
			if (style.contains("vertical-align:bottom")) {
				cs.setVerticalAlignment(VerticalAlignment.BOTTOM);
			}
			if (style.contains("vertical-align:distributed")) {
				cs.setVerticalAlignment(VerticalAlignment.DISTRIBUTED);
			}
			if (style.contains("vertical-align:center")) {
				cs.setVerticalAlignment(VerticalAlignment.CENTER);
			}
			if (style.contains("vertical-align:justify")) {
				cs.setVerticalAlignment(VerticalAlignment.JUSTIFY);
			}
			if (style.contains("background:")) {
				// cs.setFillBackgroundColor((short)
				// 10);//IndexedColors.GREY_25_PERCENT.getIndex()
				cs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
				cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			}
			return cs;
		}
	}

	public void setBorder() {
		PropertyTemplate pt = new PropertyTemplate();
		try {
			pt.drawBorders(new CellRangeAddress(1, this.rowCount() - 1, 0, this.getTotalColumns() - 1),
					BorderStyle.DOUBLE, IndexedColors.BLACK.getIndex(), BorderExtent.OUTSIDE_HORIZONTAL);
			pt.drawBorders(new CellRangeAddress(1, this.rowCount() - 1, 0, this.getTotalColumns() - 1),
					BorderStyle.DOUBLE, IndexedColors.BLACK.getIndex(), BorderExtent.OUTSIDE_VERTICAL);
			pt.drawBorders(new CellRangeAddress(1, this.rowCount() - 1, 0, this.getTotalColumns() - 1),
					BorderStyle.THIN, IndexedColors.BLACK.getIndex(), BorderExtent.INSIDE_HORIZONTAL);
			pt.drawBorders(new CellRangeAddress(1, this.rowCount() - 1, 0, this.getTotalColumns() - 1),
					BorderStyle.THIN, IndexedColors.BLACK.getIndex(), BorderExtent.INSIDE_VERTICAL);
			pt.applyBorders(getSheet());
		} catch (Exception e) {

		}
	}

	public void setBorder(int startRow, int endRow, int startCol, int endCol, BorderStyle borderType, short color,
			BorderExtent borderDirection) {
		PropertyTemplate pt = new PropertyTemplate();
		try {
			pt.drawBorders(new CellRangeAddress(startRow, endRow, startCol, endCol), borderType, color,
					borderDirection);
			pt.applyBorders(getSheet());
		} catch (Exception e) {

		}
	}
}
