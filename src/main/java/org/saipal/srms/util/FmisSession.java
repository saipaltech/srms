package org.saipal.srms.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.persistence.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class FmisSession {
	
	@Autowired
	ApplicationContext context;
	
	@Autowired
	DB db;

	private static Logger log = LoggerFactory.getLogger(FmisSession.class);

	public String session(String key) {
		SessionStore ss = context.getBean(SessionStore.class);
		if (ss.data == null) {
			return "";
		}
		return ss.data.get(key.toLowerCase()) == null ? "" : ss.data.get(key.toLowerCase()) + "";
	}

	public void setAttribute(String key, Object value) {
		SessionStore ss = context.getBean(SessionStore.class);
		if (ss.data == null) {
			ss.data = new HashMap<>();
		}
		ss.data.put(key.toLowerCase(), value);
	}

	public Object getAttribute(String key) {
		SessionStore ss = context.getBean(SessionStore.class);
		if (ss.data == null) {
			return "";
		}
		return ss.data.get(key.toLowerCase()) == null ? "" : ss.data.get(key.toLowerCase());
	}

	public void initSession(String sessionid) {
		SessionStore ss = context.getBean(SessionStore.class);
		if (ss.data == null) {
			ss.data = new HashMap<>();
		}
		String sql = "select variablename,valuess from sys_useractivity where sessionid=?";

		List<Tuple> tList = db.getResultList(sql, Arrays.asList(sessionid));
		if (tList.size() > 0) {
			for (Tuple t : tList) {
				ss.data.put((t.get("variablename") + "").toLowerCase(), t.get("valuess"));
			}
		}
	}

	public void removeAttribute(String key) {
		SessionStore ss = context.getBean(SessionStore.class);
		if (ss.data != null) {
			if (ss.data.get("sessionid") != null) {
				if (ss.data.get(key) != null) {
					ss.data.remove(key);
					String sql = "delete from sys_useractivity where sessionid=? and variablename=?";
					db.execute(sql, Arrays.asList(ss.data.get("sessionid") + "", key.toLowerCase()));
				} else {
					log.info("session attribute not found with key " + key);
				}
			} else {
				log.info("session id not found in map");
			}
		} else {
			log.info("Session map is empty");
		}

	}

}
