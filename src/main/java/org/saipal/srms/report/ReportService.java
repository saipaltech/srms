package org.saipal.srms.report;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.saipal.srms.service.AutoService;
import org.saipal.srms.util.Messenger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ReportService extends AutoService{

	public ResponseEntity<Map<String, Object>> getFys() {
		return Messenger.getMessenger().setData(Map.of("id",18,"label","2078/79","id",19,"label","2079/80","id",20,"label","2080/81")).success();
	}
	
	public ResponseEntity<Map<String, Object>> getLocalLevels() {
		List<Map<String,Object>> d = db.getResultListMap(
				"select distinct als.id,als.nameen,als.namenp from admin_local_level_structure als join bankaccount ba on als.id=ba.lgid and bankid=? order by als.namenp",
				Arrays.asList(auth.getBankId(), auth.getBranchId()));
		return Messenger.getMessenger().setData(d).success();
	}
	
	public ResponseEntity<Map<String, Object>> getBranches() {
		String cond="";
		if (!auth.hasPermission("bankhq")) {
			cond +="and id='"+auth.getBranchId()+"'";
		}
		List<Map<String,Object>> d = db.getResultListMap(
				"select distinct * from branches where bankid=? "+cond+" order by name",
				Arrays.asList(auth.getBankId()));
		return Messenger.getMessenger().setData(d).success();
	}
}
