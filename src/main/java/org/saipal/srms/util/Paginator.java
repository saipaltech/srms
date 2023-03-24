package org.saipal.srms.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.saipal.srms.ApplicationContextProvider;

import jakarta.persistence.Tuple;

public class Paginator {
	private String selections;
	private String body;
	private String countFiled;

	private int perPage = 10;
	private int pageNo = 1;
	private int maxPerPage = 100;

	private List<Object> params;
	private String orderField = "";
	private String orderDir = "";

	public Paginator select(String selections) {
		this.selections = selections;
		return this;
	}

	public Paginator sqlBody(String body) {
		this.body = body;
		return this;
	}

	public Paginator setOrderBy(String orderField) {
		this.orderField = orderField;
		return this;
	}

	public Paginator setOrderDir(String orderDir) {
		this.orderDir = orderDir;
		return this;
	}

	public Paginator setPerPage(String perPage) {
		try {
			if (!perPage.isBlank() && !perPage.equals("0")) {
				this.perPage = Integer.parseInt(perPage);
			}
		} catch (NumberFormatException e) {

		}
		return this;
	}

	public Paginator setPageNo(String pageNo) {
		try {
			if (!pageNo.isBlank() && !pageNo.equals("0")) {
				this.pageNo = Integer.parseInt(pageNo);
			}
		} catch (NumberFormatException e) {
			// do nothing
		}
		return this;
	}

	public Paginator setMaxPerPage(int maxPerPage) {
		this.maxPerPage = maxPerPage;
		return this;
	}

	public Paginator setQueryParms(List<Object> params) {
		this.params = params;
		return this;
	}

	public Map<String, Object> paginate() {
		String countSql = "";
		String paginateSql = "";
		Map<String, Object> result = new HashMap<>();
		List<Map<String, Object>> rows = new ArrayList<>();

		DB db = ApplicationContextProvider.getBean(DB.class);
		// total sql
		if (countFiled != null) {
			countSql = "select count(" + countFiled + ") as total " + body;
		} else {
			countSql = "select count(*) as total " + body;
		}
		Tuple totalResp;
		// add perpage & limit
		if (this.perPage > this.maxPerPage) {
			this.perPage = maxPerPage;
		}
		int offset = ((pageNo - 1) * perPage);
		paginateSql = "select " + selections + " " + body;
		if (!orderField.isBlank()) {
			paginateSql += " order by " + orderField + " " + (orderDir.isBlank()?"asc":orderDir);
		}
		paginateSql += " limit " + offset + "," + perPage;
		if (params != null) {
			totalResp = db.getSingleResult(countSql, params);
		} else {
			totalResp = db.getSingleResult(countSql);
		}
		// check if rcord exists or not
		Long totalRecords = 0l;
		if(totalResp!=null) {
			totalRecords = (Long) totalResp.get(0);
			if (totalRecords.compareTo(0l) > 0) {
				if (params != null) {
					rows = db.getResultListMap(paginateSql, params);
				} else {
					rows = db.getResultListMap(paginateSql);
				}
			}
		}
		
		result.put("data", rows);
		result.put("currentPage", pageNo);
		result.put("perPage", perPage);
		result.put("total", totalRecords);
		return result;
	}

}
