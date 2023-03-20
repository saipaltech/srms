package org.saipal.srms.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class DB {
	private static final Logger LOG = LoggerFactory.getLogger(DB.class);

	@PersistenceContext
	EntityManager em;

	@Autowired
	public PlatformTransactionManager transactionManager;

	public EntityManager getConnection() {
		return em;
	}

	public TransactionTemplate getTransTemplate() {
		return new TransactionTemplate(transactionManager);
	}

	public List<Tuple> getResultList(String sql, List<?> args) {
		try {
			Query qry = em.createNativeQuery(sql, Tuple.class);
			if (args != null) {
				for (int i = 0; i < args.size(); i++) {
					qry.setParameter(i + 1, args.get(i));
				}
			}
			List<Tuple> result =  qry.getResultList();
			return result;
		} catch (Exception e) {
			LOG.info("On getRestlutList Query: " + e.getMessage());
		}
		return null;
	}

	/**
	 * queries and returns the result list for the sql
	 * 
	 * @param sql sql query
	 * @return List<Tuple> containing the result set
	 * @return empty list if there is no matching tuple in database for given query
	 */
	public List<Tuple> getResultList(String sql) {
		return getResultList(sql, null);
	}

	/**
	 * queries and returns the a single tuple for the sql uses prepared statement to
	 * execute the queries
	 * 
	 * @param sql  sql query with dynamic parameter binding
	 * @param args arguments to be bound in runtime in the sql query
	 * @return Tuple containing the single tuple
	 * @return null if there is no matching tuple for given query
	 */

	public Tuple getSingleResult(String sql, List<?> args) {
		LOG.info(dumpQuery(sql, args));
		try {
			Query qry = em.createNativeQuery(sql, Tuple.class);
			if (args != null) {
				for (int i = 0; i < args.size(); i++) {
					qry.setParameter(i + 1, args.get(i));
				}
			}
			Tuple result = (Tuple) qry.getSingleResult();
			return result;
		} catch (Exception e) {
			LOG.info("On Singele Resutt Query: " + e.getMessage());
		}
		return null;

	}

	/**
	 * queries and returns the a single tuple for the sql
	 * 
	 * @param sql  sql query
	 * @param args arguments to be bound in runtime in the sql query
	 * @return Tuple containing the single tuple
	 * @return null if there is no matching tuple for given query
	 */
	public Tuple getSingleResult(String sql) {
		return getSingleResult(sql, null);
	}

	public List<Map<String, Object>> getResultListMap(String sql) {
		List<Tuple> t = getResultList(sql);
		if (t == null) {
			return null;
		}
		return getMapList(t);

	}

	public List<Map<String, Object>> getResultListMap(String sql, List<?> args) {
		List<Tuple> t = getResultList(sql, args);
		if (t == null) {
			return null;
		}
		return getMapList(t);
	}

	public Map<String, Object> getSingleResultMap(String sql) {
		Tuple t = getSingleResult(sql);
		if (t == null) {
			return null;
		}
		return getSingleMap(t);

	}

	public Map<String, Object> getSingleResultMap(String sql, List<?> args) {
		Tuple t = getSingleResult(sql, args);
		if (t == null) {
			return null;
		}
		return getSingleMap(t);
	}

	private Map<String, Object> getSingleMap(Tuple t) {
		List<String> alias = fieldExtractor(t);
		Map<String, Object> row = new LinkedHashMap<>();
		for (String field : alias) {
			row.put(field, t.get(field));
		}
		return row;
	}

	private List<Map<String, Object>> getMapList(List<Tuple> tuples) {
		// prepares rows
		List<Map<String, Object>> rows = new ArrayList<>();
		
		if(tuples.size() == 0 ) {
			return rows;
		}
		
		// extracts the alias
		List<String> alias = fieldExtractor(tuples.get(0));
		
		for (Tuple f : tuples) {
			Map<String, Object> insData = new LinkedHashMap<>();
			for (String field : alias) {
				insData.put(field, f.get(field));
			}
			rows.add(insData);
		}
		return rows;
	}

	private List<String> fieldExtractor(Tuple t) {
		List<String> alias = new ArrayList<>();
		if (t != null) {
			List<TupleElement<?>> lte = t.getElements();
			for (TupleElement<?> te : lte) {
				alias.add(te.getAlias());
			}
		}
		return alias;
	}

	/**
	 * performs database update for given query(update/delete)
	 * 
	 * @param sql  sql query with dynamic parameter binding
	 * @param args arguments to be bound in runtime in the sql query
	 * @return Map contains number of rows affected in key "num" if update is
	 *         successful, else contains error message in "error" key
	 */

	@Transactional
	public DbResponse execute(String sql, List<Object> args) {
		DbResponse dbResp = new DbResponse();
		try {
			LOG.info(dumpQuery(sql, args));
			Query qry = em.createNativeQuery(sql);
			if (args != null) {
				for (int i = 0; i < args.size(); i++) {
					qry.setParameter(i + 1, args.get(i));
				}
			}
			int dt = qry.executeUpdate();
			dbResp.setRows(dt);
			dbResp.setErrorNumber(0);
		} catch (Exception e) {
			dbResp.setRows(0);
			dbResp.setErrorNumber(1);
			dbResp.setMessage(e.getMessage());
		}
		return dbResp;
	}

	/**
	 * method to execute multiple update with single sql statement and different
	 * query parameters uses batch processing of jdbc to perform the update
	 * 
	 * @param sql    sql string
	 * @param params List of argument, each value is another list that passed as
	 *               arguement in sql
	 */
	public DbResponse executeBulk(String sql, List<List<Object>> params) {
		Session hibernateSession = em.unwrap(Session.class);
		//params.forEach(p->{ LOG.info(dumpQuery(sql, p)); });
		DbResponse dbResp = new DbResponse();
		hibernateSession.doWork(connection -> {
			try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				for (List<Object> param : params) {
					int j = 1;
					for (Object pm : param) {
						preparedStatement.setObject(j, pm);
						j++;
					}
					j = 1;
					preparedStatement.addBatch();
				}
				int[] rs = preparedStatement.executeBatch();
				dbResp.setRows(rs.length);
				dbResp.setErrorNumber(0);
			} catch (SQLException e) {
				dbResp.setRows(0);
				dbResp.setErrorNumber(1);
				e.printStackTrace();
			}
		});
		return dbResp;
	}

	/**
	 * performs database update for given query(update/delete)
	 * 
	 * @param sql sql query
	 * @return Map contains number of rows affected in key "num" if update is
	 *         successful, else contains error message in "error" key
	 */
	@Transactional
	public DbResponse execute(String sql) {
		return execute(sql, null);
	}

	/**
	 * returns new unique integer value
	 * 
	 * @return String unique integer generated by sql server database
	 */
	public String newIdInt() {
		return getSingleResult("select dbo.newidint() as newidint").get("newidint") + "";
	}

	/**
	 * method for viewing dump of sql query with dynamic parameter binding
	 * 
	 * @param sql  sql query with dynamic parameter binding
	 * @param args arguments to be bound in runtime in the sql query
	 * @return String sql constructed from the sql string with binding parameters
	 */
	public String dumpQuery(String sql, List<?> args) {
		if (args == null) {
			return sql;
		}
		try {
			List<Object> args1 = new ArrayList<>();
			args1.addAll(args);
			return replace(sql, args1);
		} catch (Exception e) {
			return "";
		}
	}

	public String replace(String sql, List<Object> args) {
		if (args == null || args.size() == 0) {
			return sql;
		}
		Object val = args.get(0);
		args.remove(0);
		return sql.substring(0, sql.indexOf('?')) + "'" + val + "'"
				+ replace(sql.substring(sql.indexOf('?') + 1), args);

	}

	/**
	 * method to perform
	 * 
	 * @param sqlList
	 * @param argList
	 */
	public DbResponse execute(List<String> sqlList, List<List<Object>> argList) {
		Session hibernateSession = em.unwrap(Session.class);
		DbResponse dbResp = new DbResponse();
		hibernateSession.doWork(connection -> {
			for (String sql : sqlList) {
				int i = 0;
				try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
					List<Object> params = argList.get(i);
					int j = 1;
					for (Object pm : params) {
						preparedStatement.setObject(j, pm);
						j++;
					}
					j = 1;
					preparedStatement.addBatch();
					int[] rs = preparedStatement.executeBatch();
					i++;
					dbResp.setRows(dbResp.getRows() + rs.length);
					dbResp.setErrorNumber(0);
				} catch (SQLException e) {
					dbResp.setRows(0);
					dbResp.setErrorNumber(1);
					dbResp.setMessage(e.getMessage());
					e.printStackTrace();
				}
			}
		});
		return dbResp;
	}

	public String esc(String s) {
		String ret = "";
		ret = s.replace("'", "''");
		return ret;
	}

	@Transactional
	public DbResponse insert(String table, Map<String, Object> data) {
		if (data.size() > 0) {
			String fields = "";
			String paramHolder = "";
			List<Object> params = new ArrayList<>();
			for (String field : data.keySet()) {
				Object value = data.get(field);
				if(value != null && !"null".equals(value)) {
					//if(!(value+"").isBlank() && !(value+"").equals("null")) {
						fields += field + ", ";
						paramHolder += "?" + ", ";
						params.add(value);
					//s}
				}
			}
			String sql = "insert into " + table + " (" + fields.substring(0, fields.length() - 2) + " ) values ("
					+ paramHolder.substring(0, paramHolder.length() - 2) + ")";
			return execute(sql, params);
		}
		DbResponse dbResp = new DbResponse();
		dbResp.setRows(0);
		dbResp.setErrorNumber(1);
		dbResp.setMessage("Invalid Data provided");
		return dbResp;

	}
	
	@Transactional
	public DbResponse insert(String table, List<Map<String, Object>> datas) {
		String sql = "";
		List<Object> params = new ArrayList<>();
		int count = 0;
		for(Map<String,Object> data : datas ) {
			if (data.size() > 0) {
				String fields = "";
				String paramHolder = "";
				for (String field : data.keySet()) {
					fields += field + ", ";
					paramHolder += "?" + ", ";
					Object value = data.get(field);
					if(value==null) {
						params.add(null);
					}else if((value+"").isBlank() || (value+"").equals("null")) {
						params.add(null);
					}else {
						params.add(value);
					}
				}
				if(count==0) {
					sql += "insert into " + table + " (" + fields.substring(0, fields.length() - 2) + " ) values ("
							+ paramHolder.substring(0, paramHolder.length() - 2) + "),";
				}else {
					sql +=" ("+ paramHolder.substring(0, paramHolder.length() - 2) + "),";
				}
				count++;
			}
		}
		return execute(sql.replaceAll(",$", ""), params);
	}

	@Transactional
	public DbResponse update(String table, String key, Map<String, Object> data) {
		List<Object> params = new ArrayList<>();
		if (data.size() > 0) {
			String fields = "";
			Object keyValue = data.remove(key);
			for (String field : data.keySet()) {
				Object value = data.get(field);
				if(value != null) {
					if(!"null".equals(value)) {
						fields += field + "=?, ";
						params.add(value);
					}
				}
			}
			params.add(keyValue);
			String sql = "update " + table + " set " + fields.substring(0, fields.length() - 2) + " where " + key
					+ " = ?";
			return execute(sql, params);
		}
		DbResponse dbResp = new DbResponse();
		dbResp.setRows(0);
		dbResp.setErrorNumber(1);
		dbResp.setMessage("Invalid Data provided");
		return dbResp;

	}

	public DbResponse delete(String table, String pkey, String value) {
		String sql = "delete from " + table + " where " + pkey + " = ? ";
		return execute(sql, Arrays.asList(value));
	}
}