<%--
/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This file is part of Liferay Social Office. Liferay Social Office is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Liferay Social Office is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Liferay Social Office. If not, see http://www.gnu.org/licenses/agpl-3.0.html.
 */
--%>

<%@ include file="/sites/init.jsp" %>

<%
String tabs1 = ParamUtil.getString(request, "tabs1");

String keywords = ParamUtil.getString(request, "keywords");

List<Group> groups = null;
int groupsCount = 0;

if (tabs1.equals("my-sites")) {
	groups = SitesUtil.getVisibleSites(themeDisplay.getCompanyId(), themeDisplay.getUserId(), keywords, true, 0, maxResultSize);
	groupsCount = SitesUtil.getVisibleSitesCount(themeDisplay.getCompanyId(), themeDisplay.getUserId(), keywords, true);

	if (groupsCount == 0) {
		tabs1 = "all-sites";

		groups = SitesUtil.getVisibleSites(themeDisplay.getCompanyId(), themeDisplay.getUserId(), keywords, false, 0, maxResultSize);
		groupsCount = SitesUtil.getVisibleSitesCount(themeDisplay.getCompanyId(), themeDisplay.getUserId(), keywords, false);
	}
}
else if (tabs1.equals("my-favorites")) {
	groups = SitesUtil.getFavoriteSitesGroups(themeDisplay.getUserId(), keywords, 0, maxResultSize);
	groupsCount = SitesUtil.getFavoriteSitesGroupsCount(themeDisplay.getUserId(), keywords);
}
else {
	groups = SitesUtil.getVisibleSites(themeDisplay.getCompanyId(), themeDisplay.getUserId(), keywords, false, 0, maxResultSize);
	groupsCount = SitesUtil.getVisibleSitesCount(themeDisplay.getCompanyId(), themeDisplay.getUserId(), keywords, false);
}
%>

<div class="so-sites-directory" id="<portlet:namespace />directory">
	<liferay-ui:header title="directory" />

	<div class="search">
		<div class="buttons-left">
			<input id="<portlet:namespace />dialogKeywords" size="30" type="text" value="<%= HtmlUtil.escape(keywords) %>" />

			<span class="sites-tabs">
				<aui:select label="" name="tabs1">
					<aui:option label="all-sites" selected='<%= tabs1.equals("all-sites") %>' value="all-sites" />
					<aui:option label="my-sites" selected='<%= tabs1.equals("my-sites") %>' value="my-sites" />
					<aui:option label="my-favorites" selected='<%= tabs1.equals("my-favorites") %>' value="my-favorites" />
				</aui:select>
			</span>
		</div>

		<div class="buttons-right">
			<aui:button cssClass="previous" disabled="<%= true %>" value="previous" />

			<aui:button cssClass="next" disabled="<%= groupsCount < maxResultSize %>" value="next" />
		</div>

		<div style="clear: both;"><!-- --></div>
	</div>

	<ul class="directory-list"></ul>

	<aui:button-row>
		<div class="directory-navigation buttons-left">
			<span class="page-indicator">
				<%= LanguageUtil.format(pageContext, "page-x-of-x", new String[] {"<span class=\"current\">1</span>", "<span class=\"total\">" + String.valueOf((int)Math.ceil(groupsCount / (float)maxResultSize)) + "</span>"}) %>
			</span>
		</div>
	</aui:button-row>
</div>

