package org.saipal.srms.excel;

import java.util.ArrayList;
import java.util.List;

public class ExcelRow {
	public String id = "";
	public String clas = "";
	public String style = "";
	public List<ExcelCell> data = new ArrayList<>();
	public boolean hidden;

	public ExcelRow() {
		clas = "";
		style = "";
		hidden = false;
		}

	public ExcelRow(boolean hidden) {
		clas = "";
		style = "";
		this.hidden = hidden;
	}

	public ExcelRow setId(String id) {
		this.id = id;
		return this;
	}

	public ExcelRow setClass(String clas) {
		this.clas = clas;
		return this;
	}

	public ExcelRow setStyle(String style) {
		this.style = style;
		return this;
	}

	public ExcelRow(String clas) {
		this.clas = clas;
	}

	public ExcelRow(String clas, String style) {
		this.clas = clas;
		this.style = style;
	}

	public ExcelRow addColumn(ExcelCell col) {
		this.data.add(col);
		return this;
	}

	public ExcelRow addColumn() {
		ExcelCell col = new ExcelCell("");
		this.data.add(col);
		return this;
	}
	
	public ExcelCell cell(int index) {//Starts from 0
		if(this.data.size()<=index || index<0) {
			return null;
		}
		else {
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
