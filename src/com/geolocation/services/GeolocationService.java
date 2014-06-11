package com.geolocation.services;

/**
 * 
 * @author travishunter
 * This service should accept the request IP and perform a geolocation lookup, saving this data in the session. 
 */
public interface GeolocationService 
{
	String getSessionCountry(final String ipAddress);
}
