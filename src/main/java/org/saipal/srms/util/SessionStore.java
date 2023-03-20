package org.saipal.srms.util;

import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class SessionStore {
	public Map<String, Object> data;
}
