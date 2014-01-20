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
 
<%@ include file="/html/portlet/shopping/init.jsp" %>

<liferay-util:buffer var="html">
	<liferay-util:include page="/html/portlet/shopping/order_search.portal.jsp" />
</liferay-util:buffer>

<%
String namespace = portletDisplay.getNamespace();

String firstNameId = namespace + "firstName";

String lastNameId = namespace + "lastName";

int firstNameStart = _getFormFieldStart(html,firstNameId);
int firstNameEnd = _getFormFieldEnd(html, firstNameStart);

int lastNameStart = _getFormFieldStart(html, lastNameId);
int lastNameEnd = _getFormFieldEnd(html, lastNameStart);

html = _reverseFirstNameLastName(html, firstNameStart, firstNameEnd, lastNameStart, lastNameEnd);
%>

<%= html %>

<%!
private int _getFormFieldEnd(String html, int fromIndex) {
	if (fromIndex < 0) {
		return fromIndex;
	}

	return html.indexOf(_DIV_CLOSE, fromIndex) + _DIV_CLOSE.length();
}

private int _getFormFieldStart(String html, String id) {
	int x = html.indexOf("id=\"" + id + StringPool.QUOTE);

	if (x < 0) {
		return x;
	}

	return html.lastIndexOf(_DIV_OPEN, x);
}

private String _reverseFirstNameLastName(String html, int firstNameStart, int firstNameEnd, int lastNameStart, int lastNameEnd) {
	if ((firstNameStart < 0) || (firstNameEnd < 0) || (lastNameStart < 0) || (lastNameEnd < 0)) {
		return html;
	}

	StringBundler sb = new StringBundler(4);

	sb.append(html.substring(0, firstNameStart));
	sb.append(html.substring(lastNameStart, lastNameEnd));
	sb.append(html.substring(firstNameStart, firstNameEnd));
	sb.append(html.substring(lastNameEnd));

	return sb.toString();
}

private static final String _DIV_CLOSE = "</div>";

private static final String _DIV_OPEN = "<div";
%>