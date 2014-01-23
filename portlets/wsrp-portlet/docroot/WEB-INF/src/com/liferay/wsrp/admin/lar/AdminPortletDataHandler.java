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

package com.liferay.wsrp.admin.lar;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.lar.BasePortletDataHandler;
import com.liferay.portal.kernel.lar.DataLevel;
import com.liferay.portal.kernel.lar.PortletDataContext;
import com.liferay.portal.kernel.lar.PortletDataHandlerBoolean;
import com.liferay.portal.kernel.lar.PortletDataHandlerControl;
import com.liferay.portal.kernel.lar.StagedModelType;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.service.ServiceContext;
import com.liferay.wsrp.NoSuchConsumerException;
import com.liferay.wsrp.NoSuchConsumerPortletException;
import com.liferay.wsrp.NoSuchProducerException;
import com.liferay.wsrp.model.WSRPConsumer;
import com.liferay.wsrp.model.WSRPConsumerPortlet;
import com.liferay.wsrp.model.WSRPProducer;
import com.liferay.wsrp.service.WSRPConsumerLocalServiceUtil;
import com.liferay.wsrp.service.WSRPConsumerPortletLocalServiceUtil;
import com.liferay.wsrp.service.WSRPProducerLocalServiceUtil;

import java.util.List;

import javax.portlet.PortletPreferences;

/**
 * @author Michael C. Han
 */
public class AdminPortletDataHandler extends BasePortletDataHandler {

	public static final String NAMESPACE = "wsrp";

	public AdminPortletDataHandler() {
		setDataLevel(DataLevel.SITE);
		setDeletionSystemEventStagedModelTypes(
			new StagedModelType(WSRPConsumer.class),
			new StagedModelType(WSRPConsumerPortlet.class),
			new StagedModelType(WSRPProducer.class));
		setExportControls(
			new PortletDataHandlerBoolean(NAMESPACE, "wsrp-producers", false),
			new PortletDataHandlerBoolean(
				NAMESPACE, "wsrp-consumers", true,
				new PortletDataHandlerControl[] {
					new PortletDataHandlerBoolean(
						NAMESPACE, "wsrp-consumer-portlets")
				}));
		setPublishToLiveByDefault(true);
	}

