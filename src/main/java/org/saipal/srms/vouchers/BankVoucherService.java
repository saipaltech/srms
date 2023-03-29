package org.saipal.srms.vouchers;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.saipal.srms.auth.Authenticated;
import org.saipal.srms.service.AutoService;
import org.saipal.srms.util.ApiManager;
import org.saipal.srms.util.DB;
import org.saipal.srms.util.DbResponse;
import org.saipal.srms.util.Messenger;
import org.saipal.srms.util.Paginator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;

@Component
public class BankVoucherService extends AutoService {

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;

	@Autowired
	ApiManager api;

	private String table = "bank_deposits";



	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select transactionid,office,voucherdate,bankacname,bankacno from " + table + " where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		if (!auth.hasPermission("*")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		DbResponse rowEffect;
		BankVoucher model = new BankVoucher();
		model.loadData(document);
		String sql = "UPDATE " + table + " set transactionid=?,office=?,voucherdate=?,bankacname=?,bankacno=?,depositdate=?,bankvoucherno=?,remarks=?,creatorid=?,approverid=?,status=?,approved=? where id=?";
		rowEffect = db.execute(sql, Arrays.asList(model.transactionid,model.office,model.voucherdate,model.bankacname,model.bankacno,model.depositdate,model.bankvoucherno,model.remarks,model.creatorid,model.approverid,model.status,model.approved));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
		} else {
			return Messenger.getMessenger().error();
		}

	}


}