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
public class TaxPayerVoucherService extends AutoService {

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;

	@Autowired
	ApiManager api;

	private String table = "taxvouchers";

	public ResponseEntity<Map<String, Object>> index() {
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String condition = " where id!=1 ";
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = TaxPayerVoucher.searchables();
			condition += "and (";
			for (String field : searchbles) {
				condition += field + " LIKE '%" + db.esc(request("searchTerm")) + "%' or ";
			}
			condition = condition.substring(0, condition.length() - 3);
			condition += ")";
		}
		String sort = "";
		if (!request("sortKey").isBlank()) {
			if (!request("sortDir").isBlank()) {
				sort = request("sortKey") + " " + request("sortDir");
			}
		}

		Paginator p = new Paginator();
		Map<String, Object> result = p.setPageNo(request("page")).setPerPage(request("perPage")).setOrderBy(sort)
				.select("voucherno,taxpayername,llgname,amount,revenuetitle").sqlBody("from " + table + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	@Transactional
	public ResponseEntity<Map<String, Object>> store() {
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String sql = "";
		TaxPayerVoucher model = new TaxPayerVoucher();
		model.loadData(document);
		String usq = "select count(voucherno) from taxvouchers where voucherno=?";
		Tuple res = db.getSingleResult(usq, Arrays.asList(model.voucherno));
		if ((!(res.get(0) + "").equals("0"))) {
			return Messenger.getMessenger().setMessage("This voucher No is already used.").error();
		}
		sql = "INSERT INTO banks(code,name, disabled, approved) VALUES (?,?,?,?)";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(model.voucherno, model.llgname, model.llgcode, model.costcentername));

		if (rowEffect.getErrorNumber() == 0) {			
			return Messenger.getMessenger().success();
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select id, code, name ,disabled,approved from " + table + " where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		DbResponse rowEffect;
		TaxPayerVoucher model = new TaxPayerVoucher();
		model.loadData(document);
		String sql = "UPDATE " + table + " set approved=?, disabled=? where id=?";
		rowEffect = db.execute(sql, Arrays.asList(model.voucherno, model.llgcode, model.llgname, model.costcentercode));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
		} else {
			return Messenger.getMessenger().error();
		}

	}

	public ResponseEntity<Map<String, Object>> destroy(String id) {
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String sql = "delete from " + table + " where id  = ?";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<List<Map<String, Object>>> getList() {
		String bankId = auth.getBankId();
		String sql = "";
		if (bankId.equals("1")) {
			sql = "select id,name from " + table + " where id !=1";
		} else {
			sql = "select id,name from " + table + " where id ='" + bankId + "'";
		}
		return ResponseEntity.ok(db.getResultListMap(sql));
	}

	public ResponseEntity<String> getBanksFromSutra() {
		return ResponseEntity.ok(api.getBanks().toString());
	}

	public ResponseEntity<String> getLocalLevels() {
		String bankCode = auth.getBankCode();
		return ResponseEntity.ok(api.localLevels(bankCode).toString());
	}

	public ResponseEntity<String> getCostCentres() {
		String llgCode = request("llgcode");
		return ResponseEntity.ok(api.costCentres(llgCode).toString());
	}

	public ResponseEntity<String> getBankAccounts() {
		String bankCode = auth.getBankCode();
		String llgCode = request("llgcode");
		if(llgCode.isBlank()) {
			return ResponseEntity.ok("{status:0,message:,\"Local Level Code is required\"}");
		}
		return ResponseEntity.ok(api.bankAccounts(bankCode,llgCode).toString());
	}

	public ResponseEntity<String> getRevenue() {
		String llgCode = request("llgcode");
		if(llgCode.isBlank()) {
			return ResponseEntity.ok("{status:0,message:,\"Local Level Code is required\"}");
		}
		return ResponseEntity.ok(api.revenueCodes(llgCode).toString());
	}
	
	public ResponseEntity<String> getVoucherDetails() {
		String voucherno = request("voucherno");
		if(voucherno.isBlank()) {
			return ResponseEntity.ok("{status:0,message:,\"Local Level Code is required\"}");
		}
		return ResponseEntity.ok(api.getVoucherDetails(voucherno).toString());
	}

}