	@Override
	protected PortletPreferences doDeleteData(
			PortletDataContext portletDataContext, String portletId,
			PortletPreferences portletPreferences)
		throws Exception {

		long companyId = portletDataContext.getCompanyId();

		List<WSRPProducer> wsrpProducers =
			WSRPProducerLocalServiceUtil.getWSRPProducers(
				companyId, QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		for (WSRPProducer wsrpProducer : wsrpProducers) {
			WSRPProducerLocalServiceUtil.deleteWSRPProducer(wsrpProducer);
		}

		List<WSRPConsumer> wsrpConsumers =
			WSRPConsumerLocalServiceUtil.getWSRPConsumers(
				companyId, QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		for (WSRPConsumer wsrpConsumer : wsrpConsumers) {
			WSRPConsumerLocalServiceUtil.deleteWSRPConsumer(wsrpConsumer);
		}

		return portletPreferences;
	}

	@Override
	protected String doExportData(
			PortletDataContext portletDataContext, String portletId,
			PortletPreferences portletPreferences)
		throws Exception {

		Element rootElement = addExportDataRootElement(portletDataContext);

		if (portletDataContext.getBooleanParameter(
				NAMESPACE, "wsrp-producers")) {

			Element wsrpProducersElement = rootElement.addElement(
				"wsrp-producers");

			List<WSRPProducer> wsrpProducers =
				WSRPProducerLocalServiceUtil.getWSRPProducers(
					portletDataContext.getCompanyId(), QueryUtil.ALL_POS,
					QueryUtil.ALL_POS);

			for (WSRPProducer wsrpProducer : wsrpProducers) {
				exportWSRPProducer(
					portletDataContext, wsrpProducersElement, wsrpProducer);
			}
		}

		if (portletDataContext.getBooleanParameter(
				NAMESPACE, "wsrp-consumers")) {

			Element wsrpConsumersElement = rootElement.addElement(
				"wsrp-consumers");

			List<WSRPConsumer> wsrpConsumers =
				WSRPConsumerLocalServiceUtil.getWSRPConsumers(
					portletDataContext.getCompanyId(), QueryUtil.ALL_POS,
					QueryUtil.ALL_POS);

			for (WSRPConsumer wsrpConsumer : wsrpConsumers) {
				exportWSRPConsumer(
					portletDataContext, wsrpConsumersElement, wsrpConsumer);
			}
		}

		return getExportDataRootElementString(rootElement);
	}

	@Override
	protected PortletPreferences doImportData(
			PortletDataContext portletDataContext, String portletId,
			PortletPreferences portletPreferences, String data)
		throws Exception {

		Element rootElement = portletDataContext.getImportDataRootElement();

		if (portletDataContext.getBooleanParameter(
				NAMESPACE, "wsrp-producers")) {

			Element wsrpProducersElement = rootElement.element(
				"wsrp-producers");

			importWSRPProducers(portletDataContext, wsrpProducersElement);
		}

		if (portletDataContext.getBooleanParameter(
				NAMESPACE, "wsrp-consumers")) {

			Element wsrpConsumersElement = rootElement.element(
				"wsrp-consumers");

			importWSRPConsumers(portletDataContext, wsrpConsumersElement);
		}

		return null;
	}

	protected void exportWSRPConsumer(
			PortletDataContext portletDataContext, Element wsrpConsumersElement,
			WSRPConsumer wsrpConsumer)
		throws Exception {

		String path = getWSRPConsumerPath(portletDataContext, wsrpConsumer);

		if (!portletDataContext.isPathNotProcessed(path)) {
			return;
		}

		Element wsrpConsumerElement = wsrpConsumersElement.addElement(
			"wsrp-consumer");

		portletDataContext.addClassedModel(
			wsrpConsumerElement, path, wsrpConsumer);

		if (portletDataContext.getBooleanParameter(
				NAMESPACE, "wsrp-consumer-portlets")) {

			List<WSRPConsumerPortlet> wsrpConsumerPortlets =
				WSRPConsumerPortletLocalServiceUtil.getWSRPConsumerPortlets(
					wsrpConsumer.getWsrpConsumerId(), QueryUtil.ALL_POS,
					QueryUtil.ALL_POS);

			Element wsrpConsumerPortletsElement =
				wsrpConsumerElement.addElement("wsrp-consumer-portlets");

			for (WSRPConsumerPortlet wsrpConsumerPortlet :
					wsrpConsumerPortlets) {

				exportWSRPConsumerPortlet(
					portletDataContext, wsrpConsumerPortletsElement,
					wsrpConsumerPortlet);
			}
		}
	}

	protected void exportWSRPConsumerPortlet(
			PortletDataContext portletDataContext,
			Element wsrpConsumerPortletsElement,
			WSRPConsumerPortlet wsrpConsumerPortlet)
		throws Exception {

		String path = getWSRPConsumerPortletsPath(
			portletDataContext, wsrpConsumerPortlet);

		if (!portletDataContext.isPathNotProcessed(path)) {
			return;
		}

		Element wsrpConsumerPortletElement =
			wsrpConsumerPortletsElement.addElement("wsrp-consumer-portlet");

		portletDataContext.addClassedModel(
			wsrpConsumerPortletElement, path, wsrpConsumerPortlet);
	}

	protected void exportWSRPProducer(
			PortletDataContext portletDataContext, Element wsrpProducersElement,
			WSRPProducer wsrpProducer)
		throws Exception {

		String path = getWSRPProducerPath(portletDataContext, wsrpProducer);

		if (!portletDataContext.isPathNotProcessed(path)) {
			return;
		}

		Element wsrpProducerElement = wsrpProducersElement.addElement(
			"wsrp-producer");

		portletDataContext.addClassedModel(
			wsrpProducerElement, path, wsrpProducer);
	}

	protected String getWSRPConsumerPath(
		PortletDataContext portletDataContext, WSRPConsumer wsrpConsumer) {

		StringBundler sb = new StringBundler(4);

		sb.append(portletDataContext.getPortletPath(_PORTLET_ID));
		sb.append("/wsrp-consumers/");
		sb.append(wsrpConsumer.getUuid());
		sb.append(".xml");

		return sb.toString();
	}

	protected String getWSRPConsumerPortletsPath(
		PortletDataContext portletDataContext,
		WSRPConsumerPortlet wsrpConsumerPortlet) {

		StringBundler sb = new StringBundler(4);

		sb.append(portletDataContext.getPortletPath(_PORTLET_ID));
		sb.append("/wsrp-consumer-portlets/");
		sb.append(wsrpConsumerPortlet.getWsrpConsumerPortletId());
		sb.append(".xml");

		return sb.toString();
	}

	protected String getWSRPProducerPath(
		PortletDataContext portletDataContext, WSRPProducer wsrpProducer) {

		StringBundler sb = new StringBundler(4);

		sb.append(portletDataContext.getPortletPath(_PORTLET_ID));
		sb.append("/wsrp-producers/");
		sb.append(wsrpProducer.getWsrpProducerId());
		sb.append(".xml");

		return sb.toString();
	}

	protected WSRPConsumer importWSRPConsumer(
			PortletDataContext portletDataContext, Element wsrpConsumerElement,
			WSRPConsumer wsrpConsumer)
		throws Exception {

		WSRPConsumer importedWSRPConsumer = null;

		try {
			importedWSRPConsumer = WSRPConsumerLocalServiceUtil.getWSRPConsumer(
				wsrpConsumer.getUuid());

			importedWSRPConsumer.setName(wsrpConsumer.getName());
			importedWSRPConsumer.setUrl(wsrpConsumer.getUrl());
			importedWSRPConsumer.setWsdl(wsrpConsumer.getWsdl());
			importedWSRPConsumer.setForwardCookies(
				wsrpConsumer.getForwardCookies());
			importedWSRPConsumer.setForwardHeaders(
				wsrpConsumer.getForwardHeaders());
			importedWSRPConsumer.setMarkupCharacterSets(
				wsrpConsumer.getMarkupCharacterSets());

			WSRPConsumerLocalServiceUtil.updateWSRPConsumer(
				importedWSRPConsumer);
		}
		catch (NoSuchConsumerException nsce) {
			ServiceContext serviceContext =
				portletDataContext.createServiceContext(
					wsrpConsumerElement, wsrpConsumer);

			serviceContext.setUuid(wsrpConsumer.getUuid());

			importedWSRPConsumer = WSRPConsumerLocalServiceUtil.addWSRPConsumer(
				portletDataContext.getCompanyId(), null, wsrpConsumer.getName(),
				wsrpConsumer.getUrl(), wsrpConsumer.getForwardCookies(),
				wsrpConsumer.getForwardHeaders(),
				wsrpConsumer.getMarkupCharacterSets(), serviceContext);
		}

		return importedWSRPConsumer;
	}

	protected void importWSRPConsumerPortlet(
			PortletDataContext portletDataContext, WSRPConsumer wsrpConsumer,
			Element wsrpConsumerPortletElement,
			WSRPConsumerPortlet wsrpConsumerPortlet)
		throws Exception {

		try {
			WSRPConsumerPortlet importedWSRPConsumerPortlet =
				WSRPConsumerPortletLocalServiceUtil.getWSRPConsumerPortlet(
					wsrpConsumerPortlet.getUuid());

			importedWSRPConsumerPortlet.setWsrpConsumerId(
				wsrpConsumer.getWsrpConsumerId());
			importedWSRPConsumerPortlet.setName(wsrpConsumerPortlet.getName());
			importedWSRPConsumerPortlet.setPortletHandle(
				wsrpConsumerPortlet.getPortletHandle());

			WSRPConsumerPortletLocalServiceUtil.updateWSRPConsumerPortlet(
				importedWSRPConsumerPortlet);
		}
		catch (NoSuchConsumerPortletException nscpe) {
			ServiceContext serviceContext =
				portletDataContext.createServiceContext(
					wsrpConsumerPortletElement, wsrpConsumerPortlet);

			serviceContext.setUuid(wsrpConsumerPortlet.getUuid());

			WSRPConsumerPortletLocalServiceUtil.addWSRPConsumerPortlet(
				wsrpConsumer.getUuid(), wsrpConsumerPortlet.getName(),
				wsrpConsumerPortlet.getPortletHandle(), serviceContext);
		}
	}

	protected void importWSRPConsumerPortlets(
			PortletDataContext portletDataContext, WSRPConsumer wsrpConsumer,
			Element wsrpConsumerPortletsElement)
		throws Exception {

		if (wsrpConsumerPortletsElement == null) {
			return;
		}

		for (Element wsrpConsumerPortletElement :
				wsrpConsumerPortletsElement.elements("wsrp-consumer-portlet")) {

			String path = wsrpConsumerPortletElement.attributeValue("path");

			if (!portletDataContext.isPathNotProcessed(path)) {
				continue;
			}

			WSRPConsumerPortlet wsrpConsumerPortlet =
				(WSRPConsumerPortlet)portletDataContext.getZipEntryAsObject(
					path);

			importWSRPConsumerPortlet(
				portletDataContext, wsrpConsumer, wsrpConsumerPortletElement,
				wsrpConsumerPortlet);
		}
	}

	protected void importWSRPConsumers(
			PortletDataContext portletDataContext, Element wsrpConsumersElement)
		throws Exception {

		if (wsrpConsumersElement == null) {
			return;
		}

		for (Element wsrpConsumerElement :
				wsrpConsumersElement.elements("wsrp-consumer")) {

			String path = wsrpConsumerElement.attributeValue("path");

			if (!portletDataContext.isPathNotProcessed(path)) {
				continue;
			}

			WSRPConsumer wsrpConsumer =
				(WSRPConsumer)portletDataContext.getZipEntryAsObject(path);

			WSRPConsumer importedWSRPConsumer = importWSRPConsumer(
				portletDataContext, wsrpConsumerElement, wsrpConsumer);

			if (portletDataContext.getBooleanParameter(
					NAMESPACE, "wsrp-consumer-portlets")) {

				Element wsrpConsumerPortletsElement =
					wsrpConsumerElement.element("wsrp-consumer-portlets");

				importWSRPConsumerPortlets(
					portletDataContext, importedWSRPConsumer,
					wsrpConsumerPortletsElement);
			}
		}
	}

	protected void importWSRPProducer(
			PortletDataContext portletDataContext, Element wsrpProducerElement,
			WSRPProducer wsrpProducer)
		throws Exception {

		try {
			WSRPProducer importedWSRPProducer =
				WSRPProducerLocalServiceUtil.getWSRPProducer(
					wsrpProducer.getUuid());

			importedWSRPProducer.setName(wsrpProducer.getName());
			importedWSRPProducer.setVersion(wsrpProducer.getVersion());
			importedWSRPProducer.setPortletIds(wsrpProducer.getPortletIds());

			WSRPProducerLocalServiceUtil.updateWSRPProducer(
				importedWSRPProducer);
		}
		catch (NoSuchProducerException nspe) {
			ServiceContext serviceContext =
				portletDataContext.createServiceContext(
					wsrpProducerElement, wsrpProducer);

			serviceContext.setUuid(wsrpProducer.getUuid());

			WSRPProducerLocalServiceUtil.addWSRPProducer(
				portletDataContext.getUserId(null), wsrpProducer.getName(),
				wsrpProducer.getVersion(), wsrpProducer.getPortletIds(),
				serviceContext);
		}
	}

	protected void importWSRPProducers(
			PortletDataContext portletDataContext, Element wsrpProducersElement)
		throws Exception {

		if (wsrpProducersElement == null) {
			return;
		}

		for (Element wsrpProducerElement :
				wsrpProducersElement.elements("wsrp-producer")) {

			String path = wsrpProducerElement.attributeValue("path");

			if (!portletDataContext.isPathNotProcessed(path)) {
				continue;
			}

			WSRPProducer wsrpProducer =
				(WSRPProducer)portletDataContext.getZipEntryAsObject(path);

			importWSRPProducer(
				portletDataContext, wsrpProducerElement, wsrpProducer);
		}
	}

	private static final String _PORTLET_ID = "1_WAR_wsrpportlet";

}