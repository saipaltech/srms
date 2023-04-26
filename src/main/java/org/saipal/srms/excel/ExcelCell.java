package org.saipal.srms.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

public class ExcelCell {
	public String id = "";
	public String clas = "";
	public String data = "";
	public String style = "";
	public String formula="";
	public String dataOption="";
	public int rowspan=0;
	public int colspan=0;
	
	public ExcelCell setId(String id) {
		this.id = id;
		return this;
	}
	
	public ExcelCell setClass(String clas) {
		this.clas = clas;
		return this;
	}
	
	public ExcelCell setStyle(String style) {
		this.style = style;
		return this;
	}
	
	public ExcelCell() {
		this.data = "";
	}
	public ExcelCell(String data) {
		this.data = data;
	}

	public ExcelCell(String data, String style) {
		this.data = data;
		this.style = style;
	}

	public ExcelCell(String data, int colspan) {
		this.data = data;
		this.colspan = colspan;
	}

	public ExcelCell(String data, int colspan, int rowspan) {
		this.data = data;
		this.colspan = colspan;
		this.rowspan = rowspan;
	}

	public ExcelCell(String data, int colspan, String style) {
		this.data = data;
		this.colspan = colspan;
		this.style = style;
	}

	public ExcelCell(String data, int colspan, int rowspan, String style) {
		this.data = data;
		this.style = style;
		this.rowspan = rowspan;
		this.colspan = colspan;
	}

	private String cellInternal() {
		return (new StringBuilder())
				.append((colspan > 1 ? " colspan=\"" + colspan + "\"" : ""))
				.append((rowspan > 1 ? " rowspan=\"" + rowspan + "\"" : ""))
				.append(((!id.equals("")) ? " id=\"" + id + "\"" : ""))
				.append(((!clas.equals("")) ? " class=\"" + clas + "\"" : ""))
				.append(((!style.equals("")) ? " style=\"" + style.replace("\"", "'") + "\"" : ""))
				.toString();
	}

	public String getTh() {
		if(this.dataOption.isBlank()) {
			return "<th" + cellInternal() + ">" + data + "</th>";
		}
		else {
			//System.out.println(this.dataOption);
			return "<th" + cellInternal() + " data-options=\""+ this.dataOption + "\">" + data + "</th>";
		}
	}

	public String getTd() {
		if(this.dataOption.isBlank()) {
			return "<td" + cellInternal() + ">" + data + "</td>";
		}
		else {
			return "<td" + cellInternal() + " data-options=\""+ this.dataOption + "\">" + data + "</td>";
		}
		
		//return "<td" + cellInternal() + ">" + data + "</td>";
	}
	
	
	public CellStyle calcStyles(CellStyle cs) {
		if (data.contains("<br>")) {
			cs.setWrapText(true);
		}
		if(style.contains("text-align:center")) {
			cs.setAlignment(HorizontalAlignment.CENTER);
		}
		if(style.contains("text-align:left")) {
			cs.setAlignment(HorizontalAlignment.LEFT);
		}
		if(style.contains("text-align:right")) {
			cs.setAlignment(HorizontalAlignment.RIGHT);
		}
		if(style.contains("text-align:justify")) {
			cs.setAlignment(HorizontalAlignment.JUSTIFY);
		}
		
		//v-align
		
		if(style.contains("vertical-align:top")) {
			cs.setVerticalAlignment(VerticalAlignment.TOP);
		}
		if(style.contains("vertical-align:bottom")) {
			cs.setVerticalAlignment(VerticalAlignment.BOTTOM);
		}
		if(style.contains("vertical-align:distributed")) {
			cs.setVerticalAlignment(VerticalAlignment.DISTRIBUTED);
		}
		if(style.contains("vertical-align:center")) {
			cs.setVerticalAlignment(VerticalAlignment.CENTER);
		}
		if(style.contains("vertical-align:justify")) {
			cs.setVerticalAlignment(VerticalAlignment.JUSTIFY);
		}
		if(style.contains("background:")) {
			// cs.setFillBackgroundColor((short) 10);//IndexedColors.GREY_25_PERCENT.getIndex()
			cs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			 cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		}
		return cs;
	}
}
