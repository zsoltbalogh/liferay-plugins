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

<%@ include file="/html/taglib/ui/social_bookmark/init.jsp" %>

<a class="addthis_button" href="http://www.addthis.com/bookmark.php?v=300&amp;pubid=xa-50994fd636c849d6">
	<img alt="Bookmark and Share" height="16" src="http://s7.addthis.com/static/btn/v2/lg-share-en.gif" style="border:0" width="125" />
</a>

<liferay-util:html-bottom outputKey="addthis">
	<script src="<%= HttpUtil.getProtocol(request) %>://s7.addthis.com/js/300/addthis_widget.js#pubid=xa-50994fd636c849d6" type="text/javascript"></script>
</liferay-util:html-bottom>