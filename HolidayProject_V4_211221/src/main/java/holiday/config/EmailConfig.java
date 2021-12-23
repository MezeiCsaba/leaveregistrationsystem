package holiday.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import holiday.entity.SystemParams;
import holiday.services.SysService;

@Configuration
public class EmailConfig {

	private SysService sysService;
	private SystemParams sysparams;
	private JavaMailSenderImpl mailSender;

	@Autowired
	public void setSysService(SysService sysService) {
		this.sysService = sysService;
	}

	@Bean
	public JavaMailSender getJavaMailSender() {

		sysparams = sysService.getLastSysParam();
		mailSender = new JavaMailSenderImpl();
		setParams(sysparams);
		return mailSender;
	}

	public void updateJavaMailSender(SystemParams sysparams) {
		setParams(sysparams);
	}

	private void setParams(SystemParams sysparams) {
		mailSender.setHost(sysparams.getMailHost() == null ? "" : sysparams.getMailHost());
		mailSender.setPort(sysparams.getMailPort() == null ? 0 : sysparams.getMailPort());
		mailSender.setUsername(sysparams.getMailUsername() == null ? "" : sysparams.getMailUsername());
		mailSender.setPassword(sysparams.getMailPassword() == null ? "" : sysparams.getMailPassword());
		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol",
				sysparams.getTransportProtocol() == null ? "" : sysparams.getTransportProtocol());
		props.put("mail.smtp.auth", sysparams.getSmtpAuth() == null ? "" : sysparams.getSmtpAuth());
		props.put("mail.smtp.starttls.enable",
				sysparams.getStartSslEnable() == null ? "" : sysparams.getStartSslEnable());
		props.put("mail.debug", sysparams.getMailDebug() == null ? "false" : sysparams.getMailDebug());

	}

}