package com.geolocation.web.interceptors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.geolocation.services.GeolocationService;

/**
 * This interceptor is used to initiate the geolocation lookup for the request.
 * @author travis
 *
 */
public class GeolocationInterceptor implements HandlerInterceptor 
{
	@Resource(name = "geolocationService")
	private GeolocationService geolocationService;
	
	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception 
	{

	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler, ModelAndView modelAndView) throws Exception 
	{

	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception 
	{
		final String ipAddress = request.getRemoteAddr();

		// Pass the request IP to the geolocation service, which will perform the location lookup
		geolocationService.setSessionIp(ipAddress);
		
		return true;
	}
}
