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

package com.liferay.testhook.hook.service.impl;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalService;
import com.liferay.portal.service.UserLocalServiceWrapper;
import com.liferay.testhook.hook.model.impl.TestHookUserImpl;

/**
 * @author Brian Wing Shun Chan
 */
public class TestHookUserLocalServiceImpl extends UserLocalServiceWrapper {

	public TestHookUserLocalServiceImpl(UserLocalService userLocalService) {
		super(userLocalService);
	}

	@Override
	public User getUserByEmailAddress(long companyId, String emailAddress)
		throws PortalException, SystemException {

		System.out.println(
			"Called TestHookUserLocalServiceImpl.getUserByEmailAddress(" +
				companyId + ", " + emailAddress + ")");

		User user = super.getUserByEmailAddress(companyId, emailAddress);

		return new TestHookUserImpl(user);
	}

	@Override
	public User getUserById(long userId)
		throws PortalException, SystemException {

		System.out.println(
			"Called TestHookUserLocalServiceImpl.getUserById(" + userId + ")");

		User user = super.getUserById(userId);

		return new TestHookUserImpl(user);
	}

}