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

package com.liferay.emailtombfilter.hook.sanitizer;

import com.liferay.emailtombfilter.util.PortletPropsValues;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.sanitizer.Sanitizer;
import com.liferay.portal.kernel.sanitizer.SanitizerException;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.StreamUtil;
import com.liferay.portlet.messageboards.model.MBMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Douglas Wong
 */
public class EmailToMBMessageFilterSanitizerImpl implements Sanitizer {

	@Override
	public byte[] sanitize(
		long companyId, long groupId, long userId, String className,
		long classPK, String contentType, String[] modes, byte[] bytes,
		Map<String, Object> options) {

		return bytes;
	}

	@Override
	public void sanitize(
			long companyId, long groupId, long userId, String className,
			long classPK, String contentType, String[] modes,
			InputStream inputStream, OutputStream outputStream,
			Map<String, Object> options)
		throws SanitizerException {

		try {
			StreamUtil.transfer(inputStream, outputStream);
		}
		catch (IOException ioe) {
			throw new SanitizerException(ioe);
		}
	}

	@Override
	public String sanitize(
		long companyId, long groupId, long userId, String className,
		long classPK, String contentType, String[] modes, String s,
		Map<String, Object> options) {

		if (!className.equals(MBMessage.class.getName())) {
			return s;
		}

		if (options == null) {
			return s;
		}

		Object emailPartToMBMessageBody = options.get(
			"emailPartToMBMessageBody");

		if ((emailPartToMBMessageBody == null) ||
			(emailPartToMBMessageBody != Boolean.TRUE)) {

			return s;
		}

		if (!contentType.startsWith(ContentTypes.TEXT_PLAIN)) {
			return s;
		}

		Matcher matcher = _pattern.matcher(s);

		if (!matcher.find()) {
			return s;
		}

		if (_log.isDebugEnabled()) {
			_log.debug("Sanitizing " + className + "#" + classPK);
		}

		s = s.substring(0, matcher.start());

		return s.trim();
	}

	private static final Pattern _pattern = Pattern.compile(
		PortletPropsValues.PLAIN_TEXT_EMAIL_REGEXP);

	private static Log _log = LogFactoryUtil.getLog(
		EmailToMBMessageFilterSanitizerImpl.class);

}