package org.saipal.srms.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class EmailSender {
	
	@Autowired
	private DB db;
	
	public boolean sendOtpToEmail(String toEmail,String subject,String message) {
		
        try {
        	Map<String,String> config = new HashMap<>();
        	List<Tuple> dt = db.getResultList("select * from app_properties where pkey like '%mail.%'");
        	for(Tuple t:dt) {
        		config.put(t.get("pkey")+"",t.get("pvalue")+"");
        	}
    	    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    	    mailSender.setHost(config.get("mail.host"));
    	    mailSender.setPort(Integer.parseInt(config.get("mail.port")));
    	    mailSender.setUsername(config.get("mail.username"));
    	    mailSender.setPassword(config.get("mail.password"));
    	    Properties props = mailSender.getJavaMailProperties();
    	    props.put("mail.transport.protocol", "smtp");
    	    props.put("mail.smtp.auth", "true");
    	    props.put("mail.smtp.starttls.enable", "true");
    	    props.put("mail.debug", "true");
    	    //return mailSender;
            // Creating a simple mail message
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(config.get("mail.username"));
            mailMessage.setTo(toEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            mailSender.send(mailMessage);
            return true;
        }catch (Exception e) {
            return false;
        }
	}
}
