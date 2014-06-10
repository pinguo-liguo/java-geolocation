package com.geolocation.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/")
public class GeolocationTestController
{	
	@RequestMapping(method = RequestMethod.GET)
    public String testGeolocationService(Model model) {
        return "geotest";
    }
}
