package com.geolocation.services.impl;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.geolocation.services.GeolocationService;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.Omni;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

public class DefaultGeolocationService implements GeolocationService
{
	public final static String SESSION_REQUEST_COUNTRY_KEY = "sessionCountry";

	@Override
	public String getSessionCountry(final String ipAddress)
	{
		return getLocationForIpAddress(ipAddress);
	}
	
	private String getLocationForIpAddress(final String ipAddress)
	{
		// First try the webservice
		String country = getCurrentLocationForIp(ipAddress);
		if (country == null)
		{
			// Then try the local DB
			country = getFallbackLocationForIp(ipAddress);
		}
		return country;
	}
	
	/**
	 * This will request the location via a web service. This will always be called first
	 * 
	 * @param ipAddress
	 *           - IP of current incoming request
	 */
	private String getCurrentLocationForIp(final String ipAddress)
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
						return jsonParser.getText();
					}
				}
			}
		}
		catch (final Exception e)
		{
			return null;
		}

        return null;
	}

	/**
	 * As a fallback, his will look up the location in a local DB. This will be called second.
	 * 
	 * @param ipAddress
	 *           - IP of current incoming request
	 */
	private String getFallbackLocationForIp(final String ipAddress)
	{
		try
		{
			final URL dbResource = DefaultGeolocationService.class.getResource("GeoLite2-Country.mmdb");
			final DatabaseReader reader = new DatabaseReader(new File(dbResource.toURI()));
			final Omni model = reader.omni(InetAddress.getByName(ipAddress));
			reader.close();
			return model.getCountry().getIsoCode();
		}
		catch (final Exception e)
		{
			return null;
		}
	}
}
