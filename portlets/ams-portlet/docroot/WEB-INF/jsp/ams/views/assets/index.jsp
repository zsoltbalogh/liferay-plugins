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

<%@ include file="/WEB-INF/jsp/ams/views/init.jsp" %>

<portlet:renderURL var="editAssetURL">
	<portlet:param name="controller" value="assets" />
	<portlet:param name="action" value="edit" />
	<portlet:param name="format" value="html" />
</portlet:renderURL>

<aui:button-row>
	<aui:button href="${editAssetURL}" value="add-asset" />
</aui:button-row>

<portlet:renderURL var="viewAssetsURL">
	<portlet:param name="controller" value="assets" />
	<portlet:param name="action" value="index" />
	<portlet:param name="format" value="html" />
</portlet:renderURL>

<aui:form action="${viewAssetsURL}" method="get" name="fm">
	<aui:fieldset>
		<aui:input inlineField="<%= true %>" label="" name="keywords" size="30" title="search-assets" type="text" />

		<aui:button type="submit" value="search" />
	</aui:fieldset>
</aui:form>

<liferay-ui:search-container emptyResultsMessage="there-are-no-assets" iteratorURL="${alloySearchResult.portletURL}">
	<liferay-ui:search-container-results
		results="${alloySearchResult.baseModels}"
		total="${alloySearchResult.size}"
	/>

	<liferay-ui:search-container-row
		className="com.liferay.ams.model.Asset"
		escapedModel="<%= true %>"
		keyProperty="assetId"
		modelVar="asset"
	>
		<portlet:renderURL var="viewAssetURL">
			<portlet:param name="controller" value="assets" />
			<portlet:param name="action" value="view" />
			<portlet:param name="id" value="${asset.assetId}" />
			<portlet:param name="format" value="html" />
		</portlet:renderURL>

		<liferay-ui:search-container-column-text
			href="${viewAssetURL}"
			name="id"
			property="assetId"
		/>

		<liferay-ui:search-container-column-text
			href="${viewAssetURL}"
			name="serial-number"
			property="serialNumber"
		/>

		<portlet:actionURL var="deleteAssetURL">
			<portlet:param name="controller" value="assets" />
			<portlet:param name="action" value="delete" />
			<portlet:param name="id" value="${asset.assetId}" />
			<portlet:param name="format" value="html" />
		</portlet:actionURL>

		<liferay-ui:search-container-column-text
			href="javascript:submitForm(document.hrefFm, '${deleteAssetURL}&p_p_state=normal');"
			value="X"
		/>
	</liferay-ui:search-container-row>

	<liferay-ui:search-iterator />
</liferay-ui:search-container>