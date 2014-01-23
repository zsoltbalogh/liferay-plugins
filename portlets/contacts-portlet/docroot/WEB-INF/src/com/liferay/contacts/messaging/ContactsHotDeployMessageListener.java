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

package com.liferay.contacts.messaging;

import com.liferay.contacts.service.ClpSerializer;
import com.liferay.contacts.util.ContactsExtensionsUtil;
import com.liferay.portal.kernel.messaging.HotDeployMessageListener;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.util.ClassResolverUtil;
import com.liferay.portal.kernel.util.MethodKey;
import com.liferay.portal.kernel.util.PortletClassInvoker;

/**
 * @author Ryan Park
 */
public class ContactsHotDeployMessageListener extends HotDeployMessageListener {

	public ContactsHotDeployMessageListener(String... servletContextNames) {
		super(servletContextNames);
	}

	@Override
	protected void onDeploy(Message message) throws Exception {
		if (_registerMethodKey == null) {
			try {
				_registerMethodKey = new MethodKey(
					ClassResolverUtil.resolveByPortletClassLoader(
						"com.liferay.chat.util.ChatExtensionsUtil",
						"chat-portlet"),
					"register", String.class, String.class);
			}
			catch (RuntimeException re) {
				return;
			}
		}

		PortletClassInvoker.invoke(
			false, "1_WAR_chatportlet", _registerMethodKey,
			ClpSerializer.getServletContextName(), "/chat/view.jsp");
	}

	@Override
	protected void onUndeploy(Message message) throws Exception {
		ContactsExtensionsUtil.unregister(
			message.getString("servletContextName"));
	}

	private MethodKey _registerMethodKey;

}