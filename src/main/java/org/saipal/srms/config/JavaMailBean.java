package org.saipal.srms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

//@Configuration
public class JavaMailBean {
	
//	private final DB db;
//
//    @Autowired
//    public JavaMailBean(DB db) {
//        this.db = db;
//    }
//
//    @Bean
//    @DependsOn("dataSource")
//	public JavaMailSender getJavaMailSender() {
//    	Map<String,String> config = new HashMap<>();
//    	List<Tuple> dt = db.getResultList("select * from app_properties where pkey like '%mail.%'");
//    	for(Tuple t:dt) {
//    		config.put(t.get("pkey")+"",t.get("pvalue")+"");
//    	}
//	    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//	    mailSender.setHost(config.get("mail.host"));
//	    mailSender.setPort(Integer.parseInt(config.get("mail.port")));
//	    mailSender.setUsername(config.get("mail.username"));
//	    mailSender.setPassword(config.get("mail.password"));
//	    Properties props = mailSender.getJavaMailProperties();
//	    props.put("mail.transport.protocol", "smtp");
//	    props.put("mail.smtp.auth", "true");
//	    props.put("mail.smtp.starttls.enable", "true");
//	    //props.put("mail.debug", "true");
//	    return mailSender;
//	}
	
	@Bean
	public JavaMailSender getJavaMailSender() {
		return null;
	}

}
