package org.saipal.srms.parser;

import java.util.ArrayList;
import java.util.List;

public class DataGridRow {
	List<String> values;
	List<String> keys;

	public DataGridRow() {
		values = new ArrayList<>();
		keys = new ArrayList<>();
	}

	public void addValue(String key, String value) {
		values.add(value);
		keys.add(key);
	}

	public String getValue(String key) {
		return values.get(keys.indexOf(key));
	}

	public String getValue(int colIndex) {
		return values.get(colIndex);
	}
	public List<String> getKeys(){
		return this.keys;
	}
}
