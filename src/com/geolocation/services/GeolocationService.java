package com.geolocation.services;

/**
 * 
 * @author travishunter
 * This service should accept the request IP and perform a geolocation lookup, saving this data in the session. 
 */
public interface GeolocationService 
{
	// This method will do the heavy lifting, and should be invoked by a custom session filter
	void setSessionIp(final String ipAddress);

	String getSessionCountry();

	String getSessionRegion();
		
	String getSessionCity();
}
