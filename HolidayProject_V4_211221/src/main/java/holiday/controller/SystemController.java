package holiday.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import holiday.config.EmailConfig;
import holiday.entity.SystemParams;
import holiday.services.SysService;

@Controller
public class SystemController {

	private SysService sysService;
	private EmailConfig emailConfig;

	@Autowired
	public void setEmailConfig(EmailConfig emailConfig) {
		this.emailConfig = emailConfig;
	}

	@Autowired
	public void setSysService(SysService sysService) {
		this.sysService = sysService;
	}

	@RequestMapping("/sysadmin")
	public String sysadmin(Model model) {

		SystemParams sysparamsDB = sysService.getLastSysParam();

		sysparamsDB.setTransportProtocol("smtp");
		sysparamsDB.setSmtpAuth("true");
		sysparamsDB.setStartSslEnable("true");

		model.addAttribute("sysparamsDB", sysparamsDB);

		return "sysadmin";
	}

	@PostMapping("/setnewsysadmin")
	public String setnewsysadmin(@ModelAttribute SystemParams sysparams) {

		if (sysparams.getMailDebug() == null)
			sysparams.setMailDebug("false");
		sysService.save(sysparams);
		emailConfig.updateJavaMailSender(sysparams);

		return "redirect:/";
	}
}
