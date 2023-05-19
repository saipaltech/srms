package org.saipal.srms.sms;

import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sendmsg")

public class sendmsg {
	@Autowired
	F1SoftSmsGateway sms;
	@RequestMapping("")
	public void testsms() {
		
		try {
			
			sms.sendSms("9851087175", "hi", "123");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
