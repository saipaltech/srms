package org.saipal.srms.excel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
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
import org.apache.poi.ss.usermodel.IndexedColors;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

public class ExcelReport {

	public String title = "";
	public String subtitle = "";
	public String fy = "";
	public String fyYear = "";
	public String anusuchi = "";
	public String filename = "";
	public String orgid = "";
	public String figurein = "";
	public String inlineCss = "";
	public String cssFiles = "tblstyle_print.css";
	public String criteria="";
	//Added on 11 Feb 2021 Begin
	public String id="";
	public String clas="";
	public String dataOption="";
	public String tableTitle="";
	public String toolbar="";
	//Added on 11 Feb 2021 End
	private int headRowIndex = 0;
	private int skipRow = 0;
	// private int skipCol = 0;
	private List<Integer> hiddenColumnIndex = new ArrayList<>();
	Logger log = LoggerFactory.getLogger(ExcelReport.class);
	Map<Integer, Integer> indexMap = new HashMap<>();

	public void addHiddenColumnIndex(int colIndex) {
		hiddenColumnIndex.add(colIndex);
	}
	private Map<Integer, Integer> skipCol = new HashMap<>();
	public String defaultSheetName = "Sheet1";
	public Workbook wb = new XSSFWorkbook();
	public int totalColumns;
	public int headRowCount = 0;
	public List<ExcelRow> tr = new ArrayList<>();
	public List<ExcelRow> htr = new ArrayList<>();

	private Map<Integer,Map<String,String>> figureIn=new HashMap<Integer,Map<String,String>>();
	
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
	
	public ExcelReport(int totlColumns) {
		this.totalColumns = totlColumns;
		this.wb.createSheet(defaultSheetName);
		Map<String,String> fin1=new HashMap<String,String>();
		fin1.put("NP", "");
		fin1.put("EN", "");
		figureIn.put(1, fin1);
		
		Map<String,String> fin10=new HashMap<String,String>();
		fin10.put("NP", "रु. दसमा");
		fin10.put("EN", "In Ten");
		figureIn.put(10, fin10);
		
		Map<String,String> fin100=new HashMap<String,String>();
		fin100.put("NP", "रु. सयमा");
		fin100.put("EN", "In Hundred");
		figureIn.put(100, fin100);
		
		
		Map<String,String> fin1000=new HashMap<String,String>();
		fin1000.put("NP", "रु. हजारमा");
		fin1000.put("EN", "In Thousand");
		figureIn.put(1000, fin1000);
		
		Map<String,String> fin100000=new HashMap<String,String>();
		fin100000.put("NP", "रु. लाखमा");
		fin100000.put("EN", "In Hundred Thousand");
		figureIn.put(100000, fin100000);
		
		Map<String,String> fin1000000=new HashMap<String,String>();
		fin1000000.put("NP", "रु. दसलाखमा");
		fin1000000.put("EN", "In Million");
		figureIn.put(1000000, fin1000000);
		
		Map<String,String> fin10000000=new HashMap<String,String>();
		fin10000000.put("NP", "रु. करोडमा");
		fin10000000.put("EN", "In Ten Million");
		figureIn.put(10000000, fin10000000);
		
		Map<String,String> fin100000000=new HashMap<String,String>();
		fin100000000.put("NP", "रु. दसकरोडमा");
		fin100000000.put("EN", "In Hundred Millioin");
		figureIn.put(100000000, fin100000000);
		
		Map<String,String> fin1000000000=new HashMap<String,String>();
		fin1000000000.put("NP", "रु. अरबमा");
		fin1000000000.put("EN", "In Billion");
		figureIn.put(1000000000, fin1000000000);
		
		
	}

