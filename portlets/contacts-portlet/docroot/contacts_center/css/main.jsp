<%--
/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
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

<%@ include file="/css_init.jsp" %>

$border-color: #EBEBEB;
$section-header-bg: #CCE6F7;

#wrapper .contacts-portlet .panel-page-body {
	padding: 0;
}

.contacts-portlet {
	.clear {
		clear: both;
	}

	.aui-column-content, .aui-column-content-center {
		padding: 0;
	}

	.contact-group-filter {
		font-size: 14px;
		padding: 5px 8px;

		select {
			height: 24px;
		}
	}

	.contacts-search {
		float: left;
		width: 30%;

		.contacts-search-content {
			height: 28px;
			padding: 5px 0;
			position: relative;

			.aui-field-label {
				display: none;
			}

			.search-input {
				left: 5px;
				position: absolute;
				right: 5px;
			}
		}
	}

	.lfr-contact-grid-item {
		border-bottom: 1px solid $border-color;
		display: block;
		white-space: nowrap;

		.lfr-contact-thumb {
			float: left;
		}

		.lfr-contact-thumb img {
			width: 100px;
		}

		.lfr-contact-info {
			margin-left: 100px;

			&.no-icon {
				margin-left: 0;
			}
		}
	}

	.lfr-contact-info {
		.lfr-contact-name {
			font-weight: bold;
		}

		.lfr-contact-extra {
			color: #777;
		}
	}

	.contacts-result {
		bottom: 0;
		position: absolute;
		top: 0;
		width: 30%;
		overflow-y: scroll;

		.lfr-contact-grid-item {
			cursor: pointer;
			height: 35px;
			overflow: hidden;
			padding: 5px;

			.lfr-contact-thumb img {
				width: 35px;
			}

			.lfr-contact-info {
				margin-left: 40px;
				font-size: 1em;
			}
		}

		.lfr-contact-grid-item:hover {
			background-color: #E8EFF4;
		}

		.more-results {
			background-color: #E8EFF4;
			border: 1px solid #CCC;
			margin: 5px;
			padding: 2px;
			text-align: center;
		}

		.empty {
			background: url(<%= themeImagesPath %>/messages/alert.png) no-repeat 0 50%;
			margin: 5px;
			padding-left: 25px;
		}

		.lastNameAnchor a {
			background: $section-header-bg;
			border-bottom: 1px solid $border-color;
			cursor: default;
			display: block;
			padding: 1px 5px;
		}

		a:hover {
			text-decoration: none;
		}
	}

	.my-contacts {
		.lfr-contact-grid-item {
			height: 35px;
			overflow: hidden;
			padding: 5px;

			.lfr-contact-thumb img {
				width: 35px;
			}

			.lfr-contact-info {
				margin-left: 40px;
			}
		}
	}

	.contacts-home {
		padding: 5px;
	}

	.contacts-profile {
		border-bottom-width: 1px;
		padding: 20px 10px 5px;
		position: relative;

		.lfr-contact-grid-item {
			border-bottom-width: 0;
			display: inline-block;
		}

		.lfr-contact-name a {
			font-size: 2em;
		}

		.lfr-contact-job-title {
			color: #777;
			font-size: 1.25em;
			font-weight: bold;
		}

		.social-relations {
			position: absolute;
			right: 0;
			text-align: right;
			top: 30px;
			width: auto;

			.lfr-asset-metadata {
				display: none;
			}
		}

		.aui-column-content {
			padding: 0 10px;
		}

		.aui-column:before {
			border-width: 0;
		}
	}

	.contacts-result-container .contacts-container {
		margin-left: 30%;
		min-height: 500px;

		.contacts-profile {
			border-bottom-width: 0;
			padding-top: 10px;
		}
	}

	.contacts-result-container-content {
		.contacts-profile {
			.lfr-contact-info {
				padding: 8px;
			}

			.social-relations {
				background: $section-header-bg;
				position: static;
				padding: 5px;
				left: 0;
				margin: 5px -10px 10px;
				top: 0;
			}
		}
	}

	.contacts-action-content span {
		padding-right: 5px;
	}

	.lfr-user-info-container {
		min-height: 10px;
		margin-bottom: 10px;
	}

	.section {
		border-bottom: 1px solid #E7E7E7;
		clear: both;
		padding: 10px;
		zoom: 1;

		&:after {
			content: "";
			display: block;
			clear: both;
		}

		h3 {
			color: #B4BEC8;
			float: left;
			font-size: 1em;
			margin: 0;
			text-align: right;
			width: 100px;
		}

		ul {
			margin: 0 0 0 105px;
			padding: 0;
		}
	}

	.property-type {
		font-weight: bold;
	}

	.property-list li {
		list-style: none;
	}

	.user-information-title {
		color: #666;
		font-size: 1.5em;
		text-align: right;
		width: 110px;
	}

	.lfr-user-comments {
		float: none;
		width: 100%;
	}

	.contacts-center-home {
		.contacts-center-home-content {
			padding: 10px;
		}

		.header-title {
			margin: 0px;
		}

		.contacts-count {
			font-weight: bold;
		}

		.contacts-count, .contacts-center-introduction {
			margin: 5px 0;
		}
	}

	.export-group {
		margin-top: 1em;
	}

	.lfr-user-profile-preferences {
		margin-left: 20px;
	}

	.lfr-user-action-confirm a {
		background: url(<%= themeImagesPath %>/common/activate.png) no-repeat;
	}

	.lfr-user-action-ignore a {
		background: url(<%= themeImagesPath %>/common/deactivate.png) no-repeat;
	}

	.lfr-asset-metadata {
		margin-bottom: 5px;

		.lfr-asset-coworker, .lfr-asset-follower, .lfr-asset-following {
			padding: 0 20px
		}

		.lfr-asset-coworker {
			background: url(<%= themeImagesPath %>/social/coworker.png) no-repeat;
		}

		.lfr-asset-follower {
			background: url(<%= themeImagesPath %>/social/follower.png) no-repeat;
		}

		.lfr-asset-following {
			background: url(<%= themeImagesPath %>/social/following.png) no-repeat;
		}
	}

	.members-container {
		clear: both;
		margin: 1em 0;
	}

	.lfr-members-grid-item {
		float: left;
		height: 50px;
		margin-bottom: 10px;
		max-width: 300px;
		min-width: 200px;
		overflow: hidden;
		width: 25%;

		.lfr-members-thumb {
			float: left;
			margin: 0 5px;

			a {
				clip: rect(0 50px 50px 0);
				position: absolute;
			}

			img {
				width: 50px;
			}
		}

		.lfr-user-data-info {
			margin-left: 60px;
		}

		.lfr-user-data-name {
			font-weight: bold;
		}

		.lfr-user-data-name, .lfr-user-data-job-title, .lfr-user-data-extra {
			white-space: nowrap
		}
	}

	.edit-profile {
		cursor: pointer;
	}

	.lfr-panel, .taglib-header {
		margin-bottom: 1em;
	}

	.lfr-search-container {
		margin-top: 1em;
	}
}