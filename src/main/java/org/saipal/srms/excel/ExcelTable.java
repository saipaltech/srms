package org.saipal.srms.excel;

import java.util.ArrayList;
import java.util.List;

public class ExcelTable {
	public String id = "";
	public String clas = "";
	public String style = "";
	
	public List<ExcelRow> header = new ArrayList<ExcelRow>();
	public List<ExcelRow> data = new ArrayList<>();
	
	public ExcelTable() {
		
	}
	public ExcelTable(String id) {
		this.id= id;
	}
	
	public ExcelTable(String id, String clas) {
		this.id=id;
		this.clas = clas;
	}
	
	
	public ExcelTable (String id, String clas, String style) {
		this.id=id;
		this.clas = clas;
		this.style = style;
	}

	public ExcelTable setId(String id) {
		this.id = id;
		return this;
	}

	public ExcelTable setClass(String clas) {
		this.clas = clas;
		return this;
	}

	public ExcelTable setStyle(String style) {
		this.style = style;
		return this;
	}

	public ExcelTable addHeadRow(ExcelRow row) {
		this.header.add(row);
		return this;
	}

	public ExcelTable addRow(ExcelRow row) {
		this.data.add(row);
		return this;
	}

	private String tableOpening() {
		return "<table" + ((!id.equals("")) ? " id=\"" + id + "\"" : "")
				+ ((!clas.equals("")) ? " class=\"" + clas + "\"" : "")
				+ ((!style.equals("")) ? " style=\"" + style.replace("\"", "'") + "\"" : "") + ">";
	}

	private String tableClosing() {
		return "</table>";
	}

	public String getTable() {
		String html = tableOpening();
		html += prepareTableHead();
		html += prepareTableBody();
		return html + tableClosing();
	}

	private String prepareTableHead() {
		String html = "";
		if (header.size() > 0) {
			html += "\t<thead>\n";
			for (ExcelRow trs : header) {
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

	private String prepareTableBody() {
		String html = "";
		if (data.size() > 0) {
			for (ExcelRow trs : data) {
				if (trs.data.size() > 0) {
					html += "\t" + trs.trOpening() + "\n\t\t";
					for (ExcelCell tc : trs.data) {
						html += tc.getTd();
					}
					html += "\n\t" + trs.trClosing() + "\n";
				}
			}
		}
		return html;
	}
}
