package org.saipal.srms.organizations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.saipal.srms.auth.Authenticated;
import org.saipal.srms.service.AutoService;
import org.saipal.srms.util.DB;
import org.saipal.srms.util.DbResponse;
import org.saipal.srms.util.Messenger;
//import org.saipal.srms.util.Paginator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class OrganizationService extends AutoService{
	
	@Autowired
	DB db;

	@Autowired
	Authenticated auth;
	
	private String table = "tbl_transfer";

}