<aui:script use="datatype-number,liferay-so-site-list">
	var directoryContainer = A.one('#<portlet:namespace />directory');

	var navigationContainer = directoryContainer.all('.directory-navigation');

	var currentPageNode = directoryContainer.one('.page-indicator .current');
	var totalPageNode = directoryContainer.one('.page-indicator .total');

	var keywordsInput = directoryContainer.one('#<portlet:namespace />dialogKeywords');
	var nextButton = directoryContainer.one('.search .next');
	var previousButton = directoryContainer.one('.search .previous');
	var sitesTabsSelect = directoryContainer.one('select[name=<portlet:namespace />tabs1]');

	var directoryList = new Liferay.SO.SiteList(
		{
			inputNode: '#<portlet:namespace />directory #<portlet:namespace />dialogKeywords',
			listNode: '#<portlet:namespace />directory .directory-list',
			minQueryLength: 0,
			requestTemplate: function(query) {
				return {
					<portlet:namespace />directory: true,
					<portlet:namespace />end: <%= maxResultSize %>,
					<portlet:namespace />keywords: query,
					<portlet:namespace />searchTab: sitesTabsSelect.get('value'),
					<portlet:namespace />start: 0
				}
			},
			resultTextLocator: function(response) {
				var result = '';

				if (typeof response.toString != 'undefined') {
					result = response.toString();
				}
				else if (typeof response.responseText != 'undefined') {
					result = response.responseText;
				}

				return result;
			},
			source: Liferay.SO.Sites.createDataSource('<portlet:resourceURL id="getSites" />', '<portlet:namespace />')
		}
	);

	Liferay.SO.Sites.createDirectoryList(directoryList);

	directoryList.sendRequest();

	var updateDirectoryList = function(event) {
		var data = A.JSON.parse(event.data.responseText);

		var results = data.sites;
		var count = data.count;

		var options = data.options;

		var buffer = [];

		if (results.length == 0) {
			buffer.push(
				'<li class="empty"><liferay-ui:message key="there-are-no-results" unicode="<%= true %>" /></li>'
			);
		}
		else {
			var siteTemplate =
				'<li class="{classNames}">' +
					'{favoriteHtml}' +
					'{joinHtml}' +
					'{leaveHtml}' +
					'{requestHtml}' +
					'{requestedHtml}' +
					'{deleteHtml}' +
					'<span class="name">{siteName}</span>' +
					'<span class="description">{siteDescription}</span>'
				'</li>';

			buffer.push(
				A.Array.map(
					results,
					function(result, index) {
						var classNames = [];
						var joinHtml = '';

						if (result.socialOfficeGroup) {
							classNames.push('social-office-enabled');
						}

						if (!result.joinUrl) {
							classNames.push('member');
						}

						if ((index % 2) == 1) {
							classNames.push('alt');
						}

						var name = result.name;

						if (result.publicLayoutsURL) {
							name = '<a href="' + result.publicLayoutsURL + '">' + name + '</a>';

							if (result.privateLayoutsURL) {
								name += '<a class="private-pages" href="' + result.privateLayoutsURL + '"> (<liferay-ui:message key="private-pages" />)</a>';
							}
						}
						else if (!result.publicLayoutsURL && result.privateLayoutsURL) {
							name = '<a href="' + result.privateLayoutsURL + '">' + name + '</a>';
						}

						return A.Lang.sub(
							siteTemplate,
							{
								classNames: classNames.join(' '),
								deleteHtml: (result.deleteURL ? '<span class="action delete"><a class="delete-site" href="' + result.deleteURL + '"><liferay-ui:message key="delete" /></a></span>' : '<span class="action-not-allowed"></span>'),
								joinHtml: (result.joinUrl ? '<span class="action join"><a class="join-site" href="' + result.joinUrl + '"><liferay-ui:message key="join" /></a></span>' : ''),
								leaveHtml: (result.leaveUrl ? '<span class="action leave"><a class="leave-site" href="' + result.leaveUrl + '"><liferay-ui:message key="leave" /></a></span>' : ''),
								requestHtml: (result.requestUrl ? '<span class="action request"><a class="request-site" href="' + result.requestUrl + '"><liferay-ui:message key="request-membership" /></a></span>' : ''),
								requestedHtml: (result.membershipRequested ? '<span class="action requested"><a><liferay-ui:message key="membership-requested" /></a></span>' : ''),
								siteDescription: result.description,
								siteName: name,
								favoriteHtml: (result.favoriteURL ? '<span class="action favorite"><a class="favorite-site" href="' + result.favoriteURL + '"><liferay-ui:message key="favorite" /></a></span>' : '<span class="action unfavorite"><a class="unfavorite-site" href="' + result.unfavoriteURL + '"><liferay-ui:message key="unfavorite" /></a></span>')
							}

						);
					}
				).join('')
			);
		}

		this._listNode.html(buffer.join(''));

		var currentPage = Math.floor(options.start/<%= maxResultSize %>) + 1;
		var totalPage = Math.ceil(count/<%= maxResultSize %>);

		currentPageNode.html(currentPage);
		totalPageNode.html(totalPage);

		if (currentPage <= 1) {
			Liferay.SO.Sites.disableButton(previousButton);
		}
		else {
			Liferay.SO.Sites.enableButton(previousButton);
		}

		if (currentPage >= totalPage) {
			Liferay.SO.Sites.disableButton(nextButton);
		}
		else {
			Liferay.SO.Sites.enableButton(nextButton);
		}
	};

	directoryList.on('results', updateDirectoryList);

	var getRequestTemplate = function(targetPage) {
		var start = (targetPage - 1) * <%= maxResultSize %>;
		var end = start + <%= maxResultSize %>;

		return function(query) {
			return {
				<portlet:namespace />directory: true,
				<portlet:namespace />end: end,
				<portlet:namespace />keywords: query,
				<portlet:namespace />searchTab: sitesTabsSelect.get('value'),
				<portlet:namespace />start: start
			}
		};
	};

	sitesTabsSelect.on(
		'change',
		function(event) {
			directoryList.sendRequest();
		}
	);

	nextButton.on(
		'click',
		function(event) {
			var currentPage = A.DataType.Number.parse(currentPageNode.html());
			var totalPage = A.DataType.Number.parse(totalPageNode.html());

			var targetPage = currentPage + 1;

			if (targetPage > totalPage) {
				return;
			}

			directoryList.sendRequest(keywordsInput.get('value'), getRequestTemplate(targetPage));
		}
	);

	previousButton.on(
		'click',
		function(event) {
			var currentPage = A.DataType.Number.parse(currentPageNode.html());

			var targetPage = currentPage - 1;

			if (targetPage <= 0) {
				return;
			}

			directoryList.sendRequest(keywordsInput.get('value'), getRequestTemplate(targetPage));
		}
	);

	directoryContainer.one('.directory-list').delegate(
		'click',
		function(event) {
			event.preventDefault();

			var currentPage = A.DataType.Number.parse(currentPageNode.html());

			var currentTargetClass = event.currentTarget.getAttribute('class');

			if ((currentTargetClass == 'delete-site') || (currentTargetClass == "leave-site") || (currentTargetClass == "join-site") || (currentTargetClass == "request-site")) {
				var confirmMessage = '';

				var siteAction = '';

				var siteNode = event.currentTarget.ancestor('li');

				var siteName = siteNode.one('.name a');

				if (!siteName) {
					siteName = siteNode.one('.name');
				}

				var unescapedSiteName = Liferay.Util.unescapeHTML(siteName.getContent());

				if (currentTargetClass == "leave-site") {
					confirmMessage = '<%= LanguageUtil.format(pageContext, "are-you-sure-you-want-to-leave-x", new String[] {"' + unescapedSiteName + '"}) %>';
					siteAction = '<%= LanguageUtil.format(pageContext, "you-left-x", new String[] {"' + unescapedSiteName + '"}) %>';
				}
				else if (currentTargetClass == "join-site") {
					confirmMessage = '<%= LanguageUtil.format(pageContext, "are-you-sure-you-want-to-join-x", new String[] {"' + unescapedSiteName + '"}) %>';
					siteAction = '<%= LanguageUtil.format(pageContext, "you-joined-x", new String[] {"' + unescapedSiteName + '"}) %>';
				}
				else if (currentTargetClass == "request-site") {
					confirmMessage = '<%= LanguageUtil.format(pageContext, "this-is-a-restricted-site-do-you-want-to-send-a-membership-request-to-x", new String[] {"' + unescapedSiteName + '"}) %>';
					siteAction = '<%= LanguageUtil.get(pageContext, "your-membership-request-has-been-sent") %>';
				}
				else {
					confirmMessage = '<%= LanguageUtil.format(pageContext, "are-you-sure-you-want-to-delete-x", new String[] {"' + unescapedSiteName + '"}) %>';
					siteAction = '<%= LanguageUtil.format(pageContext, "you-deleted-x", new String[] {"' + unescapedSiteName + '"}) %>';
				}

				if (confirm(confirmMessage)) {
					A.io.request(
						event.currentTarget.get('href'),
						{
							after: {
								success: function(event, id, obj) {
									siteName.insert(siteAction, 'replace');

									var updateSites = function() {
										Liferay.SO.Sites.updateSites(false, keywordsInput.get('value'), getRequestTemplate(currentPage));
									}

									setTimeout(updateSites, 2000);

									<c:if test="<%= themeDisplay.isStatePopUp() %>">
										if (window.parent) {
											Liferay.Util.getOpener().Liferay.Portlet.refresh('#p_p_id_5_WAR_soportlet_');
										}
									</c:if>
								}
							}
						}
					);
				}
			}
			else {
				A.io.request(
					event.currentTarget.get('href'),
					{
						after: {
							success: function(event, id, obj) {
								Liferay.SO.Sites.updateSites(false, keywordsInput.get('value'), getRequestTemplate(currentPage));

								<c:if test="<%= themeDisplay.isStatePopUp() %>">
									if (window.parent) {
										Liferay.Util.getOpener().Liferay.Portlet.refresh('#p_p_id_5_WAR_soportlet_');
									}
								</c:if>
							}
						}
					}
				);
			}
		},
		'.action a'
	);

	<c:if test="<%= themeDisplay.isStatePopUp() %>">
		directoryContainer.one('.directory-list').delegate(
			'click',
			function(event) {
				if (window.parent) {
					event.preventDefault();

					var uri = event.currentTarget.getAttribute('href');

					Liferay.Util.getOpener().location.href = uri;
				}
			},
			'.name a'
		);
	</c:if>
</aui:script>