package com.community.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorHandlerController {
	
	@GetMapping("/400")
	public String error400(Model model) {
		model.addAttribute("headerError", "400");
		model.addAttribute("error", "I guess I could not figure out what you mean :)");
		return "error-page";
	}
	
	@GetMapping("/404")
	public String error404(Model model) {
		model.addAttribute("headerError", "404");
		model.addAttribute("error", "The requested URL was not found on this server.");
		return "error-page";
	}
	
	@GetMapping("/500")
	public String error500(Model model) {
		model.addAttribute("headerError", "500");
		model.addAttribute("error", "It's my foult :(\nThe server encountered an internal error or misconfiguration"
				+ " and was unable to complete your request.Please contact the server administrator, "
				+ "you{at}your.address and inform them of the time the error occurred, and anything "
				+ "you might have done that may have caused the error.");
		return "error-page";
	}
}
