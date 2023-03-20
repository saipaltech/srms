
package org.saipal.srms.util;

import java.sql.Connection;

public class ContextHolder {

	private static final ThreadLocal<String> CONTEXTTENANT = new ThreadLocal<>();
	private static final ThreadLocal<String> CONTEXTVDIR = new ThreadLocal<>();
	private static final ThreadLocal<Connection> CONN = new ThreadLocal<>();
	private static final ThreadLocal<Connection> CONNREV = new ThreadLocal<>();
	
	public static void setTenantId(String tenant) {
		CONTEXTTENANT.set(tenant);
	}

	public static String getTenant() {
		return CONTEXTTENANT.get();
	}

	public static String getVDir() {
		return CONTEXTVDIR.get();
	}

	public static void setCon(Connection con) {
		CONN.set(con);
	}

	public static Connection getCon() {
		return CONN.get();
	}
	
	public static Connection getConRev() {
		return CONNREV.get();
	}

	public static void setVDir(String tenant) {
		CONTEXTVDIR.set(tenant);
	}

	public static void clear() {
		CONTEXTTENANT.remove();
		CONTEXTVDIR.remove();
		CONN.remove();
	}

	public static void setConRev(Connection conrev) {
		CONNREV.set(conrev);
	}

}