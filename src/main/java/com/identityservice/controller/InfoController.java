package com.identityservice.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wiki")
public class InfoController {
	
	public static final Logger logger = LoggerFactory.getLogger(InfoController.class);
	
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public @ResponseBody String info(Principal principal) {
		Authentication authentication = (Authentication) principal;
		org.springframework.security.core.userdetails.User reqUser = (org.springframework.security.core.userdetails.User) authentication
				.getPrincipal();
		logger.info("POST/info requesting user: " + reqUser.toString());
		
		return String.format("Hello %s! \nI am:\n {'name': 'Simple User Identity Service', 'version': '1.0'}\n", reqUser.getUsername());
	}

}
