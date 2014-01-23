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

package com.liferay.akismet.util;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
import com.liferay.util.portlet.PortletProps;

import javax.portlet.PortletPreferences;

/**
 * @author Amos Fong
 */
public class PrefsPortletPropsUtil {

	public static boolean getBoolean(long companyId, String name)
		throws SystemException {

		return GetterUtil.getBoolean(getString(companyId, name));
	}

	public static int getInteger(long companyId, String name)
		throws SystemException {

		return GetterUtil.getInteger(getString(companyId, name));
	}

	public static PortletPreferences getPortletPreferences(long companyId)
		throws SystemException {

		return PortletPreferencesLocalServiceUtil.getPreferences(
			companyId, companyId, PortletKeys.PREFS_OWNER_TYPE_COMPANY,
			PortletKeys.PREFS_PLID_SHARED, PortletKeys.AKISMET);
	}

	public static String getString(long companyId, String name)
		throws SystemException {

		PortletPreferences portletPreferences = getPortletPreferences(
			companyId);

		return _getString(portletPreferences, companyId, name);
	}

	private static String _getString(
		PortletPreferences portletPreferences, long companyId, String name) {

		String defaultValue = PortletProps.get(name);

		return portletPreferences.getValue(name, defaultValue);
	}

}