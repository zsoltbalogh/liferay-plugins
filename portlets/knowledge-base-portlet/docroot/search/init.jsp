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

<%
boolean showKBArticleAuthorColumn = GetterUtil.getBoolean(portletPreferences.getValue("showKBArticleAuthorColumn", null));
boolean showKBArticleCreateDateColumn = GetterUtil.getBoolean(portletPreferences.getValue("showKBArticleCreateDateColumn", null));
boolean showKBArticleModifiedDateColumn = GetterUtil.getBoolean(portletPreferences.getValue("showKBArticleModifiedDateColumn", null));
boolean showKBArticleViewsColumn = GetterUtil.getBoolean(portletPreferences.getValue("showKBArticleViewsColumn", null));

boolean enableKBArticleDescription = GetterUtil.getBoolean(portletPreferences.getValue("enableKBArticleDescription", null));
boolean enableKBArticleRatings = GetterUtil.getBoolean(portletPreferences.getValue("enableKBArticleRatings", null));
boolean showKBArticleAssetEntries = GetterUtil.getBoolean(portletPreferences.getValue("showKBArticleAssetEntries", null));
boolean enableKBArticleKBComments = GetterUtil.getBoolean(portletPreferences.getValue("enableKBArticleKBComments", null));
boolean showKBArticleKBComments = GetterUtil.getBoolean(portletPreferences.getValue("showKBArticleKBComments", null));
boolean enableKBArticleViewCountIncrement = GetterUtil.getBoolean(portletPreferences.getValue("enableKBArticleViewCountIncrement", null));

boolean enableRSS = !PortalUtil.isRSSFeedsEnabled() ? false : GetterUtil.getBoolean(portletPreferences.getValue("enableRss", null), true);
int rssDelta = GetterUtil.getInteger(portletPreferences.getValue("rssDelta", StringPool.BLANK), SearchContainer.DEFAULT_DELTA);
String rssDisplayStyle = portletPreferences.getValue("rssDisplayStyle", RSSUtil.DISPLAY_STYLE_DEFAULT);
String rssFeedType = portletPreferences.getValue("rssFeedType", RSSUtil.FEED_TYPE_DEFAULT);
%>