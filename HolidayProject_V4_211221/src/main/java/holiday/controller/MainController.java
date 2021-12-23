package holiday.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import holiday.entity.User;
import holiday.services.UserService;

@Controller
public class MainController {

	private UserService userService;

	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@RequestMapping("/")
	public String index(Authentication authentication, Model model) {
		User authUser = userService.findByEmail(authentication.getName());
		userService.setPageAttributums(authUser, model);
		return "index";
	}

}
