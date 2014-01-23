/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation; version 3.0 of the License.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */

package com.liferay.portal.mobile.device.wurfl;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.mobile.device.Device;
import com.liferay.portal.kernel.mobile.device.DeviceCapabilityFilter;
import com.liferay.portal.kernel.mobile.device.DeviceRecognitionProvider;
import com.liferay.portal.kernel.mobile.device.KnownDevices;
import com.liferay.portal.kernel.mobile.device.UnknownDevice;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.wurfl.core.WURFLManager;

/**
 * @author Milen Dyankov
 * @author Michael C. Han
 */
public class WURFLDeviceRecognitionProvider
	implements DeviceRecognitionProvider {

	@Override
	public Device detectDevice(HttpServletRequest request) {
		WURFLManager wurflManager = _wurflHolderImpl.getWURFLManager();

		if (wurflManager == null) {
			if (_log.isWarnEnabled()) {
				_log.warn("WURFL database has not initialized");
			}

			return UnknownDevice.getInstance();
		}

		net.sourceforge.wurfl.core.Device wurflDevice =
			wurflManager.getDeviceForRequest(request);

		Device device = null;

		if (wurflDevice != null) {
			Map<String, String> capabilities = wurflDevice.getCapabilities();

			if ((capabilities != null) && !capabilities.isEmpty()) {
				device = new WURFLDevice(capabilities, _deviceCapabilityFilter);
			}
			else {
				device = UnknownDevice.getInstance();
			}
		}

		return device;
	}

	@Override
	public KnownDevices getKnownDevices() {
		return _knownDevices;
	}

	@Override
	public void reload() throws Exception {
		_wurflHolderImpl.reload();

		_knownDevices.reload();
	}

	@Override
	public void setDeviceCapabilityFilter(
		DeviceCapabilityFilter deviceCapabilityFilter) {

		_deviceCapabilityFilter = deviceCapabilityFilter;
	}

	public void setKnownDevices(KnownDevices knownDevices) {
		_knownDevices = knownDevices;
	}

	public void setWurflHolderImpl(WURFLHolderImpl wurflHolderImpl) {
		_wurflHolderImpl = wurflHolderImpl;
	}

	private static Log _log = LogFactoryUtil.getLog(
		WURFLDeviceRecognitionProvider.class);

	private DeviceCapabilityFilter _deviceCapabilityFilter;
	private KnownDevices _knownDevices;
	private WURFLHolderImpl _wurflHolderImpl;

}