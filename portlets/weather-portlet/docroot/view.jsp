<%--
/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/init.jsp" %>

<form name="<portlet:namespace />fm" onSubmit="submitForm(document.<portlet:namespace />fm, 'http://openweathermap.org/find?q=', false); return false;" target="_blank">

<table class="lfr-table">

<%
for (String zip : zips) {
	Weather weather = WeatherUtil.getWeather(zip);

	if (weather != null) {
%>

		<tr>
			<td>
				<a href="http://www.openweathermap.org/city/<%= HtmlUtil.escapeURL(weather.getCityId()) %>" style="font-size: xx-small; font-weight: bold;" target="_blank"><%= HtmlUtil.escape(weather.getZip()) %></a>
			</td>
			<td align="right">
				<span style="font-size: xx-small;">

				<c:if test="<%= fahrenheit %>">
					<%= weather.getCurrentTemp() %> &deg;F
				</c:if>

				<c:if test="<%= !fahrenheit %>">
					<%= Math.round((.5555555555 * (weather.getCurrentTemp() + 459.67)) - 273.15) %> &deg;C
				</c:if>

				</span>
			</td>
			<td align="right">
				<img alt="" src="<%= weather.getIconURL() %>" />
			</td>
		</tr>

<%
	}
}
%>

</table>

<br />

<liferay-ui:message key="city-or-zip-code" />

<input name="q" size="23" type="text" />

<input type="submit" value="<liferay-ui:message key="search" />" />

</form>

<br />

<liferay-ui:message key="powered-by" /> <a href="http://www.openweathermap.org" target="_blank">Open Weather Map</a>

<c:if test="<%= windowState.equals(WindowState.MAXIMIZED) %>">
	<aui:script>
		Liferay.Util.focusFormField(document.<portlet:namespace />fm.q);
	</aui:script>
</c:if>