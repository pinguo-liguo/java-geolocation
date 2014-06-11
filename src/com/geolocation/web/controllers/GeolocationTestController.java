package com.geolocation.web.controllers;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.ui.Model;

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
		model.addAttribute("country", geolocationService.getSessionCountry("50.133.230.146"));
        return "geotest";
    }
}
