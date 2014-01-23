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

package com.liferay.so.hook.upgrade.v2_0_2;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.ClassResolverUtil;
import com.liferay.portal.kernel.util.MethodKey;
import com.liferay.portal.kernel.util.PortalClassInvoker;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.LayoutSet;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.service.LayoutSetLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.social.model.SocialRelationConstants;
import com.liferay.portlet.social.service.SocialRelationLocalServiceUtil;
import com.liferay.so.util.LayoutSetPrototypeUtil;
import com.liferay.so.util.RoleConstants;
import com.liferay.so.util.SocialOfficeConstants;
import com.liferay.so.util.SocialOfficeUtil;

import java.util.List;

/**
 * @author Jonathan Lee
 */
public class UpgradeUser extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		List<User> users = UserLocalServiceUtil.getUsers(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		for (User user : users) {
			try {
				if (user.isDefaultUser()) {
					continue;
				}

				Group group = user.getGroup();

				LayoutSet layoutSet = LayoutSetLocalServiceUtil.getLayoutSet(
					group.getGroupId(), false);

				String themeId = layoutSet.getThemeId();

				if (!themeId.equals("so_WAR_sotheme")) {
					return;
				}

				Role role = RoleLocalServiceUtil.getRole(
					user.getCompanyId(), RoleConstants.SOCIAL_OFFICE_USER);

				UserLocalServiceUtil.addRoleUsers(
					role.getRoleId(), new long[] {user.getUserId()});

				updateUserGroup(group);
				updateSocialRelations(user);
			}
			catch (Exception e) {
			}
		}
	}

	protected void updateSocialRelations(User user) throws Exception {
		List<User> socialUsers = UserLocalServiceUtil.getSocialUsers(
			user.getUserId(), SocialRelationConstants.TYPE_BI_FRIEND,
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);

		for (User socialUser : socialUsers) {
			SocialRelationLocalServiceUtil.addRelation(
				user.getUserId(), socialUser.getUserId(),
				SocialRelationConstants.TYPE_BI_CONNECTION);
		}
	}

	protected void updateUserGroup(Group group) throws Exception {
		LayoutLocalServiceUtil.deleteLayouts(
			group.getGroupId(), false, new ServiceContext());

		LayoutSetPrototypeUtil.updateLayoutSetPrototype(
			group, false,
			SocialOfficeConstants.LAYOUT_SET_PROTOTYPE_KEY_USER_PUBLIC);

		LayoutSet publicLayoutSet = LayoutSetLocalServiceUtil.getLayoutSet(
			group.getGroupId(), false);

		PortalClassInvoker.invoke(
			true, _mergeLayoutSetPrototypeLayoutsMethodKey, group,
			publicLayoutSet);

		LayoutLocalServiceUtil.deleteLayouts(
			group.getGroupId(), true, new ServiceContext());

		LayoutSetPrototypeUtil.updateLayoutSetPrototype(
			group, true,
			SocialOfficeConstants.LAYOUT_SET_PROTOTYPE_KEY_USER_PRIVATE);

		LayoutSet privateLayoutSet = LayoutSetLocalServiceUtil.getLayoutSet(
			group.getGroupId(), true);

		PortalClassInvoker.invoke(
			true, _mergeLayoutSetPrototypeLayoutsMethodKey, group,
			privateLayoutSet);

		SocialOfficeUtil.enableSocialOffice(group);
	}

	private static MethodKey _mergeLayoutSetPrototypeLayoutsMethodKey =
		new MethodKey(
			ClassResolverUtil.resolveByPortalClassLoader(
				"com.liferay.portlet.sites.util.SitesUtil"),
			"mergeLayoutSetPrototypeLayouts", Group.class, LayoutSet.class);

}