package com.geolocation.web.controllers;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.geolocation.services.GeolocationService;

@Controller
@RequestMapping("/")
public class GeolocationTestController
{	
	@Resource(name = "geolocationService")
	private GeolocationService geolocationService;
	
	@RequestMapping(method = RequestMethod.GET)
    public String testGeolocationService(Model model) 
	{
		final String requestCountry = geolocationService.getSessionCountry();
		
		// Only do something or show something for US users
		if ("US".equalsIgnoreCase(requestCountry))
		{
			// Some business logic here
			
			model.addAttribute("image", "http://placekitten.com/200/300");
		}
		
		model.addAttribute("country", requestCountry);
		model.addAttribute("region", geolocationService.getSessionRegion());
		model.addAttribute("city", geolocationService.getSessionCity());
        return "geotest";
    }
}
