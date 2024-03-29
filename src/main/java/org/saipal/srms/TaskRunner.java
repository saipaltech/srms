package org.saipal.srms;

import org.saipal.srms.dayclose.DaycloseService;
import org.saipal.srms.util.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class TaskRunner {
	
	Logger log = LoggerFactory.getLogger(TaskRunner.class);
	
	@Autowired
	DB db;
	
	@Autowired
	DaycloseService ds;
	
//	@Scheduled(cron="0 5 0 * * *")
//	public void deleteAllUnVerifiedVouchers() {
//		log.info("Auto Delete Unverified Vouchers Strted");
//		db.execute("delete from taxvouchers where dateint < (cast(format(getdate(),'yyyyMMdd') as int)) and ttype=1 and approved=0");
//		log.info("Auto Delete Unverified Vouchers End");
//	}
	
	@Scheduled(cron="5 00 00 * * *")
	public void automaticDayClose() {
		log.info("Automatic DayClose Started");
		ds.daycloseScheduler();
		log.info("Automatic Dayclose End");
	}
}
