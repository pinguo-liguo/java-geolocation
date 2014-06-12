java-geolocation
=================

A lightweight spring-mvc project setup with request geolocation detection!

#### How it works:

There are two important files that do the heavy lifting:

DefaultGeolocationService (com.geolocation.services.impl) - This class takes the IP address of the current request and performs a geolocation lookup using the IP. It initially uses a web service, freegeoip.net, which returns a lightweight JSON object with location information. In the case of a failure (web service is down, incomplete location information), the service leverages a Maxmind location database and API as a fallback.

GeolocationInterceptor (com.geolocation.web.interceptors) - This class simply grabs the IP address from each request and passes it to the DefaultGeolocationService.

By the time code execution reaches a controller, your business logic can reliably query this location information from the geolocation service.