	public ExcelReport() {
		this.totalColumns = 1;
		this.wb.createSheet(defaultSheetName);
		Map<String,String> fin1=new HashMap<String,String>();
		fin1.put("NP", "");
		fin1.put("EN", "");
		figureIn.put(1, fin1);
		
		Map<String,String> fin10=new HashMap<String,String>();
		fin10.put("NP", "रु. दसमा");
		fin10.put("EN", "In Ten");
		figureIn.put(10, fin10);
		
		Map<String,String> fin100=new HashMap<String,String>();
		fin100.put("NP", "रु. सयमा");
		fin100.put("EN", "In Hundred");
		figureIn.put(100, fin100);
		
		
		Map<String,String> fin1000=new HashMap<String,String>();
		fin1000.put("NP", "रु. हजारमा");
		fin1000.put("EN", "In Thousand");
		figureIn.put(1000, fin1000);
		
		Map<String,String> fin100000=new HashMap<String,String>();
		fin100000.put("NP", "रु. लाखमा");
		fin100000.put("EN", "In Hundred Thousand");
		figureIn.put(100000, fin100000);
		
		Map<String,String> fin1000000=new HashMap<String,String>();
		fin1000000.put("NP", "रु. दसलाखमा");
		fin1000000.put("EN", "In Million");
		figureIn.put(1000000, fin1000000);
		
		Map<String,String> fin10000000=new HashMap<String,String>();
		fin10000000.put("NP", "रु. करोडमा");
		fin10000000.put("EN", "In Ten Million");
		figureIn.put(10000000, fin10000000);
		
		Map<String,String> fin100000000=new HashMap<String,String>();
		fin100000000.put("NP", "रु. दसकरोडमा");
		fin100000000.put("EN", "In Hundred Millioin");
		figureIn.put(100000000, fin100000000);
		
		Map<String,String> fin1000000000=new HashMap<String,String>();
		fin1000000000.put("NP", "रु. अरबमा");
		fin1000000000.put("EN", "In Billion");
		figureIn.put(1000000000, fin1000000000);
		
	}

