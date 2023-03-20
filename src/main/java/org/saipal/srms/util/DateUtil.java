package org.saipal.srms.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DateUtil{

	/*
	 * format of date in sutra java english date format input mm/dd/yyyy nepali date
	 * format input yyyy/mm/dd
	 */

	DB db;
	
	public DateUtil(DB db) {
		this.db=db;
	}
	private static final Logger LOG = LoggerFactory.getLogger(DateUtil.class);

	/**
	 * Converts the given nepali date into english. Can accept null as input
	 * 
	 * @param nepDate String format YYYY/MM/DD or null
	 * @return String format MM/DD/YYYY
	 */
	public String getEngDate(String nepDate) {
		String date;
		Tuple t;
		if (nepDate != null && !nepDate.isEmpty()) {

			String sql = "select cast(dbo.toDate(?) as date)";
			t = db.getSingleResult(sql, Arrays.asList(nepDate));
		} else {
			String sql = "select cast(dbo.toDate(dbo.getnepdate(getdate())) as date)";
			t = db.getSingleResult(sql);
		}
		date = t.get(0) + "";
		String[] dates = date.split("-");
		date = dates[1] + "/" + dates[2] + "/" + dates[0];
		return date;
	}

	/**
	 * Converts the given english date into nepali. Can accept null as input
	 * 
	 * @param engDate String format MM/DD/YYYY or YYYY/MM/DD or null
	 * @return String format YYYY/MM/DD
	 */

	public String getNepDate(String engDate) {
		Tuple t;
		String sql;
		//System.out.println(db.getDbName());
		if (engDate != null && !engDate.isEmpty()) {
			sql = "select dbo.getnepdate(?)";
			t = db.getSingleResult(sql, Arrays.asList(engDate));

		} else {
			sql = "select dbo.getnepdate(getdate())";
			t = db.getSingleResult(sql);

		}

		return (t.get(0) + "");
	}

	/**
	 * Converts date according to value passed. if english converts to nepali and
	 * vice versa
	 * 
	 * @param date mm/dd/yyyy for english, yyyy/mm/dd for nepali
	 * @return date mm/dd/yyyy for english, yyyy/mm/dd for nepali
	 */
//	public String cDate(String date) {
//		return cDate(date, db.getConnection());
//	}
//
//	public String cDate(String date, Connection con) {
//		int len = date.split("/")[0].length();
//		String sql;
//		if (len == 2) { // english date
//			String[] dates = date.split("/");
//			//date = dates[2] + "/" + dates[0] + "/" + dates[1];
//			sql = "select dbo.getnepdate(?)";
//			Tuple t = db.getSingleResult(sql, Arrays.asList(date), con);
//			return t.get(0) + "";
//		} else {
//			sql = "select cast(dbo.getEngdate(?) as date)";
//			Tuple t = db.getSingleResult(sql, Arrays.asList(date), con);
//			String[] dates = (t.get(0) + "").split("-");
//			return dates[1] + "/" + dates[2] + "/" + dates[0];
//		}
//
//	}

	/**
	 * Convert given date to integer value
	 * 
	 * @param engdate mm/dd/yyyy or nepdate yyyy/mm/dd
	 * @return equivalent integer value of given english date
	 */
	
	public String dateToInt(String date) {
		
		String sql;
		if (date == null || date.isEmpty()) {
			date = getNepDate("");
		}
		String[] dates = date.split("/");
		if (dates[0].length() == 2) {// english date
			sql = "select dbo.datetoint(dbo.getnepdate(?))";
		} else {
			sql = "select dbo.datetoint(?)";
		}
		Tuple t = db.getSingleResult(sql, Arrays.asList(date));
		return  t.get(0)+"";
	}

	/**
	 * converts int to date
	 * 
	 * @param dint
	 * @return nepali date yyyy/mm/dd
	 */
	public String intToDate(String dint) {
		
		String sql = "select dbo.inttodate(?)";
		Tuple t = db.getSingleResult(sql, Arrays.asList(dint));
		String date = t.get(0) + "";
		return date;
		

	}

	/**
	 * gets today date.
	 * 
	 * @param Lang language code stored in session
	 * @return date in yyyy/mm/dd if language code is np, mm/dd/yyy if langeuage
	 *         code is en
	 */
	public String todayDate(String Lang) {
		String sql = "";

		if ("np".equals(Lang.toLowerCase()))
			sql = "select dbo.getnepdate(getdate())";
		else
			sql = "SELECT convert(varchar, getdate(), 101)";

		Tuple t = db.getSingleResult(sql);
		return t.get(0) + "";
	}

	public String getFiscalYearEng(Date date) {
		String sql = "select dbo.getfiscalyear(?)";
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Tuple t = db.getSingleResult(sql, Arrays.asList(df.format(date)));
		return t.get(0) + "";
	}

	public int getFiscalYearEngInt(Date date) {
		String fiscYr = getFiscalYearEng(date);
		return Integer.parseInt(fiscYr.split("/")[0]);
	}

	public int getFiscalYearNepInt(String date) {
		String fiscYr = getFiscalYearNep(date);
		return Integer.parseInt(fiscYr.split("/")[0]);
	}

	public String getFiscalYearNep(String date) {
		String sql = "select dbo.getfiscalyearex(?)";
		Tuple t = db.getSingleResult(sql, Arrays.asList(date.replace('-', '/')));
		return t.get(0) + "";
	}

	public String getfyid(String fiscalyear) {
		
		String sql = "select dbo.getfyid(?)";
		//System.out.println(sql);
		Tuple t = db.getSingleResult(sql, Arrays.asList(fiscalyear == null ? "" : fiscalyear.replace('-', '/')));
		if (t != null) {
			return t.get(0) + "";
		}
		return "0";

	}

	public String fyStartDate(String fyid, String Lang) {
		String sql = "";
		if ("np".equals(Lang.toLowerCase()))
			sql = " select sdate from dbo.getdateintbetween(" + fyid + ",0,0)";
		else
			sql = "select dbo.getengdate(sdate) from dbo.getdateintbetween(" + fyid + ",0,0)";

		Tuple t = db.getSingleResult(sql);
		return t.get(0) + "";
	}

	public String getFyidByNepDate(String nDate) {
		// String nfy = getFiscalYearEx(nDate);
		// return getfyid(nfy);
		return getfyid(nDate);
	}

	public String getfybyid(String fyid) {

		String sql = "select dbo.getfybyid(?)";
		Tuple t = db.getSingleResult(sql, Arrays.asList(fyid));
		if (t != null) {
			return t.get(0) + "";
		} else {
			return "0000/00";
		}

	}

	public String getFiscalYearEx(String date) {
		if (date.isEmpty()) {
			date = getNepDate("").replace('-', '/');
		} else {
			date = date.replace('-', '/');
		}
		String sql = "select dbo.getfiscalyearex(?)";
		Tuple t = db.getSingleResult(sql, Arrays.asList(date));
		return t.get(0) + "";
	}

	public Tuple getDateIntBetween(String fyid, String tri, String monthid) {
		String sql = "select * from dbo.getdateintbetween(?,?,?)";
		Tuple t = db.getSingleResult(sql, Arrays.asList(fyid, tri, monthid));
		return t;
	}

	public boolean compareFyid(String oldDate, String date) {
		String sql = "select dbo.getfyid(?) as fyid,dbo.getfyid(?)as oldfyid";
		List<Tuple> tList = db.getResultList(sql, Arrays.asList(date, oldDate));
		if (tList.size() > 0) {
			if (cint(tList.get(0).get(0)) != cint(tList.get(0).get(1)))
				return false;
			else {
				return true;
			}
		}
		return false;
	}
	public long cint(Object num) {
		try {
			if (num instanceof Long) {
				return (Long) num;
			} else {
				return (long) Double.parseDouble(num + "");
			}
		} catch (NumberFormatException ex) {
						ex.printStackTrace();
		}
		return 0;
		
	}
	
	
	
}
