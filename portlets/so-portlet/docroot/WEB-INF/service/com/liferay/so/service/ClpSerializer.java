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

package com.liferay.so.service;

import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ClassLoaderObjectInputStream;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.BaseModel;

import com.liferay.so.model.FavoriteSiteClp;
import com.liferay.so.model.MemberRequestClp;
import com.liferay.so.model.ProjectsEntryClp;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brian Wing Shun Chan
 */
public class ClpSerializer {
	public static String getServletContextName() {
		if (Validator.isNotNull(_servletContextName)) {
			return _servletContextName;
		}

		synchronized (ClpSerializer.class) {
			if (Validator.isNotNull(_servletContextName)) {
				return _servletContextName;
			}

			try {
				ClassLoader classLoader = ClpSerializer.class.getClassLoader();

				Class<?> portletPropsClass = classLoader.loadClass(
						"com.liferay.util.portlet.PortletProps");

				Method getMethod = portletPropsClass.getMethod("get",
						new Class<?>[] { String.class });

				String portletPropsServletContextName = (String)getMethod.invoke(null,
						"so-portlet-deployment-context");

				if (Validator.isNotNull(portletPropsServletContextName)) {
					_servletContextName = portletPropsServletContextName;
				}
			}
			catch (Throwable t) {
				if (_log.isInfoEnabled()) {
					_log.info(
						"Unable to locate deployment context from portlet properties");
				}
			}

			if (Validator.isNull(_servletContextName)) {
				try {
					String propsUtilServletContextName = PropsUtil.get(
							"so-portlet-deployment-context");

					if (Validator.isNotNull(propsUtilServletContextName)) {
						_servletContextName = propsUtilServletContextName;
					}
				}
				catch (Throwable t) {
					if (_log.isInfoEnabled()) {
						_log.info(
							"Unable to locate deployment context from portal properties");
					}
				}
			}

			if (Validator.isNull(_servletContextName)) {
				_servletContextName = "so-portlet";
			}

			return _servletContextName;
		}
	}

	public static Object translateInput(BaseModel<?> oldModel) {
		Class<?> oldModelClass = oldModel.getClass();

		String oldModelClassName = oldModelClass.getName();

		if (oldModelClassName.equals(FavoriteSiteClp.class.getName())) {
			return translateInputFavoriteSite(oldModel);
		}

		if (oldModelClassName.equals(MemberRequestClp.class.getName())) {
			return translateInputMemberRequest(oldModel);
		}

		if (oldModelClassName.equals(ProjectsEntryClp.class.getName())) {
			return translateInputProjectsEntry(oldModel);
		}

		return oldModel;
	}

	public static Object translateInput(List<Object> oldList) {
		List<Object> newList = new ArrayList<Object>(oldList.size());

		for (int i = 0; i < oldList.size(); i++) {
			Object curObj = oldList.get(i);

			newList.add(translateInput(curObj));
		}

		return newList;
	}

	public static Object translateInputFavoriteSite(BaseModel<?> oldModel) {
		FavoriteSiteClp oldClpModel = (FavoriteSiteClp)oldModel;

		BaseModel<?> newModel = oldClpModel.getFavoriteSiteRemoteModel();

		newModel.setModelAttributes(oldClpModel.getModelAttributes());

		return newModel;
	}

	public static Object translateInputMemberRequest(BaseModel<?> oldModel) {
		MemberRequestClp oldClpModel = (MemberRequestClp)oldModel;

		BaseModel<?> newModel = oldClpModel.getMemberRequestRemoteModel();

		newModel.setModelAttributes(oldClpModel.getModelAttributes());

		return newModel;
	}

	public static Object translateInputProjectsEntry(BaseModel<?> oldModel) {
		ProjectsEntryClp oldClpModel = (ProjectsEntryClp)oldModel;

		BaseModel<?> newModel = oldClpModel.getProjectsEntryRemoteModel();

		newModel.setModelAttributes(oldClpModel.getModelAttributes());

		return newModel;
	}

	public static Object translateInput(Object obj) {
		if (obj instanceof BaseModel<?>) {
			return translateInput((BaseModel<?>)obj);
		}
		else if (obj instanceof List<?>) {
			return translateInput((List<Object>)obj);
		}
		else {
			return obj;
		}
	}

	public static Object translateOutput(BaseModel<?> oldModel) {
		Class<?> oldModelClass = oldModel.getClass();

		String oldModelClassName = oldModelClass.getName();

		if (oldModelClassName.equals(
					"com.liferay.so.model.impl.FavoriteSiteImpl")) {
			return translateOutputFavoriteSite(oldModel);
		}

		if (oldModelClassName.equals(
					"com.liferay.so.model.impl.MemberRequestImpl")) {
			return translateOutputMemberRequest(oldModel);
		}

		if (oldModelClassName.equals(
					"com.liferay.so.model.impl.ProjectsEntryImpl")) {
			return translateOutputProjectsEntry(oldModel);
		}

		return oldModel;
	}