	public String getFigureIn(int figure,String lang) {
		lang=lang.toUpperCase();
		return figureIn.get(figure).get(lang).toString();
	}
	public String getFigureIn(String figure,String lang) {
		if(figure.isBlank()) {
			figure="1";
		}
		if(isNumeric(figure)) {
			return getFigureIn(Integer.parseInt(figure),lang);
		}
		else {
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
		wb.createSheet(defaultSheetName);
	}

	public ExcelReport addRow(ExcelRow tr) {
		this.tr.add(tr);
		return this;
	}

	public ExcelReport addHeadRow(ExcelRow tr) {
		this.htr.add(tr);
		return this;
	}
	
	public ExcelRow insertRow(int index) {
		int hrow=this.htr.size();
		int drow=this.tr.size();
		int tindex=index;
		ExcelRow tr=new ExcelRow();
		if(index<0) {
			return null;
		}
		
		if(index<hrow-1) {
			this.htr.add(index,tr);
		}
		else {
			index=index-(hrow);
			if(index<drow) {
				this.tr.add(index,tr);
			}
			else {
				return null;
			}
		}
		return this.row(tindex);
	}

	public void deleteRow(int index) {
		if(index<0) {
			return;
		}
		if(index<this.htr.size()) {
			this.htr.remove(index);
		}
		else {
			index=index-(this.htr.size());
			if(index<this.tr.size()) {
				this.tr.remove(index);
			}
		}
		
		
	}
	
	public Workbook getExcel() {
		return getExcel(true);
	}
	
	//Added on 11 Feb 20201 Begin
	public ExcelRow row(int index) {
		int totalRow=this.htr.size()+this.tr.size();
		if(totalRow<=index || index<0) {
			return null;
		}
		else {
			if(index<this.htr.size()) {
				return this.htr.get(index);
			}
			else {
				index=index-this.htr.size();
				return this.tr.get(index);
			}
		}
	}
	
	public int rowCount() {
		return (this.htr.size()+this.tr.size());
	}
	
	public ExcelCell cell(int rowIndex,int colIndex) {
		ExcelRow r=this.row(rowIndex);
		if(r==null) {
			return null;
		}
		else {
			return r.cell(colIndex);
		}
	}
	
	//Added on 11 Feb 20201 Begin
	public Workbook getExcel(boolean headerRequired) {
		if (headerRequired) {
			makeExcelHeader();
		}
		if (htr.size() > 0) {
			this.prepareHeadRows();
		}
		if (tr.size() > 0) {
			this.prepareRows();
		}
		this.setBorder();
		return wb;
	}

	public Workbook getExcelTest() {
		// makeExcelHeader();

		this.prepareRowsTest();

		return wb;
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

	private void prepareRows() {
		Sheet sh = getSheet();
		if (hiddenColumnIndex.size() > 0) {
			for (int i : hiddenColumnIndex) {
				sh.setColumnHidden(i, true);
			}
		}
		int rowIndex = htr.size(), colIndex = 0;
		for (ExcelRow trs : tr) {
			if (trs.data.size() > 0) {
				Row row = sh.createRow(rowIndex);
				if (trs.hidden) {
					row.setZeroHeight(true);
				}
				int cellIndex = 0;
				for (ExcelCell tc : trs.data) {
					for (int i = cellIndex; i < getTotalColumns(); i++) {
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
		if (hiddenColumnIndex.size() > 0) {
			for (int i : hiddenColumnIndex) {
				sh.setColumnHidden(i, true);
			}
		}
		int rowIndex = htr.size(), colIndex = 0;
		for (ExcelRow trs : tr) {
			if (trs.data.size() > 0) {
				Row row = sh.createRow(rowIndex);
				if (trs.hidden) {
					row.setZeroHeight(true);
				}
				int cellIndex = 0;
				for (ExcelCell tc : trs.data) {
					for (int i = cellIndex; i < getTotalColumns(); i++) {
						if (hasNext(i)) {
							colIndex++;
						} else {
							cellIndex++;
							break;
						}
					}
					colIndex = getNextEmptyIndex(colIndex, rowIndex);
					Cell cell = row.createCell(colIndex);
					log.info(tc.data + ":" + rowIndex + ":" + colIndex);
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
							indexMap.put(i, tc.rowspan + rowIndex - 1);
						sh.addMergedRegion(cra);
						colIndex += tc.colspan - 1;
					} else if (tc.rowspan > 1) {
						indexMap.put(colIndex, tc.rowspan + rowIndex - 1);
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
		while (indexMap.get(index) != null && indexMap.get(index) >= rowindex) {
			index++;
		}
		return index;
	}

	private void prepareHeadRows() {
		Sheet sh = getSheet();
		int rowIndex = headRowIndex, colIndex = 0;
		Map<String,Integer>RowSpan=new HashMap<String,Integer>();
		for (ExcelRow trs : htr) {
			if (trs.data.size() > 0) {
				Row row = sh.createRow(rowIndex);
				int maxRowSpan = 0;
				for (ExcelCell tc : trs.data) {
					//RowSpan Begin
					int trspan=tc.rowspan;
					int tcspan=tc.colspan;
					if(trspan<=0) {
						trspan=1;
					}
					if(tcspan<=0) {
						tcspan=1;
					}
					int i=0;
					
					if(rowIndex>headRowIndex) {
						i=colIndex;
						int mapIndex=1;
						try {
							mapIndex=RowSpan.get("R"+colIndex);
						}
						catch(Exception e) {
							mapIndex=1;
						}
						while(mapIndex>1) {
							RowSpan.put("R"+colIndex, (mapIndex-1));
							colIndex++;
							try {
							mapIndex=RowSpan.get("R"+colIndex);
						}
						catch(Exception e) {
							mapIndex=1;
						}
							}
					}
					
					Cell cell = row.createCell(colIndex);
					String text = tc.data;
					text = cleanData(text);
					
					for(i=0;i<tcspan;i++) {
						RowSpan.put("R"+(i+colIndex), trspan);
					}
					
					if(trs.clas.isBlank()) {
						if(tc.style.isBlank()) {
							tc.style="text-align:center;vertical-align:center; background:#ccc";
						}
					}
					
					
					/*
					 * if (!util.request("viewtype").equals("3")) { // htmlreport
					 * html=html.replace("<div class=\"figurein\">", "").replace("</div>", ""); }
					 */

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
				
				//Added for RowSpan Management Begin
				if(colIndex<=this.getTotalColumns()-1) {
					for(int i=colIndex;i<this.getTotalColumns();i++) {
						int mapIndex=1;
						try {
						mapIndex=RowSpan.get("R"+i);
						}
						catch(Exception e) {
							mapIndex=1;
						}
						mapIndex--;
						if(mapIndex<1) {
							mapIndex=1;
						}
						RowSpan.put("R"+colIndex,mapIndex);
					}
				}
				//Added for RowSpan Management End
				colIndex = 0;
				if (maxRowSpan > 1) {
					rowIndex ++;//= maxRowSpan;
					skipRow += maxRowSpan;
				} else {
					rowIndex++;
				}
			}
		}
	}

	public String getHtmlTable() {
		makeHtmlHeader();
		
		/*
		 public String id="";
		public String clas="";
		public String dataOption="";
		public String tableTitle=""; 
		 
		 */
		String tblSetting="";
		if(this.id.isBlank()) {
			tblSetting=" id=\"row_head\"";
		}
		else {
			tblSetting=" id=\""+this.id+"\"";
		}
		
		if(this.clas.isBlank()) {
			tblSetting= tblSetting+" class=\"rtable\"";
		}
		else {
			tblSetting=tblSetting+" class=\""+this.clas+"\"";
		}
		
		if(!this.dataOption.isBlank()) {
			tblSetting=tblSetting+" data-options=\""+this.dataOption+"\"";
		}
		
		if(!this.tableTitle.isBlank()) {
			tblSetting=tblSetting+" title=\""+this.tableTitle+"\"";
		}
		
		if(!this.toolbar.isBlank()) {
			tblSetting=tblSetting+" toolbar=\""+this.toolbar+"\"";
		}
		
		//String html = "<table calss=\"rtable\" id=\"row_head\">\n";
		String html="<table "+tblSetting+">\n";
		html += prepareHtmlHead();
		html += prepareHtmlBody();
		html += "</table>";
		return html;
	}
	public String getHtmlTableNoHeader() {	
		StringBuilder html = new StringBuilder();
		String tblSetting="";
		if(this.id.isBlank()) {
			tblSetting=" id=\"row_head\"";
		}
		else {
			tblSetting=" id=\""+this.id+"\"";
		}
		
		if(this.clas.isBlank()) {
			tblSetting= tblSetting+" class=\"rtable\"";
		}
		else {
			tblSetting=tblSetting+" class=\""+this.clas+"\"";
		}
		
		if(!this.dataOption.isBlank()) {
			tblSetting=tblSetting+" data-options=\""+this.dataOption+"\"";
		}
		
		if(!this.tableTitle.isBlank()) {
			tblSetting=tblSetting+" title=\""+this.tableTitle+"\"";
		}
		
		if(!this.toolbar.isBlank()) {
			tblSetting=tblSetting+" toolbar=\""+this.toolbar+"\"";
		}
		//html.append("<table calss=\"rtable\" id=\"row_head\">\n");
		html.append("<table "+tblSetting+">\n");
		html.append(prepareHtmlHead());
		html.append(prepareHtmlBody());
		html.append("</table >\n");		
		return html.toString();
	}

	private String prepareHtmlHead() {
		String html = "";
		if (htr.size() > 0) {
			html += "\t<thead>\n";
			for (ExcelRow trs : htr) {
				if (trs.data.size() > 0) {
					html += "\t\t" + trs.trOpening() + "\n\t\t\t";
					for (ExcelCell tc : trs.data) {
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
		if (tr.size() > 0) {
			for (ExcelRow trs : tr) {
				if (trs.data.size() > 0) {
					html.append("\t" + trs.trOpening() + "\n\t\t");
					for (ExcelCell tc : trs.data) {
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
		
		ExcelRow firstRow = new ExcelRow("LetterHead");
		ExcelRow secondRow = new ExcelRow("LetterHead");

		ExcelRow thirdRow = new ExcelRow();
		// ExcelRow fourthRow = new ExcelRow();
		// ExcelRow thirdRow = new ExcelRow("LetterHead");
		// ExcelRow fourthRow = new ExcelRow("LetterHead");
		
		StringBuilder logoInfo = new StringBuilder();
		
		logoInfo.append("<div class=\"logo\">") 
				.append("<img src=\"")
				//.append(session.session("baseUrl"))
				.append("/images/nepal-gov-logo.png\"></div>")
				.append("<div class=\"anusuchi\" contenteditable='true'>")
				//.append(lService.wds(anusuchi))
				.append("</div>")
				.append("<div class=\"orginfo\">") 
				.append(orgname)
				.append("</div>")
				.append("<br><div contenteditable='true'>")
				.append("<div calss=\"title\">") 
				.append(title) 
				.append("</div></div>");
		
		if (!subtitle.isBlank())
			logoInfo.append("<div contenteditable='true'>")
					.append("<div calss=\"subtitle\">")
					.append(subtitle)
					.append("</div></div>");

		//log.info(logoInfo);
		firstRow.addColumn(new ExcelCell(logoInfo.toString(), getTotalColumns()));
		secondRow.addColumn(new ExcelCell("<div contenteditable=\"true\">&nbsp;</div>", getTotalColumns()));
		thirdRow.addColumn(new ExcelCell(" ", getTotalColumns(), "text-align:center"));
		// String odetails = lService.wds("FY") + " " + lService.tNum(fyYear) + "<div
		// class=\"figurein\">"
		// + lService.wds(figurein) + "</div>";
		// fourthRow.addColumn(new ExcelCell(odetails, getTotalColumns(),
		// "text-align:left;padding-left:5px;"));
		htr.add(0, firstRow);
		htr.add(1, secondRow);
		htr.add(2, thirdRow);
		// htr.add(3, fourthRow);
		// thirdRow.addColumn(new ExcelCell(" ", getTotalColumns(), "text-align:center"));
		// String odetails = "<div class=\"figurein\">"+ lService.wds(figurein) +
		// "</div>";
		// thirdRow.addColumn(new ExcelCell(odetails, getTotalColumns(),
		// "text-align:left;padding-left:5px;"));
		// htr.add(0, firstRow);
		// htr.add(1, secondRow);
		// htr.add(2, thirdRow);
		// htr.add(3, fourthRow);
	}

	private void makeExcelHeader() {
		String orgname = "";
		//if (orgid.equals("")) {
		//	orgname = rlservice.getOrgInfoText();
		//} else {
		//	orgname = rlservice.getOrgInfoText(orgid);
		//}
		orgname = orgname
				.replace("<span style='font-size:14px;'>", "")
				.replace("</span >", "")
				.replace("<span style='font-size:13px;'>", "")
				.replace("</span>", "");
		InputStream is;
		try {
			ClassPathResource keyResource = new ClassPathResource("static/images/nepal-gov-logo.png");
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
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}

		ExcelRow firstRow = new ExcelRow("LetterHead");
		ExcelRow secondRow = new ExcelRow("LetterHead");
		ExcelRow thirdRow = new ExcelRow("LetterHead");
		
		String mAnusuchi=anusuchi;
		
		String mTitle=title;
		
		String mSubTitle=subtitle;
		
		if(mTitle.isBlank()) {
			mTitle=mSubTitle;
		}
		else {
			if(!mSubTitle.isBlank()) {
				mTitle=mTitle+"<br>"+mSubTitle;
			}
		}
		
		if(mAnusuchi.isBlank()) {
			firstRow.addColumn(new ExcelCell(orgname, this.getTotalColumns(), "text-align:center"));
			secondRow.addColumn(new ExcelCell(mTitle, this.getTotalColumns(), "text-align:center"));
			
			}
			else {
				firstRow.addColumn(new ExcelCell(orgname, this.getTotalColumns()-1, "text-align:center"));
				firstRow.addColumn(new ExcelCell(mAnusuchi, 1,2,"text-align:right;vertical-align:top;"));
				secondRow.addColumn(new ExcelCell(mTitle, this.getTotalColumns()-1, "text-align:center"));
				
			}
		
		String mFigureIn=this.figurein;
		if(mFigureIn.isBlank()) {
			mFigureIn=figurein;
		}
		String mCriteria=this.criteria;
		if(mCriteria.isBlank()) {
			mCriteria=criteria;
		}
		if(mFigureIn.isBlank()) {
			thirdRow.addColumn(new ExcelCell(mCriteria, this.getTotalColumns(), "text-align:left"));
		}
		else {
			thirdRow.addColumn(new ExcelCell(mCriteria, this.getTotalColumns()-1, "text-align:left"));
			thirdRow.addColumn(new ExcelCell(mFigureIn, 1, "text-align:right"));
		}
		
		//firstRow.addColumn(new ExcelCell(orgname, getTotalColumns(), "text-align:center"));
		//secondRow.addColumn(new ExcelCell(mTitle, getTotalColumns()));
		//thirdRow.addColumn(new ExcelCell("", getTotalColumns(), "text-align:center"));
		
		htr.add(0, firstRow);
		htr.add(1, secondRow);
		htr.add(2, thirdRow);
		
	}

	private String cssTag(String filename) {
		if (filename.isBlank())
			filename = "tblstyle_print.css";
		return "<link href=\""  + "/css/report/" + filename
				+ "\" rel=\"stylesheet\" type=\"text/css\">";
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
		return  sb.append("")
				.append("::") 
				//.append(lService.wds(anusuchi))
				.append("</title>\n")
				.append(getCssFiles())
				.append(getInlineCss()) 
				.append("</head>\n")
				.append("<body>\n") 
				.append(getHtmlTable())
				.append("</body>\n") 
				.append("</html>\n").toString();
	}
	public String getHtmlTableOnly() {
		StringBuilder sb = new StringBuilder("<html>\n<head>");
		return  sb
				//.append(sysService.getInfo().get("Appname"))
				//.append("::") 
				//.append(lService.wds(anusuchi))
				//.append("</title>\n")
				//.append(getCssFiles())
				//.append(getInlineCss()) 
				.append("</head>\n")
				.append("<body>\n") 
				.append(getHtmlTableNoHeader())
				.append("</body>\n") 
				.append("</html>\n").toString();
	}

	public String getJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONArray header = new JSONArray();
			JSONArray rows = new JSONArray();
			if (htr.size() > 0) {
				for (ExcelRow trs : htr) {
					if (trs.data.size() > 0) {
						JSONArray head = new JSONArray();
						for (ExcelCell tc : trs.data) {
							head.put(tc.data);
						}
						header.put(head);
					}
				}
			}
			if (tr.size() > 0) {
				for (ExcelRow trs : tr) {
					if (trs.data.size() > 0) {
						JSONArray row = new JSONArray();
						for (ExcelCell tc : trs.data) {
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
	public void setBorder() {
		PropertyTemplate pt = new PropertyTemplate(); 
		try {
		pt.drawBorders(new CellRangeAddress(3, this.rowCount()-1, 0, this.getTotalColumns()-1), BorderStyle.DOUBLE,IndexedColors.BLACK.getIndex(), BorderExtent.OUTSIDE_HORIZONTAL);
		pt.drawBorders(new CellRangeAddress(3, this.rowCount()-1, 0, this.getTotalColumns()-1), BorderStyle.DOUBLE,IndexedColors.BLACK.getIndex(), BorderExtent.OUTSIDE_VERTICAL);
		pt.drawBorders(new CellRangeAddress(3, this.rowCount()-1, 0, this.getTotalColumns()-1), BorderStyle.THIN,IndexedColors.BLACK.getIndex(), BorderExtent.INSIDE_HORIZONTAL);
		pt.drawBorders(new CellRangeAddress(3, this.rowCount()-1, 0, this.getTotalColumns()-1), BorderStyle.THIN,IndexedColors.BLACK.getIndex(), BorderExtent.INSIDE_VERTICAL);
		pt.applyBorders(getSheet());
		}
		catch(Exception e) {
			
		}
	}
	
	public void setBorder(int startRow,int endRow, int startCol,int endCol,BorderStyle borderType,short color,BorderExtent borderDirection) {
		PropertyTemplate pt = new PropertyTemplate(); 
		try {
		pt.drawBorders(new CellRangeAddress(startRow, endRow, startCol, endCol), borderType,color, borderDirection);
		pt.applyBorders(getSheet());
		}
		catch(Exception e) {
			
		}
	}
	
	public int getTotalColumns() {
		if(totalColumns<=1) {
			if(tr.size()>0) {
				return tr.get(0).cellCount();
			}
			else {
				if(htr.size()>0) {
					return htr.get(htr.size()-1).cellCount();
				}
				else {
					return 0;
				}
			}
		}
		else {
			return totalColumns;
		}
	}
	
	public void insertImage(String filepath,int row,int col,double scaleX,double scaleY) {
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
			//test begin
			
			//text end
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

}
