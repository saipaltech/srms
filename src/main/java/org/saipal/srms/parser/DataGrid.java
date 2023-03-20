package org.saipal.srms.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataGrid {

	private String id;
	private RequestParser rp;
	private static final Logger LOG = LoggerFactory.getLogger(DataGrid.class);

	// public List<List<List<String>>> values;
	public List<DataGridRow> dataGridRows;
	public int totalRows = 0;
	public int totalCols = 0;
	private int index = -1;
	private List<String> columns;
	private List<String> datatype;
	String valuesTemp;

	public DataGrid(String gridName, String columns, String datatype) {
		this.id = gridName;
		this.columns = Arrays.asList(columns.split(","));
		this.datatype = Arrays.asList(datatype.split(","));
	}
	private void raiseError(String msg) throws Exception {
		throw new Exception(msg); 
	}
	public DataGrid parseGrid() {
		dataGridRows = new ArrayList<>();
		String values = rp.getElementById(this.id + "_value").value;
		valuesTemp = values;
 
		DataGridRow dgr;
		if (!values.isEmpty()) {
			//0#0#
			String[] vals = values.split("#");
			this.totalRows = Integer.parseInt(vals[0]);
			this.totalCols = Integer.parseInt(vals[1]);
			
			if (this.totalRows > 0)
			{
				this.index = 0;
			String labelValues = vals[2];
			String[] valueList = labelValues.split(";");
			// this.values = new ArrayList<List<List<String>>>();
			
			for (int i = 0; i < this.totalRows; i++) {
				dgr = new DataGridRow();
				List<List<String>> temp = new ArrayList<List<String>>();
				String[] value = valueList[i].split(",");
				for (int j = 0; j < this.columns.size(); j++) {
					dgr.addValue(this.columns.get(j), value[j]);
				
					if (this.datatype.size() <= j)
						temp.add(Arrays.asList(this.columns.get(j), value[j], "str"));
					else
						temp.add(Arrays.asList(this.columns.get(j), value[j], this.datatype.get(j)));

				}
				dataGridRows.add(dgr);
				// this.values.add(temp);
			}
			}
		}
		return this;
	}

	private int getColIndex(String colname) {
		int ret=this.columns.indexOf(colname);
		if(ret<0)
			try {
				raiseError("Invalid columnname:'"+colname+"'");
			} catch (Exception e) {
				e.printStackTrace();
			}
		return ret;
	}

	private String mapDataType(String dt) {
		if (dt.equalsIgnoreCase("int")) {
			return "numeric(24,2)";
		} else if (dt.equalsIgnoreCase("date")) {
			return "datetime";
		} else {
			return "nvarchar(1000)";
		}
	}

	public RequestParser getRp() {
		return rp;
	}

	public void setRp(RequestParser rp) {
		this.rp = rp;
	}

	public String getId() {
		return id;
	}

	public String getValue(int rowid, int colindex) {
		return dataGridRows.get(rowid).getValue(colindex);
		// return this.values.get(rowid).get(colindex).get(1);
	}

	public String getValue(int rowid, String colname) {

		return getValue(rowid, getColIndex(colname));
	}

	public String getValue(int colindex) {

		if (this.index >= 0 && this.index < this.totalRows) {
			return dataGridRows.get(this.index).getValue(colindex);
		} else {
			return "";
		}

	}

	public String getValue(String colname) {
		if (this.index >= 0 && this.index < this.totalRows) {
			return getValue(this.index, getColIndex(colname));
		} else {
			return "";
		}
	}

	public void moveto(int i) {
		if (i >= 0 && i < this.totalRows)
			this.index = i;
	}

	public void moveNext() {
		if (this.index < this.totalRows)
			this.index++;
	}

	public void movePrevious() {
		if (this.index >= 0)
			this.index--;
	}

	public void moveFirst() {
		if (this.totalRows > 0)
			this.index = 0;
	}

	public void moveLast() {
		if (this.totalRows > 0)
			this.index = this.totalRows - 1;
	}

	public boolean BOF() {
		if (this.index < 0)
			return true;
		else
			return false;
	}

	public boolean EOF() {
		if (this.index >= this.totalRows)
			return true;
		else
			return false;
	}

	public String getDataType(int colindex) {
		return this.datatype.get(colindex);
	}

	public String getDataType(String colname) {
		return this.datatype.get(getColIndex(colname));
	}

	public String getColname(int rowid, int colid) {
		return dataGridRows.get(rowid).getKeys().get(colid);
		// return this.values.get(rowid).get(colid).get(0);
	}

	public String getColString(String colName) {
		String result = "";
		int colIndex = getColIndex(colName);
		for (int i = 0; i < totalRows; i++) {
			result += "'" + dataGridRows.get(i).getValue(colIndex) + "',";
		}
		if (!result.isEmpty()) {
			return result.substring(0, result.length() - 1);
		} else {
			result = "-1";
		}
		return result;
	}

//	public String makeTable(DB db, String sessionId) {
//		//Needs to update for insert into sql for datatime datatype
//		String tableName = "##" + this.id + "_" + db.newIdInt();
//		db.executeUpdate("exec dbo.Droptemptable " + tableName);
//		String sql = "create table " + tableName + "(jiiid numeric identity, ";
//		//System.out.println("makeTable=>"+this.columns.size());
//		for (int i = 0; i < this.columns.size(); i++) {
//			sql += this.columns.get(i) + " " + mapDataType(this.datatype.get(i)) + ", ";
//		}
//		sql = sql.substring(0, sql.length() - 2) + " )";
//
//		 //System.out.println("makeTable"+sql);
//		db.executeUpdate(sql);
//		StringBuilder sql1 = new StringBuilder();
//		if (!this.rp.getElementById(this.id + "_value").value.isEmpty() && this.totalRows == 0) {
//			this.parseGrid();
//		}
//		if (this.totalRows > 0 && this.totalCols > 0) {
//			List<String> args = new ArrayList<String>();
//			
//			// populate data to table
//			for (int i = 0; i < this.totalRows; i++) {
//				sql = "";
//				sql += "insert into " + tableName + " values(";
//				for (int j = 0; j < this.totalCols; j++) {
//					//sql += "?, ";
//					String val = getValue(i, j);
//					
//					if (val.isBlank() && datatype.get(j).equals("int")) {
//						val = "0";
//					} else if (val.isBlank() && datatype.get(j).equals("date")) {
//						val = null;
//					}
//					else {
//						val="N'"+ val.replace("'", "''") +"'";
//					}
//					//args.add(val);
//					if(j==0) {
//					sql+=val;
//					}
//					else {
//						sql+=","+val;
//					}
//				}
//				sql+=")\n";
//				sql1.append(sql);
//				//sql = sql.substring(0, sql.length() - 2) + " ),";
//			}
//			//sql = sql.substring(0, sql.length() - 1);
//			
//			// System.out.println("makeTable sql\n\n\n"+sql1 +"\n"+args+"\n\n");
//			Map<String,Object> err=db.execute(sql1.toString());
//			if((int)err.get("errorNumber")!=0) {
//				System.out.println("\n"+err.get("message").toString()+"\n");
//			}
//			//db.executeUpdate(sql1.toString());
//		}
//		return tableName;
//	}

	public double getSum(String colName) {
		Double sum = 0.0;
		int colIndex = getColIndex(colName);
		// //System.out.println(totalRows);
		for (int i = 0; i < totalRows; i++) {
			// //System.out.println(i);
			String v=getValue(i, colIndex);
			sum += Double.parseDouble(v);
		}
		return sum;
	}

	public double getSum(int colIndex) {
		Double sum = 0.0;
		// //System.out.println(totalRows);
		for (int i = 0; i < totalRows; i++) {
			// //System.out.println(i);
			sum += Double.parseDouble(getValue(i, colIndex));
		}
		return sum;
	}

//	public void addRow(String value, int index) {
//		rp.addGridRow(id, value, index + "");
//	}

	public int getRowById(String rowId) {
		for (int i = 0; i < totalRows; i++) {
			if (dataGridRows.get(i).getValue("id").equals(rowId))
				return i;
		}
		throw new RuntimeException("Column not found");
	}

}
