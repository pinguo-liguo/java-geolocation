<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
  <head><title>Geolocation Test</title></head>
  <body>
    <h1>What country do I live in?</h1>
    <p>${country}</p>
    <h1>What region (state)?</h1>
    <p>${region}</p>
    <h1>What city?</h1>
    <p>${city}</p>
    
    <c:if test="${not empty image}">
    	<p>Since we detected that you are in the US, here is a picture of a cat:</p>
    	<img src="${image}" />
    </c:if>
  </body>
</html>