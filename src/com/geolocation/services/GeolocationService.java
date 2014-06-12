package com.geolocation.services;

/**
 * 
 * @author travishunter
 * This service should accept the request IP and perform a geolocation lookup, saving this data in the session. 
 */
public interface GeolocationService 
{
	/**
	 *  This method will set the IP address for the session and invoke a geolocation lookup. It should ideally
	 *  only be invoked by the GeolocationInterceptor, but it is acceptable to invoke it otherwise.
	 * @param ipAddress
	 */
	void setSessionIp(final String ipAddress);

	String getSessionCountry();

	String getSessionRegion();
		
	String getSessionCity();
}
