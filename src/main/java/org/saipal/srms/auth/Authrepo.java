package org.saipal.srms.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class Authrepo {
	public String userId;
	public String bankId;
	public String branchId;
	public String bankCode;
	public String token;
	public Map<String,Object> extraInfo = new HashMap<>();
}
