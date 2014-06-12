package com.geolocation.services.impl;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.geolocation.services.GeolocationService;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.Omni;

public class DefaultGeolocationService implements GeolocationService
{
	public final static String SESSION_REQUEST_IP_KEY = "sessionIP";
	public final static String SESSION_REQUEST_COUNTRY_KEY = "sessionCountry";
	public final static String SESSION_REQUEST_REGION_KEY = "sessionRegion";
	public final static String SESSION_REQUEST_CITY_KEY = "sessionCity";
	public final static String PROJECT_PROPERTIES_PATH = "WEB-INF/resources/project.properties";
	public final static String MAXMIND_DB_PATH_DEFAULT = "WEB-INF/resources/GeoLite2-Country.mmdb";
	
	// Provides access to the session object for current request
	private static HttpSession getSession() 
	{
	    ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
	    return attr.getRequest().getSession(true); // true == allow create
	}
	
	// Provides access to the servlet context for the request
	private static ServletContext getServletContext() 
	{
		return getSession().getServletContext();
	}
	
	@Override
	public void setSessionIp(final String ipAddress)
	{
		// This IP check ensures we only do one lookup per session
		String lastIpAddress = (String) getSession().getAttribute(SESSION_REQUEST_IP_KEY);
		if (lastIpAddress == null || !lastIpAddress.equals(ipAddress))
		{
			getSession().setAttribute(SESSION_REQUEST_IP_KEY, ipAddress);
			getLocationForIpAddress(ipAddress);
		}
	}

	@Override
	public String getSessionCountry()
	{
		return (String) getSession().getAttribute(SESSION_REQUEST_COUNTRY_KEY);
	}

	@Override
	public String getSessionRegion()
	{
		return (String) getSession().getAttribute(SESSION_REQUEST_REGION_KEY);
	}
	
	@Override
	public String getSessionCity()
	{
		return (String) getSession().getAttribute(SESSION_REQUEST_CITY_KEY);
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
						getSession().setAttribute(SESSION_REQUEST_COUNTRY_KEY, countryCode);
					}
					if ("region_code".equals(field))
					{
						jsonParser.nextToken();
						final String regionCode = jsonParser.getText();
						getSession().setAttribute(SESSION_REQUEST_REGION_KEY, regionCode);
					}
					if ("city".equals(field))
					{
						jsonParser.nextToken();
						final String regionCode = jsonParser.getText();
						getSession().setAttribute(SESSION_REQUEST_CITY_KEY, regionCode);
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
	 * As a fallback, this will look up the location in a local DB. This will be called second.
	 * 
	 * @param ipAddress
	 *           - IP of current incoming request
	 */
	private boolean getFallbackLocationForIp(final String ipAddress)
	{
		try
		{
			final String dbPath = getMaxmindDBPath();
			final DatabaseReader reader = new DatabaseReader(new File(dbPath));
			final Omni model = reader.omni(InetAddress.getByName(ipAddress));
			getSession().setAttribute(SESSION_REQUEST_COUNTRY_KEY, model.getCountry().getIsoCode());
			getSession().setAttribute(SESSION_REQUEST_REGION_KEY, model.getMostSpecificSubdivision().getIsoCode());
			getSession().setAttribute(SESSION_REQUEST_CITY_KEY, model.getCity().getName());
			reader.close();
		}
		catch (final Exception e)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * This method will look in "project.properties" in WEB-INF/resources and see if a custom path is
	 * specified for the Maxmind DB. If found, this method returns that path, and otherwise a default one.
	 * This is useful for servlet containers that do not expand WAR files, like tomcat, where you might 
	 * want to place the Maxmind DB somewhere else on the file system.
	 * 
	 * @return String
	 */
	private String getMaxmindDBPath()
	{
		InputStream resourceContent = getServletContext().getResourceAsStream(PROJECT_PROPERTIES_PATH);
		
		if (resourceContent != null)
		{
			try 
			{
				Properties properties = new Properties();
				properties.load(resourceContent);
				
				String dbPath = properties.getProperty("maxmind.db.path");
				if (dbPath != null) {
					return dbPath;
				}
			} 
			catch (Exception e)
			{
				// Uh-oh
			}
		}
		
		return getServletContext().getRealPath(MAXMIND_DB_PATH_DEFAULT);
	}
}
