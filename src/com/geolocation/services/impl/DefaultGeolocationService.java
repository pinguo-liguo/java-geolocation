package com.geolocation.services.impl;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;

import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.geolocation.services.GeolocationService;
import com.geolocation.web.session.UserGeolocation;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.Omni;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class DefaultGeolocationService implements GeolocationService
{
	public final static String SESSION_REQUEST_IP_KEY = "sessionIP";
	public final static String SESSION_REQUEST_COUNTRY_KEY = "sessionCountry";
	public final static String SESSION_REQUEST_REGION_KEY = "sessionRegion";
	public final static String SESSION_REQUEST_CITY_KEY = "sessionCity";
	
	// Provides access to the session object for current request
	public static HttpSession session() 
	{
	    ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
	    return attr.getRequest().getSession(true); // true == allow create
	}
	
	@Override
	public void setSessionIp(final String ipAddress)
	{
		// This IP check ensures we only do one lookup per session
		String lastIpAddress = (String) session().getAttribute(SESSION_REQUEST_IP_KEY);//userGeolocation.getIpAddress();
		if (lastIpAddress == null || !lastIpAddress.equals(ipAddress))
		{
			session().setAttribute(SESSION_REQUEST_IP_KEY, ipAddress);
			getLocationForIpAddress(ipAddress);
		}
	}

	@Override
	public String getSessionCountry()
	{
		return (String) session().getAttribute(SESSION_REQUEST_COUNTRY_KEY);
	}

	@Override
	public String getSessionRegion()
	{
		return (String) session().getAttribute(SESSION_REQUEST_REGION_KEY);
	}
	
	@Override
	public String getSessionCity()
	{
		return (String) session().getAttribute(SESSION_REQUEST_CITY_KEY);
	}
	
	private void getLocationForIpAddress(final String ipAddress)
	{
		// First try the webservice
		boolean success = getCurrentLocationForIp(ipAddress);
		if (success == false)
		{
			// Then try the local Maxmind DB as a fallback
			getFallbackLocationForIp(ipAddress);
		}
	}
	
	/**
	 * This will request the location via a web service. This will always be called first
	 * 
	 * @param ipAddress
	 *           - IP of current incoming request
	 */
	private boolean getCurrentLocationForIp(final String ipAddress)
	{
		final String url = "http://freegeoip.net/json/" + ipAddress;

		final HttpClient client = new HttpClient();
		final HttpMethod method = new GetMethod(url);

		// this one causes a timeout if a connection is established but there is 
		// no response within 5 seconds
		client.getParams().setParameter("http.socket.timeout", 5 * 1000);

		// this one causes a timeout if no connection is established within 5 seconds
		client.getParams().setParameter("http.connection.timeout", 5 * 1000);

		try
		{
			final int status = client.executeMethod(method);
			if (status == 200)
			{
				final JsonFactory jsonFactory = new JsonFactory();
				final JsonParser jsonParser = jsonFactory.createJsonParser(method.getResponseBodyAsString());

				// loop until token equal to "}"
				while (jsonParser.nextToken() != JsonToken.END_OBJECT)
				{
					final String field = jsonParser.getCurrentName();
					if ("country_code".equals(field))
					{
						jsonParser.nextToken();
						final String countryCode = jsonParser.getText();
						session().setAttribute(SESSION_REQUEST_COUNTRY_KEY, countryCode);
					}
					if ("region_code".equals(field))
					{
						jsonParser.nextToken();
						final String regionCode = jsonParser.getText();
						session().setAttribute(SESSION_REQUEST_REGION_KEY, regionCode);
					}
					if ("city".equals(field))
					{
						jsonParser.nextToken();
						final String regionCode = jsonParser.getText();
						session().setAttribute(SESSION_REQUEST_CITY_KEY, regionCode);
					}
				}
				
				return true;
			}
		}
		catch (final Exception e)
		{
			// Uh-oh
		}
		
		return false;
	}

	/**
	 * As a fallback, his will look up the location in a local DB. This will be called second.
	 * 
	 * @param ipAddress
	 *           - IP of current incoming request
	 */
	private boolean getFallbackLocationForIp(final String ipAddress)
	{
		try
		{
			final URL dbResource = DefaultGeolocationService.class.getResource("GeoLite2-Country.mmdb");
			final DatabaseReader reader = new DatabaseReader(new File(dbResource.toURI()));
			final Omni model = reader.omni(InetAddress.getByName(ipAddress));
			session().setAttribute(SESSION_REQUEST_COUNTRY_KEY, model.getCountry().getIsoCode());
			session().setAttribute(SESSION_REQUEST_REGION_KEY, model.getMostSpecificSubdivision().getIsoCode());
			reader.close();
		}
		catch (final Exception e)
		{
			return false;
		}
		
		return true;
	}
}