	public static Object translateOutput(List<Object> oldList) {
		List<Object> newList = new ArrayList<Object>(oldList.size());

		for (int i = 0; i < oldList.size(); i++) {
			Object curObj = oldList.get(i);

			newList.add(translateOutput(curObj));
		}

		return newList;
	}

	public static Object translateOutput(Object obj) {
		if (obj instanceof BaseModel<?>) {
			return translateOutput((BaseModel<?>)obj);
		}
		else if (obj instanceof List<?>) {
			return translateOutput((List<Object>)obj);
		}
		else {
			return obj;
		}
	}

	public static Throwable translateThrowable(Throwable throwable) {
		if (_useReflectionToTranslateThrowable) {
			try {
				UnsyncByteArrayOutputStream unsyncByteArrayOutputStream = new UnsyncByteArrayOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(unsyncByteArrayOutputStream);

				objectOutputStream.writeObject(throwable);

				objectOutputStream.flush();
				objectOutputStream.close();

				UnsyncByteArrayInputStream unsyncByteArrayInputStream = new UnsyncByteArrayInputStream(unsyncByteArrayOutputStream.unsafeGetByteArray(),
						0, unsyncByteArrayOutputStream.size());

				Thread currentThread = Thread.currentThread();

				ClassLoader contextClassLoader = currentThread.getContextClassLoader();

				ObjectInputStream objectInputStream = new ClassLoaderObjectInputStream(unsyncByteArrayInputStream,
						contextClassLoader);

				throwable = (Throwable)objectInputStream.readObject();

				objectInputStream.close();

				return throwable;
			}
			catch (ClassNotFoundException cnfe) {
				if (_log.isInfoEnabled()) {
					_log.info("Do not use reflection to translate throwable");
				}

				_useReflectionToTranslateThrowable = false;
			}
			catch (SecurityException se) {
				if (_log.isInfoEnabled()) {
					_log.info("Do not use reflection to translate throwable");
				}

				_useReflectionToTranslateThrowable = false;
			}
			catch (Throwable throwable2) {
				_log.error(throwable2, throwable2);

				return throwable2;
			}
		}

		Class<?> clazz = throwable.getClass();

		String className = clazz.getName();

		if (className.equals("com.liferay.so.MemberRequestAlreadyUsedException")) {
			return new com.liferay.so.MemberRequestAlreadyUsedException(throwable.getMessage(),
				throwable.getCause());
		}

		if (className.equals("com.liferay.so.MemberRequestInvalidUserException")) {
			return new com.liferay.so.MemberRequestInvalidUserException(throwable.getMessage(),
				throwable.getCause());
		}

		if (className.equals("com.liferay.so.ProjectsEntryEndDateException")) {
			return new com.liferay.so.ProjectsEntryEndDateException(throwable.getMessage(),
				throwable.getCause());
		}

		if (className.equals("com.liferay.so.ProjectsEntryStartDateException")) {
			return new com.liferay.so.ProjectsEntryStartDateException(throwable.getMessage(),
				throwable.getCause());
		}

		if (className.equals("com.liferay.so.NoSuchFavoriteSiteException")) {
			return new com.liferay.so.NoSuchFavoriteSiteException(throwable.getMessage(),
				throwable.getCause());
		}

		if (className.equals("com.liferay.so.NoSuchMemberRequestException")) {
			return new com.liferay.so.NoSuchMemberRequestException(throwable.getMessage(),
				throwable.getCause());
		}

		if (className.equals("com.liferay.so.NoSuchProjectsEntryException")) {
			return new com.liferay.so.NoSuchProjectsEntryException(throwable.getMessage(),
				throwable.getCause());
		}

		return throwable;
	}

	public static Object translateOutputFavoriteSite(BaseModel<?> oldModel) {
		FavoriteSiteClp newModel = new FavoriteSiteClp();

		newModel.setModelAttributes(oldModel.getModelAttributes());

		newModel.setFavoriteSiteRemoteModel(oldModel);

		return newModel;
	}

	public static Object translateOutputMemberRequest(BaseModel<?> oldModel) {
		MemberRequestClp newModel = new MemberRequestClp();

		newModel.setModelAttributes(oldModel.getModelAttributes());

		newModel.setMemberRequestRemoteModel(oldModel);

		return newModel;
	}

	public static Object translateOutputProjectsEntry(BaseModel<?> oldModel) {
		ProjectsEntryClp newModel = new ProjectsEntryClp();

		newModel.setModelAttributes(oldModel.getModelAttributes());

		newModel.setProjectsEntryRemoteModel(oldModel);

		return newModel;
	}

	private static Log _log = LogFactoryUtil.getLog(ClpSerializer.class);
	private static String _servletContextName;
	private static boolean _useReflectionToTranslateThrowable = true;
}