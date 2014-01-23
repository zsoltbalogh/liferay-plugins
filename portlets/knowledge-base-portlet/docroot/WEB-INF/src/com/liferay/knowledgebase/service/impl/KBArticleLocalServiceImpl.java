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

package com.liferay.knowledgebase.service.impl;

import com.liferay.knowledgebase.KBArticleContentException;
import com.liferay.knowledgebase.KBArticlePriorityException;
import com.liferay.knowledgebase.KBArticleTitleException;
import com.liferay.knowledgebase.admin.social.AdminActivityKeys;
import com.liferay.knowledgebase.admin.util.AdminSubscriptionSender;
import com.liferay.knowledgebase.admin.util.AdminUtil;
import com.liferay.knowledgebase.model.KBArticle;
import com.liferay.knowledgebase.model.KBArticleConstants;
import com.liferay.knowledgebase.service.base.KBArticleLocalServiceBaseImpl;
import com.liferay.knowledgebase.util.KnowledgeBaseUtil;
import com.liferay.knowledgebase.util.PortletKeys;
import com.liferay.knowledgebase.util.PortletPropsValues;
import com.liferay.knowledgebase.util.comparator.KBArticlePriorityComparator;
import com.liferay.knowledgebase.util.comparator.KBArticleVersionComparator;
import com.liferay.portal.kernel.bean.BeanPropertiesUtil;
import com.liferay.portal.kernel.dao.orm.Conjunction;
import com.liferay.portal.kernel.dao.orm.Criterion;
import com.liferay.portal.kernel.dao.orm.Disjunction;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.Junction;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchEngineUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StreamUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.UnmodifiableList;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;
import com.liferay.portal.model.CompanyConstants;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.ResourceConstants;
import com.liferay.portal.model.Subscription;
import com.liferay.portal.model.Ticket;
import com.liferay.portal.model.User;
import com.liferay.portal.portletfilerepository.PortletFileRepositoryUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.SubscriptionSender;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.documentlibrary.DuplicateDirectoryException;
import com.liferay.portlet.documentlibrary.FileNameException;
import com.liferay.portlet.documentlibrary.NoSuchDirectoryException;
import com.liferay.portlet.documentlibrary.store.DLStoreUtil;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletPreferences;

/**
 * @author Peter Shin
 * @author Brian Wing Shun Chan
 * @author Edward Han
 */
public class KBArticleLocalServiceImpl extends KBArticleLocalServiceBaseImpl {

	public void addAttachment(
			String dirName, String shortFileName, InputStream inputStream,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		if (!isValidDirName(dirName)) {
			throw new FileNameException();
		}

		if (!isValidFileName(shortFileName)) {
			throw new FileNameException();
		}

		if (!DLStoreUtil.hasDirectory(
				serviceContext.getCompanyId(), CompanyConstants.SYSTEM,
				dirName)) {

			DLStoreUtil.addDirectory(
				serviceContext.getCompanyId(), CompanyConstants.SYSTEM,
				dirName);
		}

		DLStoreUtil.addFile(
			serviceContext.getCompanyId(), CompanyConstants.SYSTEM,
			dirName + StringPool.SLASH + shortFileName, inputStream);
	}

	public KBArticle addKBArticle(
			long userId, long parentResourcePrimKey, String title,
			String content, String description, String[] sections,
			String dirName, ServiceContext serviceContext)
		throws PortalException, SystemException {

		// KB article

		User user = userPersistence.findByPrimaryKey(userId);
		long groupId = serviceContext.getScopeGroupId();
		double priority = getPriority(groupId, parentResourcePrimKey);
		Date now = new Date();

		validate(title, content);

		long kbArticleId = counterLocalService.increment();

		long resourcePrimKey = counterLocalService.increment();

		long rootResourcePrimKey = getRootResourcePrimKey(
			resourcePrimKey, parentResourcePrimKey);

		KBArticle kbArticle = kbArticlePersistence.create(kbArticleId);

		kbArticle.setUuid(serviceContext.getUuid());
		kbArticle.setResourcePrimKey(resourcePrimKey);
		kbArticle.setGroupId(groupId);
		kbArticle.setCompanyId(user.getCompanyId());
		kbArticle.setUserId(user.getUserId());
		kbArticle.setUserName(user.getFullName());
		kbArticle.setCreateDate(serviceContext.getCreateDate(now));
		kbArticle.setModifiedDate(serviceContext.getModifiedDate(now));
		kbArticle.setRootResourcePrimKey(rootResourcePrimKey);
		kbArticle.setParentResourcePrimKey(parentResourcePrimKey);
		kbArticle.setVersion(KBArticleConstants.DEFAULT_VERSION);
		kbArticle.setTitle(title);
		kbArticle.setContent(content);
		kbArticle.setDescription(description);
		kbArticle.setPriority(priority);
		kbArticle.setSections(
			StringUtil.merge(AdminUtil.escapeSections(sections)));
		kbArticle.setViewCount(0);
		kbArticle.setLatest(true);
		kbArticle.setMain(false);
		kbArticle.setStatus(WorkflowConstants.STATUS_DRAFT);

		kbArticlePersistence.update(kbArticle);

		// Resources

		if (serviceContext.isAddGroupPermissions() ||
			serviceContext.isAddGuestPermissions()) {

			addKBArticleResources(
				kbArticle, serviceContext.isAddGroupPermissions(),
				serviceContext.isAddGuestPermissions());
		}
		else {
			addKBArticleResources(
				kbArticle, serviceContext.getGroupPermissions(),
				serviceContext.getGuestPermissions());
		}

		// Asset

		updateKBArticleAsset(
			userId, kbArticle, serviceContext.getAssetCategoryIds(),
			serviceContext.getAssetTagNames());

		// Attachments

		addKBArticleAttachments(userId, kbArticle, dirName, serviceContext);

		// Workflow

		WorkflowHandlerRegistryUtil.startWorkflowInstance(
			user.getCompanyId(), groupId, userId, KBArticle.class.getName(),
			resourcePrimKey, kbArticle, serviceContext);

		return kbArticle;
	}

	public void addKBArticleResources(
			KBArticle kbArticle, boolean addGroupPermissions,
			boolean addGuestPermissions)
		throws PortalException, SystemException {

		resourceLocalService.addResources(
			kbArticle.getCompanyId(), kbArticle.getGroupId(),
			kbArticle.getUserId(), KBArticle.class.getName(),
			kbArticle.getResourcePrimKey(), false, addGroupPermissions,
			addGuestPermissions);
	}

	public void addKBArticleResources(
			KBArticle kbArticle, String[] groupPermissions,
			String[] guestPermissions)
		throws PortalException, SystemException {

		resourceLocalService.addModelResources(
			kbArticle.getCompanyId(), kbArticle.getGroupId(),
			kbArticle.getUserId(), KBArticle.class.getName(),
			kbArticle.getResourcePrimKey(), groupPermissions, guestPermissions);
	}

	public void addKBArticleResources(
			long kbArticleId, boolean addGroupPermissions,
			boolean addGuestPermissions)
		throws PortalException, SystemException {

		KBArticle kbArticle = kbArticlePersistence.findByPrimaryKey(
			kbArticleId);

		addKBArticleResources(
			kbArticle, addGroupPermissions, addGuestPermissions);
	}

	public void addKBArticleResources(
			long kbArticleId, String[] groupPermissions,
			String[] guestPermissions)
		throws PortalException, SystemException {

		KBArticle kbArticle = kbArticlePersistence.findByPrimaryKey(
			kbArticleId);

		addKBArticleResources(kbArticle, groupPermissions, guestPermissions);
	}

	public void checkAttachments() throws PortalException, SystemException {
		for (long companyId : PortalUtil.getCompanyIds()) {
			checkAttachments(companyId);
		}
	}

	public void deleteAttachment(long companyId, String fileName)
		throws PortalException, SystemException {

		DLStoreUtil.deleteFile(companyId, CompanyConstants.SYSTEM, fileName);
	}

	public void deleteGroupKBArticles(long groupId)
		throws PortalException, SystemException {

		// KB articles

		List<KBArticle> kbArticles = getSiblingKBArticles(
			groupId, KBArticleConstants.DEFAULT_PARENT_RESOURCE_PRIM_KEY,
			WorkflowConstants.STATUS_ANY, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
			new KBArticlePriorityComparator());

		for (KBArticle kbArticle : kbArticles) {
			deleteKBArticle(kbArticle);
		}

		// Subscriptions

		Group group = groupLocalService.getGroup(groupId);

		List<Subscription> subscriptions =
			subscriptionLocalService.getSubscriptions(
				group.getCompanyId(), KBArticle.class.getName(), groupId);

		for (Subscription subscription : subscriptions) {
			unsubscribeGroupKBArticles(subscription.getUserId(), groupId);
		}
	}

	@Override
	public KBArticle deleteKBArticle(KBArticle kbArticle)
		throws PortalException, SystemException {

		// Child kb articles

		List<KBArticle> siblingKBArticles = getSiblingKBArticles(
			kbArticle.getGroupId(), kbArticle.getResourcePrimKey(),
			WorkflowConstants.STATUS_ANY, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
			new KBArticlePriorityComparator());

		for (KBArticle siblingKBArticle : siblingKBArticles) {
			deleteKBArticle(siblingKBArticle);
		}

		// Resources

		resourceLocalService.deleteResource(
			kbArticle.getCompanyId(), KBArticle.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL, kbArticle.getResourcePrimKey());

		// KB articles

		kbArticlePersistence.removeByResourcePrimKey(
			kbArticle.getResourcePrimKey());

		// KB comments

		kbCommentLocalService.deleteKBComments(
			KBArticle.class.getName(), kbArticle.getResourcePrimKey());

		// Asset

		deleteAssets(kbArticle);

		// Ratings

		ratingsStatsLocalService.deleteStats(
			KBArticle.class.getName(), kbArticle.getResourcePrimKey());

		// Social

		socialActivityLocalService.deleteActivities(
			KBArticle.class.getName(), kbArticle.getResourcePrimKey());

		// Indexer

		Indexer indexer = IndexerRegistryUtil.getIndexer(KBArticle.class);

		indexer.delete(kbArticle);

		// Attachments

		deleteKBArticleAttachments(kbArticle);

		// Subscriptions

		deleteSubscriptions(kbArticle);

		// Workflow

		workflowInstanceLinkLocalService.deleteWorkflowInstanceLinks(
			kbArticle.getCompanyId(), kbArticle.getGroupId(),
			KBArticle.class.getName(), kbArticle.getResourcePrimKey());

		return kbArticle;
	}

	@Override
	public KBArticle deleteKBArticle(long resourcePrimKey)
		throws PortalException, SystemException {

		KBArticle kbArticle = getLatestKBArticle(
			resourcePrimKey, WorkflowConstants.STATUS_ANY);

		return deleteKBArticle(kbArticle);
	}

	public void deleteKBArticles(long[] resourcePrimKeys)
		throws PortalException, SystemException {

		List<KBArticle> kbArticles = getKBArticles(
			resourcePrimKeys, WorkflowConstants.STATUS_ANY, null);

		for (KBArticle kbArticle : kbArticles) {
			deleteKBArticle(kbArticle);
		}
	}

	public List<KBArticle> getCompanyKBArticles(
			long companyId, int status, int start, int end,
			OrderByComparator orderByComparator)
		throws SystemException {

		if (status == WorkflowConstants.STATUS_ANY) {
			return kbArticlePersistence.findByC_L(
				companyId, true, start, end, orderByComparator);
		}
		else if (status == WorkflowConstants.STATUS_APPROVED) {
			return kbArticlePersistence.findByC_M(
				companyId, true, start, end, orderByComparator);
		}

		return kbArticlePersistence.findByC_S(
			companyId, status, start, end, orderByComparator);
	}

	public int getCompanyKBArticlesCount(long companyId, int status)
		throws SystemException {

		if (status == WorkflowConstants.STATUS_ANY) {
			return kbArticlePersistence.countByC_L(companyId, true);
		}
		else if (status == WorkflowConstants.STATUS_APPROVED) {
			return kbArticlePersistence.countByC_M(companyId, true);
		}

		return kbArticlePersistence.countByC_S(companyId, status);
	}

	public List<KBArticle> getGroupKBArticles(
			long groupId, int status, int start, int end,
			OrderByComparator orderByComparator)
		throws SystemException {

		if (status == WorkflowConstants.STATUS_ANY) {
			return kbArticlePersistence.findByG_L(
				groupId, true, start, end, orderByComparator);
		}
		else if (status == WorkflowConstants.STATUS_APPROVED) {
			return kbArticlePersistence.findByG_M(
				groupId, true, start, end, orderByComparator);
		}

		return kbArticlePersistence.findByG_S(
			groupId, status, start, end, orderByComparator);
	}

	public int getGroupKBArticlesCount(long groupId, int status)
		throws SystemException {

		if (status == WorkflowConstants.STATUS_ANY) {
			return kbArticlePersistence.countByG_L(groupId, true);
		}
		else if (status == WorkflowConstants.STATUS_APPROVED) {
			return kbArticlePersistence.countByG_M(groupId, true);
		}

		return kbArticlePersistence.countByG_S(groupId, status);
	}

	public KBArticle getKBArticle(long resourcePrimKey, int version)
		throws PortalException, SystemException {

		return kbArticlePersistence.findByR_V(resourcePrimKey, version);
	}

	public List<KBArticle> getKBArticleAndAllDescendants(
			long resourcePrimKey, int status,
			OrderByComparator orderByComparator)
		throws SystemException {

		List<KBArticle> kbArticles = getKBArticles(
			new long[] {resourcePrimKey}, status, null);

		kbArticles = ListUtil.copy(kbArticles);

		Long[][] params = new Long[][] {new Long[] {resourcePrimKey}};

		while ((params = KnowledgeBaseUtil.getParams(params[0])) != null) {
			List<KBArticle> curKBArticles = null;

			if (status == WorkflowConstants.STATUS_ANY) {
				curKBArticles = kbArticlePersistence.findByP_L(
					ArrayUtil.toArray(params[1]), true);
			}
			else if (status == WorkflowConstants.STATUS_APPROVED) {
				curKBArticles = kbArticlePersistence.findByP_M(
					ArrayUtil.toArray(params[1]), true);
			}
			else {
				curKBArticles = kbArticlePersistence.findByP_S(
					ArrayUtil.toArray(params[1]), status);
			}

			kbArticles.addAll(curKBArticles);

			long[] resourcePrimKeys = StringUtil.split(
				ListUtil.toString(curKBArticles, "resourcePrimKey"), 0L);

			params[0] = ArrayUtil.append(
				params[0], ArrayUtil.toArray(resourcePrimKeys));
		}

		if (orderByComparator != null) {
			kbArticles = ListUtil.sort(kbArticles, orderByComparator);
		}

		return new UnmodifiableList<KBArticle>(kbArticles);
	}

	public List<KBArticle> getKBArticles(
			long[] resourcePrimKeys, int status,
			OrderByComparator orderByComparator)
		throws SystemException {

		List<KBArticle> kbArticles = new ArrayList<KBArticle>();

		Long[][] params = new Long[][] {ArrayUtil.toArray(resourcePrimKeys)};

		while ((params = KnowledgeBaseUtil.getParams(params[0])) != null) {
			List<KBArticle> curKBArticles = null;

			if (status == WorkflowConstants.STATUS_ANY) {
				curKBArticles = kbArticlePersistence.findByR_L(
					ArrayUtil.toArray(params[1]), true);
			}
			else if (status == WorkflowConstants.STATUS_APPROVED) {
				curKBArticles = kbArticlePersistence.findByR_M(
					ArrayUtil.toArray(params[1]), true);
			}
			else {
				curKBArticles = kbArticlePersistence.findByR_S(
					ArrayUtil.toArray(params[1]), status);
			}

			kbArticles.addAll(curKBArticles);
		}

		if (orderByComparator != null) {
			kbArticles = ListUtil.sort(kbArticles, orderByComparator);
		}
		else {
			kbArticles = KnowledgeBaseUtil.sort(resourcePrimKeys, kbArticles);
		}

		return new UnmodifiableList<KBArticle>(kbArticles);
	}

	public List<KBArticle> getKBArticleVersions(
			long resourcePrimKey, int status, int start, int end,
			OrderByComparator orderByComparator)
		throws SystemException {

		if (status == WorkflowConstants.STATUS_ANY) {
			return kbArticlePersistence.findByResourcePrimKey(
				resourcePrimKey, start, end, orderByComparator);
		}

		return kbArticlePersistence.findByR_S(
			resourcePrimKey, status, start, end, orderByComparator);
	}

	public int getKBArticleVersionsCount(long resourcePrimKey, int status)
		throws SystemException {

		if (status == WorkflowConstants.STATUS_ANY) {
			return kbArticlePersistence.countByResourcePrimKey(resourcePrimKey);
		}

		return kbArticlePersistence.countByR_S(resourcePrimKey, status);
	}

	public KBArticle getLatestKBArticle(long resourcePrimKey, int status)
		throws PortalException, SystemException {

		if (status == WorkflowConstants.STATUS_ANY) {
			return kbArticlePersistence.findByResourcePrimKey_First(
				resourcePrimKey, new KBArticleVersionComparator());
		}

		return kbArticlePersistence.findByR_S_First(
			resourcePrimKey, status, new KBArticleVersionComparator());
	}

	public List<KBArticle> getSectionsKBArticles(
			long groupId, String[] sections, int status, int start, int end,
			OrderByComparator orderByComparator)
		throws SystemException {

		String[] array = AdminUtil.escapeSections(sections);

		for (int i = 0; i < array.length; i++) {
			array[i] = StringUtil.quote(array[i], StringPool.PERCENT);
		}

		if (status == WorkflowConstants.STATUS_ANY) {
			return kbArticlePersistence.findByG_P_S_L(
				groupId, KBArticleConstants.DEFAULT_PARENT_RESOURCE_PRIM_KEY,
				array, true, start, end, orderByComparator);
		}
		else if (status == WorkflowConstants.STATUS_APPROVED) {
			return kbArticlePersistence.findByG_P_S_M(
				groupId, KBArticleConstants.DEFAULT_PARENT_RESOURCE_PRIM_KEY,
				array, true, start, end, orderByComparator);
		}

		return kbArticlePersistence.findByG_P_S_S(
			groupId, KBArticleConstants.DEFAULT_PARENT_RESOURCE_PRIM_KEY, array,
			status, start, end, orderByComparator);
	}

	public int getSectionsKBArticlesCount(
			long groupId, String[] sections, int status)
		throws SystemException {

		String[] array = AdminUtil.escapeSections(sections);

		for (int i = 0; i < array.length; i++) {
			array[i] = StringUtil.quote(array[i], StringPool.PERCENT);
		}

		if (status == WorkflowConstants.STATUS_ANY) {
			return kbArticlePersistence.countByG_P_S_L(
				groupId, KBArticleConstants.DEFAULT_PARENT_RESOURCE_PRIM_KEY,
				array, true);
		}
		else if (status == WorkflowConstants.STATUS_APPROVED) {
			return kbArticlePersistence.countByG_P_S_M(
				groupId, KBArticleConstants.DEFAULT_PARENT_RESOURCE_PRIM_KEY,
				array, true);
		}

		return kbArticlePersistence.countByG_P_S_S(
			groupId, KBArticleConstants.DEFAULT_PARENT_RESOURCE_PRIM_KEY, array,
			status);
	}

	public List<KBArticle> getSiblingKBArticles(
			long groupId, long parentResourcePrimKey, int status, int start,
			int end, OrderByComparator orderByComparator)
		throws SystemException {

		if (status == WorkflowConstants.STATUS_ANY) {
			return kbArticlePersistence.findByG_P_L(
				groupId, parentResourcePrimKey, true, start, end,
				orderByComparator);
		}
		else if (status == WorkflowConstants.STATUS_APPROVED) {
			return kbArticlePersistence.findByG_P_M(
				groupId, parentResourcePrimKey, true, start, end,
				orderByComparator);
		}

		return kbArticlePersistence.findByG_P_S(
			groupId, parentResourcePrimKey, status, start, end,
			orderByComparator);
	}

	public int getSiblingKBArticlesCount(
			long groupId, long parentResourcePrimKey, int status)
		throws SystemException {

		if (status == WorkflowConstants.STATUS_ANY) {
			return kbArticlePersistence.countByG_P_L(
				groupId, parentResourcePrimKey, true);
		}
		else if (status == WorkflowConstants.STATUS_APPROVED) {
			return kbArticlePersistence.countByG_P_M(
				groupId, parentResourcePrimKey, true);
		}

		return kbArticlePersistence.countByG_P_S(
			groupId, parentResourcePrimKey, status);
	}

	public void moveKBArticle(
			long userId, long resourcePrimKey, long parentResourcePrimKey,
			double priority)
		throws PortalException, SystemException {

		// KB article

		validate(priority);

		updatePermissionFields(resourcePrimKey, parentResourcePrimKey);

		List<KBArticle> kbArticles = getKBArticleVersions(
			resourcePrimKey, WorkflowConstants.STATUS_ANY, QueryUtil.ALL_POS,
			QueryUtil.ALL_POS, new KBArticleVersionComparator());

		for (KBArticle kbArticle : kbArticles) {
			kbArticle.setParentResourcePrimKey(parentResourcePrimKey);
			kbArticle.setPriority(priority);

			kbArticlePersistence.update(kbArticle);
		}

		// Social

		KBArticle kbArticle = getLatestKBArticle(
			resourcePrimKey, WorkflowConstants.STATUS_ANY);

		JSONObject extraDataJSONObject = JSONFactoryUtil.createJSONObject();

		extraDataJSONObject.put("title", kbArticle.getTitle());

		if (kbArticle.isApproved() || !kbArticle.isFirstVersion()) {
			socialActivityLocalService.addActivity(
				userId, kbArticle.getGroupId(), KBArticle.class.getName(),
				resourcePrimKey, AdminActivityKeys.MOVE_KB_ARTICLE,
				extraDataJSONObject.toString(), 0);
		}
	}

	public List<KBArticle> search(
			long groupId, String title, String content, int status,
			Date startDate, Date endDate, boolean andOperator, int start,
			int end, OrderByComparator orderByComparator)
		throws SystemException {

		DynamicQuery dynamicQuery = buildDynamicQuery(
			groupId, title, content, status, startDate, endDate, andOperator);

		return dynamicQuery(dynamicQuery, start, end, orderByComparator);
	}

	public void subscribeGroupKBArticles(long userId, long groupId)
		throws PortalException, SystemException {

		subscriptionLocalService.addSubscription(
			userId, groupId, KBArticle.class.getName(), groupId);
	}

	public void subscribeKBArticle(
			long userId, long groupId, long resourcePrimKey)
		throws PortalException, SystemException {

		subscriptionLocalService.addSubscription(
			userId, groupId, KBArticle.class.getName(), resourcePrimKey);
	}

	public void unsubscribeGroupKBArticles(long userId, long groupId)
		throws PortalException, SystemException {

		subscriptionLocalService.deleteSubscription(
			userId, KBArticle.class.getName(), groupId);
	}

	public void unsubscribeKBArticle(long userId, long resourcePrimKey)
		throws PortalException, SystemException {

		subscriptionLocalService.deleteSubscription(
			userId, KBArticle.class.getName(), resourcePrimKey);
	}

	public String updateAttachments(
			long resourcePrimKey, String dirName, ServiceContext serviceContext)
		throws PortalException, SystemException {

		if (isValidDirName(dirName)) {
			return dirName;
		}

		Ticket ticket = ticketLocalService.addTicket(
			serviceContext.getCompanyId(), User.class.getName(),
			serviceContext.getUserId(), 0, null, getTicketExpirationDate(),
			serviceContext);

		StringBundler sb = new StringBundler(5);

		sb.append(_TEMP_DIR_NAME_PREFIX);
		sb.append(StringPool.SLASH);
		sb.append(counterLocalService.increment());
		sb.append(StringPool.SLASH);
		sb.append(ticket.getKey());

		dirName = sb.toString();

		ticket.setExtraInfo(dirName);

		ticketLocalService.updateTicket(ticket);

		DLStoreUtil.addDirectory(
			serviceContext.getCompanyId(), CompanyConstants.SYSTEM, dirName);

		if (resourcePrimKey <= 0) {
			return dirName;
		}

		KBArticle kbArticle = getLatestKBArticle(
			resourcePrimKey, WorkflowConstants.STATUS_ANY);

		List<FileEntry> attachmentsFileEntries =
			kbArticle.getAttachmentsFileEntries();

		for (FileEntry fileEntry : attachmentsFileEntries) {
			addAttachment(
				dirName, fileEntry.getTitle(), fileEntry.getContentStream(),
				serviceContext);
		}

		return dirName;
	}

	public KBArticle updateKBArticle(
			long userId, long resourcePrimKey, String title, String content,
			String description, String[] sections, String dirName,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		// KB article

		User user = userPersistence.findByPrimaryKey(userId);

		validate(title, content);

		KBArticle oldKBArticle = getLatestKBArticle(
			resourcePrimKey, WorkflowConstants.STATUS_ANY);

		int oldVersion = oldKBArticle.getVersion();

		KBArticle kbArticle = null;

		if (oldKBArticle.isApproved()) {
			long kbArticleId = counterLocalService.increment();

			kbArticle = kbArticlePersistence.create(kbArticleId);

			kbArticle.setResourcePrimKey(oldKBArticle.getResourcePrimKey());
			kbArticle.setGroupId(oldKBArticle.getGroupId());
			kbArticle.setCompanyId(user.getCompanyId());
			kbArticle.setUserId(user.getUserId());
			kbArticle.setUserName(user.getFullName());
			kbArticle.setCreateDate(oldKBArticle.getCreateDate());
			kbArticle.setRootResourcePrimKey(
				oldKBArticle.getRootResourcePrimKey());
			kbArticle.setParentResourcePrimKey(
				oldKBArticle.getParentResourcePrimKey());
			kbArticle.setVersion(oldVersion + 1);
			kbArticle.setPriority(oldKBArticle.getPriority());
			kbArticle.setViewCount(oldKBArticle.getViewCount());
		}
		else {
			kbArticle = oldKBArticle;
		}

		if (oldKBArticle.isPending()) {
			kbArticle.setStatus(WorkflowConstants.STATUS_PENDING);
		}
		else {
			kbArticle.setStatus(WorkflowConstants.STATUS_DRAFT);
		}

		kbArticle.setModifiedDate(serviceContext.getModifiedDate(null));
		kbArticle.setTitle(title);
		kbArticle.setContent(content);
		kbArticle.setDescription(description);
		kbArticle.setSections(
			StringUtil.merge(AdminUtil.escapeSections(sections)));
		kbArticle.setLatest(true);
		kbArticle.setMain(false);

		kbArticlePersistence.update(kbArticle);

		if (oldKBArticle.isApproved()) {
			oldKBArticle.setLatest(false);

			kbArticlePersistence.update(oldKBArticle);
		}

		// Resources

		if ((serviceContext.getGroupPermissions() != null) ||
			(serviceContext.getGuestPermissions() != null)) {

			updateKBArticleResources(
				kbArticle, serviceContext.getGroupPermissions(),
				serviceContext.getGuestPermissions());
		}

		// Asset

		updateKBArticleAsset(
			userId, kbArticle, serviceContext.getAssetCategoryIds(),
			serviceContext.getAssetTagNames());

		// Attachments

		updateKBArticleAttachments(
			userId, kbArticle, oldVersion, dirName, serviceContext);

		// Workflow

		WorkflowHandlerRegistryUtil.startWorkflowInstance(
			user.getCompanyId(), kbArticle.getGroupId(), userId,
			KBArticle.class.getName(), resourcePrimKey, kbArticle,
			serviceContext);

		return kbArticle;
	}

	public void updateKBArticleAsset(
			long userId, KBArticle kbArticle, long[] assetCategoryIds,
			String[] assetTagNames)
		throws PortalException, SystemException {

		// TODO

		long classTypeId = 0;

		assetEntryLocalService.updateEntry(
			userId, kbArticle.getGroupId(), kbArticle.getCreateDate(),
			kbArticle.getModifiedDate(), KBArticle.class.getName(),
			kbArticle.getClassPK(), kbArticle.getUuid(), classTypeId,
			assetCategoryIds, assetTagNames, false, null, null, null,
			ContentTypes.TEXT_HTML, kbArticle.getTitle(),
			kbArticle.getDescription(), null, null, null, 0, 0, null, false);
	}

	public void updateKBArticleResources(
			KBArticle kbArticle, String[] groupPermissions,
			String[] guestPermissions)
		throws PortalException, SystemException {

		resourceLocalService.updateResources(
			kbArticle.getCompanyId(), kbArticle.getGroupId(),
			KBArticle.class.getName(), kbArticle.getResourcePrimKey(),
			groupPermissions, guestPermissions);
	}

	public void updateKBArticlesPriorities(
			Map<Long, Double> resourcePrimKeyToPriorityMap)
		throws PortalException, SystemException {

		for (double priority : resourcePrimKeyToPriorityMap.values()) {
			validate(priority);
		}

		long[] resourcePrimKeys = StringUtil.split(
			StringUtil.merge(resourcePrimKeyToPriorityMap.keySet()), 0L);

		List<KBArticle> kbArticles = getKBArticles(
			resourcePrimKeys, WorkflowConstants.STATUS_ANY, null);

		for (KBArticle kbArticle : kbArticles) {
			double priority = resourcePrimKeyToPriorityMap.get(
				kbArticle.getResourcePrimKey());

			updatePriority(kbArticle.getResourcePrimKey(), priority);
		}
	}

	public void updatePriority(long resourcePrimKey, double priority)
		throws PortalException, SystemException {

		List<KBArticle> kbArticleVersions = getKBArticleVersions(
			resourcePrimKey, WorkflowConstants.STATUS_ANY, QueryUtil.ALL_POS,
			QueryUtil.ALL_POS, null);

		for (KBArticle kbArticle : kbArticleVersions) {
			kbArticle.setPriority(priority);

			kbArticlePersistence.update(kbArticle);
		}
	}

	public KBArticle updateStatus(
			long userId, long resourcePrimKey, int status,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		// KB article

		User user = userPersistence.findByPrimaryKey(userId);
		boolean main = false;
		Date now = new Date();

		if (status == WorkflowConstants.STATUS_APPROVED) {
			main = true;
		}

		KBArticle kbArticle = getLatestKBArticle(
			resourcePrimKey, WorkflowConstants.STATUS_ANY);

		kbArticle.setModifiedDate(serviceContext.getModifiedDate(now));
		kbArticle.setMain(main);
		kbArticle.setStatus(status);
		kbArticle.setStatusByUserId(user.getUserId());
		kbArticle.setStatusByUserName(user.getFullName());
		kbArticle.setStatusDate(serviceContext.getModifiedDate(now));

		kbArticlePersistence.update(kbArticle);

		if (status != WorkflowConstants.STATUS_APPROVED) {
			return kbArticle;
		}

		if (!kbArticle.isFirstVersion()) {
			KBArticle oldKBArticle = kbArticlePersistence.findByR_V(
				resourcePrimKey, kbArticle.getVersion() - 1);

			oldKBArticle.setMain(false);

			kbArticlePersistence.update(oldKBArticle);
		}

		// Asset

		AssetEntry assetEntry = assetEntryLocalService.getEntry(
			KBArticle.class.getName(), kbArticle.getKbArticleId());

		updateKBArticleAsset(
			userId, kbArticle, assetEntry.getCategoryIds(),
			assetEntry.getTagNames());

		assetEntryLocalService.deleteEntry(
			KBArticle.class.getName(), kbArticle.getKbArticleId());

		assetEntryLocalService.updateVisible(
			KBArticle.class.getName(), kbArticle.getResourcePrimKey(), true);

		// Social

		JSONObject extraDataJSONObject = JSONFactoryUtil.createJSONObject();

		extraDataJSONObject.put("title", kbArticle.getTitle());

		if (!kbArticle.isFirstVersion()) {
			socialActivityLocalService.addActivity(
				userId, kbArticle.getGroupId(), KBArticle.class.getName(),
				resourcePrimKey, AdminActivityKeys.UPDATE_KB_ARTICLE,
				extraDataJSONObject.toString(), 0);
		}
		else {
			socialActivityLocalService.addActivity(
				userId, kbArticle.getGroupId(), KBArticle.class.getName(),
				resourcePrimKey, AdminActivityKeys.ADD_KB_ARTICLE,
				extraDataJSONObject.toString(), 0);
		}

		// Indexer

		Indexer indexer = IndexerRegistryUtil.getIndexer(KBArticle.class);

		indexer.reindex(kbArticle);

		// Attachments

		if (!kbArticle.isFirstVersion()) {
			deleteKBArticleAttachments(kbArticle, resourcePrimKey);
		}

		String dirName =
			KBArticleConstants.DIR_NAME_PREFIX + kbArticle.getKbArticleId();

		addKBArticleAttachments(userId, kbArticle, dirName, serviceContext);

		deleteKBArticleAttachments(kbArticle, kbArticle.getKbArticleId());

		// Subscriptions

		notifySubscribers(kbArticle, serviceContext);

		return kbArticle;
	}

	public void updateViewCount(
			long userId, long resourcePrimKey, int viewCount)
		throws PortalException, SystemException {

		KBArticle kbArticle = getLatestKBArticle(
			resourcePrimKey, WorkflowConstants.STATUS_ANY);

		kbArticle.setViewCount(viewCount);

		kbArticlePersistence.update(kbArticle);

		if (kbArticle.isApproved() || kbArticle.isFirstVersion()) {
			return;
		}

		kbArticle = getLatestKBArticle(
			resourcePrimKey, WorkflowConstants.STATUS_APPROVED);

		kbArticle.setViewCount(viewCount);

		kbArticlePersistence.update(kbArticle);
	}

	protected void addKBArticleAttachments(
			long userId, KBArticle kbArticle, String dirName,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		try {
			DLStoreUtil.addDirectory(
				serviceContext.getCompanyId(), CompanyConstants.SYSTEM,
				kbArticle.getAttachmentsDirName());
		}
		catch (DuplicateDirectoryException dde) {
			_log.error("Directory already exists for " + dde.getMessage());
		}

		if (Validator.isNull(dirName) ||
			!DLStoreUtil.hasDirectory(
				serviceContext.getCompanyId(), CompanyConstants.SYSTEM,
				dirName)) {

			return;
		}

		String[] fileNames = DLStoreUtil.getFileNames(
			serviceContext.getCompanyId(), CompanyConstants.SYSTEM, dirName);

		if (fileNames.length > 0) {
			PortletFileRepositoryUtil.deletePortletFileEntries(
				serviceContext.getScopeGroupId(),
				kbArticle.getAttachmentsFolderId());
		}

		for (String fileName : fileNames) {
			InputStream inputStream = null;

			try {
				inputStream = DLStoreUtil.getFileAsStream(
					serviceContext.getCompanyId(), CompanyConstants.SYSTEM,
					fileName);

				String shortFileName = FileUtil.getShortFileName(fileName);

				String mimeType = MimeTypesUtil.getContentType(
					inputStream, fileName);

				PortletFileRepositoryUtil.addPortletFileEntry(
					serviceContext.getScopeGroupId(), userId,
					KBArticle.class.getName(), kbArticle.getClassPK(),
					PortletKeys.KNOWLEDGE_BASE_ARTICLE,
					kbArticle.getAttachmentsFolderId(), inputStream,
					shortFileName, mimeType, true);
			}
			finally {
				StreamUtil.cleanUp(inputStream);
			}
		}

		Ticket ticket = ticketLocalService.fetchTicket(
			StringUtil.extractLast(dirName, StringPool.SLASH));

		if (ticket != null) {
			ticketLocalService.deleteTicket(ticket);
		}
	}

	protected DynamicQuery buildDynamicQuery(
		long groupId, String title, String content, int status, Date startDate,
		Date endDate, boolean andOperator) {

		Junction junction = null;

		if (andOperator) {
			junction = RestrictionsFactoryUtil.conjunction();
		}
		else {
			junction = RestrictionsFactoryUtil.disjunction();
		}

		Map<String, String> terms = new HashMap<String, String>();

		if (Validator.isNotNull(title)) {
			terms.put("title", title);
		}

		if (Validator.isNotNull(content)) {
			terms.put("content", content);
		}

		for (Map.Entry<String, String> entry : terms.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			Disjunction disjunction = RestrictionsFactoryUtil.disjunction();

			for (String keyword : KnowledgeBaseUtil.parseKeywords(value)) {
				Criterion criterion = RestrictionsFactoryUtil.ilike(
					key, StringUtil.quote(keyword, StringPool.PERCENT));

				disjunction.add(criterion);
			}

			junction.add(disjunction);
		}

		if (status != WorkflowConstants.STATUS_ANY) {
			Property property = PropertyFactoryUtil.forName("status");

			junction.add(property.eq(status));
		}

		if ((endDate != null) && (startDate != null)) {
			Disjunction disjunction = RestrictionsFactoryUtil.disjunction();

			String[] propertyNames = {"createDate", "modifiedDate"};

			for (String propertyName : propertyNames) {
				Property property = PropertyFactoryUtil.forName(propertyName);

				Conjunction conjunction = RestrictionsFactoryUtil.conjunction();

				conjunction.add(property.gt(startDate));
				conjunction.add(property.lt(endDate));

				disjunction.add(conjunction);
			}

			junction.add(disjunction);
		}

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			KBArticle.class, getClassLoader());

		if (status == WorkflowConstants.STATUS_ANY) {
			Property property = PropertyFactoryUtil.forName("latest");

			dynamicQuery.add(property.eq(Boolean.TRUE));
		}
		else if (status == WorkflowConstants.STATUS_APPROVED) {
			Property property = PropertyFactoryUtil.forName("main");

			dynamicQuery.add(property.eq(Boolean.TRUE));
		}

		if (groupId > 0) {
			Property property = PropertyFactoryUtil.forName("groupId");

			dynamicQuery.add(property.eq(groupId));
		}

		return dynamicQuery.add(junction);
	}

	protected void checkAttachments(long companyId)
		throws PortalException, SystemException {

		String dirName = _TEMP_DIR_NAME_PREFIX.concat(StringPool.SLASH).concat(
			String.valueOf(counterLocalService.increment()));

		DLStoreUtil.addDirectory(companyId, CompanyConstants.SYSTEM, dirName);

		String[] fileNames = DLStoreUtil.getFileNames(
			companyId, CompanyConstants.SYSTEM, _TEMP_DIR_NAME_PREFIX);

		Arrays.sort(fileNames);

		for (int i = 0; i < fileNames.length - 50; i++) {
			DLStoreUtil.deleteDirectory(
				companyId, CompanyConstants.SYSTEM, fileNames[i]);
		}

		DLStoreUtil.deleteDirectory(
			companyId, CompanyConstants.SYSTEM, dirName);
	}

	protected void deleteAssets(KBArticle kbArticle)
		throws PortalException, SystemException {

		assetEntryLocalService.deleteEntry(
			KBArticle.class.getName(), kbArticle.getClassPK());

		if (!kbArticle.isApproved() && !kbArticle.isFirstVersion()) {
			assetEntryLocalService.deleteEntry(
				KBArticle.class.getName(), kbArticle.getResourcePrimKey());
		}
	}

	protected void deleteKBArticleAttachments(KBArticle kbArticle)
		throws PortalException, SystemException {

		deleteKBArticleAttachments(kbArticle, kbArticle.getClassPK());

		if (!kbArticle.isApproved() && !kbArticle.isFirstVersion()) {
			deleteKBArticleAttachments(
				kbArticle, kbArticle.getResourcePrimKey());
		}
	}

	protected void deleteKBArticleAttachments(
			KBArticle kbArticle, long folderId)
		throws PortalException, SystemException {

		try {
			DLStoreUtil.deleteDirectory(
				kbArticle.getCompanyId(), CompanyConstants.SYSTEM,
				KBArticleConstants.DIR_NAME_PREFIX + folderId);
		}
		catch (NoSuchDirectoryException nsde) {
			_log.error("No directory found for " + nsde.getMessage());
		}
	}

	protected void deleteSubscriptions(KBArticle kbArticle)
		throws PortalException, SystemException {

		List<Subscription> subscriptions =
			subscriptionLocalService.getSubscriptions(
				kbArticle.getCompanyId(), KBArticle.class.getName(),
				kbArticle.getResourcePrimKey());

		for (Subscription subscription : subscriptions) {
			unsubscribeKBArticle(
				subscription.getUserId(), subscription.getClassPK());
		}
	}

	protected Map<String, String> getEmailKBArticleDiffs(KBArticle kbArticle) {
		Map<String, String> emailKBArticleDiffs = new HashMap<String, String>();

		for (String param : new String[] {"content", "title"}) {
			String value = BeanPropertiesUtil.getString(kbArticle, param);

			try {
				value = AdminUtil.getKBArticleDiff(
					kbArticle.getResourcePrimKey(), kbArticle.getVersion() - 1,
					kbArticle.getVersion(), param);
			}
			catch (Exception e) {
				_log.error(e, e);
			}

			emailKBArticleDiffs.put(param, value);
		}

		return emailKBArticleDiffs;
	}

	protected double getPriority(long groupId, long parentResourcePrimKey)
		throws SystemException {

		if (!PortletPropsValues.ADMIN_KB_ARTICLE_INCREMENT_PRIORITY_ENABLED) {
			return KBArticleConstants.DEFAULT_VERSION;
		}

		List<KBArticle> kbArticles = getSiblingKBArticles(
			groupId, parentResourcePrimKey, WorkflowConstants.STATUS_ANY, 0, 1,
			new KBArticlePriorityComparator());

		if (kbArticles.isEmpty()) {
			return KBArticleConstants.DEFAULT_PRIORITY;
		}

		KBArticle kbArticle = kbArticles.get(0);

		return Math.floor(kbArticle.getPriority()) + 1;
	}

	protected long getRootResourcePrimKey(
			long resourcePrimKey, long parentResourcePrimKey)
		throws PortalException, SystemException {

		if (parentResourcePrimKey ==
				KBArticleConstants.DEFAULT_PARENT_RESOURCE_PRIM_KEY) {

			return resourcePrimKey;
		}

		KBArticle kbArticle = getLatestKBArticle(
			parentResourcePrimKey, WorkflowConstants.STATUS_ANY);

		return kbArticle.getRootResourcePrimKey();
	}

	protected Date getTicketExpirationDate() {
		return new Date(System.currentTimeMillis() + _TICKET_EXPIRATION);
	}

	protected boolean isValidDirName(String dirName) throws SystemException {
		String key = StringUtil.extractLast(dirName, StringPool.SLASH);

		if (key == null) {
			return false;
		}

		Ticket ticket = ticketLocalService.fetchTicket(key);

		if (ticket == null) {
			return false;
		}

		if (!Validator.equals(ticket.getExtraInfo(), dirName)) {
			return false;
		}

		ticket.setExpirationDate(getTicketExpirationDate());

		ticket = ticketLocalService.updateTicket(ticket);

		return true;
	}

	protected boolean isValidFileName(String name) {
		if ((name == null) || name.contains(StringPool.BACK_SLASH) ||
			name.contains(StringPool.SLASH)) {

			return false;
		}

		return true;
	}

	protected void notifySubscribers(
			KBArticle kbArticle, ServiceContext serviceContext)
		throws PortalException, SystemException {

		if (Validator.isNull(serviceContext.getLayoutFullURL())) {
			return;
		}

		PortletPreferences preferences =
			portletPreferencesLocalService.getPreferences(
				kbArticle.getCompanyId(), kbArticle.getGroupId(),
				PortletKeys.PREFS_OWNER_TYPE_GROUP,
				PortletKeys.PREFS_PLID_SHARED, PortletKeys.KNOWLEDGE_BASE_ADMIN,
				null);

		if (serviceContext.isCommandAdd() &&
			!AdminUtil.getEmailKBArticleAddedEnabled(preferences)) {

			return;
		}

		if (serviceContext.isCommandUpdate() &&
			!AdminUtil.getEmailKBArticleUpdatedEnabled(preferences)) {

			return;
		}

		String fromName = AdminUtil.getEmailFromName(
			preferences, kbArticle.getCompanyId());
		String fromAddress = AdminUtil.getEmailFromAddress(
			preferences, kbArticle.getCompanyId());

		String kbArticleContent = StringUtil.replace(
			kbArticle.getContent(),
			new String[] {
				"href=\"/", "src=\"/"
			},
			new String[] {
				"href=\"" + serviceContext.getPortalURL() + "/",
				"src=\"" + serviceContext.getPortalURL() + "/"
			});

		Map<String, String> kbArticleDiffs = getEmailKBArticleDiffs(kbArticle);

		for (String key : kbArticleDiffs.keySet()) {
			String value = StringUtil.replace(
				kbArticleDiffs.get(key),
				new String[] {
					"href=\"/", "src=\"/"
				},
				new String[] {
					"href=\"" + serviceContext.getPortalURL() + "/",
					"src=\"" + serviceContext.getPortalURL() + "/"
				});

			kbArticleDiffs.put(key, value);
		}

		String subject = null;
		String body = null;

		if (serviceContext.isCommandAdd()) {
			subject = AdminUtil.getEmailKBArticleAddedSubject(preferences);
			body = AdminUtil.getEmailKBArticleAddedBody(preferences);
		}
		else {
			subject = AdminUtil.getEmailKBArticleUpdatedSubject(preferences);
			body = AdminUtil.getEmailKBArticleUpdatedBody(preferences);
		}

		SubscriptionSender subscriptionSender = new AdminSubscriptionSender(
			kbArticle, serviceContext);

		subscriptionSender.setBody(body);
		subscriptionSender.setCompanyId(kbArticle.getCompanyId());
		subscriptionSender.setContextAttribute(
			"[$ARTICLE_CONTENT$]", kbArticleContent, false);
		subscriptionSender.setContextAttribute(
			"[$ARTICLE_CONTENT_DIFF$]", kbArticleDiffs.get("content"), false);
		subscriptionSender.setContextAttribute(
			"[$ARTICLE_TITLE$]", kbArticle.getTitle(), false);
		subscriptionSender.setContextAttribute(
			"[$ARTICLE_TITLE_DIFF$]", kbArticleDiffs.get("title"), false);
		subscriptionSender.setContextUserPrefix("ARTICLE");
		subscriptionSender.setFrom(fromAddress, fromName);
		subscriptionSender.setHtmlFormat(true);
		subscriptionSender.setMailId("kb_article", kbArticle.getKbArticleId());
		subscriptionSender.setPortletId(serviceContext.getPortletId());
		subscriptionSender.setReplyToAddress(fromAddress);
		subscriptionSender.setScopeGroupId(kbArticle.getGroupId());
		subscriptionSender.setSubject(subject);
		subscriptionSender.setUserId(kbArticle.getUserId());

		subscriptionSender.addPersistedSubscribers(
			KBArticle.class.getName(), kbArticle.getGroupId());
		subscriptionSender.addPersistedSubscribers(
			KBArticle.class.getName(), kbArticle.getResourcePrimKey());

		while (!kbArticle.isRoot()) {
			kbArticle = getLatestKBArticle(
				kbArticle.getParentResourcePrimKey(),
				WorkflowConstants.STATUS_APPROVED);

			subscriptionSender.addPersistedSubscribers(
				KBArticle.class.getName(), kbArticle.getResourcePrimKey());
		}

		subscriptionSender.flushNotificationsAsync();
	}

	protected void updateKBArticleAttachments(
			long userId, KBArticle kbArticle, int oldVersion, String dirName,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		if (kbArticle.getVersion() > oldVersion) {
			String oldDirName =
				KBArticleConstants.DIR_NAME_PREFIX +
					kbArticle.getResourcePrimKey();

			if (Validator.isNull(dirName)) {
				addKBArticleAttachments(
					userId, kbArticle, oldDirName, serviceContext);
			}
			else {
				addKBArticleAttachments(
					userId, kbArticle, dirName, serviceContext);
			}
		}
		else if (Validator.isNotNull(dirName)) {
			deleteKBArticleAttachments(kbArticle, kbArticle.getClassPK());

			addKBArticleAttachments(userId, kbArticle, dirName, serviceContext);
		}
	}

	protected void updatePermissionFields(
			long resourcePrimKey, long parentResourcePrimKey)
		throws PortalException, SystemException {

		// See KBArticlePermission#contains

		KBArticle kbArticle = getLatestKBArticle(
			resourcePrimKey, WorkflowConstants.STATUS_ANY);

		if (kbArticle.getParentResourcePrimKey() == parentResourcePrimKey) {
			return;
		}

		long rootResourcePrimKey = getRootResourcePrimKey(
			resourcePrimKey, parentResourcePrimKey);

		if (kbArticle.getRootResourcePrimKey() == rootResourcePrimKey) {
			return;
		}

		// Sync database

		List<KBArticle> kbArticles1 = getKBArticleAndAllDescendants(
			resourcePrimKey, WorkflowConstants.STATUS_ANY, null);

		for (KBArticle kbArticle1 : kbArticles1) {
			List<KBArticle> kbArticles2 = getKBArticleVersions(
				kbArticle1.getResourcePrimKey(), WorkflowConstants.STATUS_ANY,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);

			for (KBArticle kbArticle2 : kbArticles2) {
				kbArticle2.setRootResourcePrimKey(rootResourcePrimKey);

				kbArticlePersistence.update(kbArticle2);
			}
		}

		// Sync indexed permission fields

		SearchEngineUtil.updatePermissionFields(
			KBArticle.class.getName(), String.valueOf(resourcePrimKey));
	}

	protected void validate(double priority) throws PortalException {
		if (priority <= 0) {
			throw new KBArticlePriorityException();
		}
	}

	protected void validate(String title, String content)
		throws PortalException {

		if (Validator.isNull(title)) {
			throw new KBArticleTitleException();
		}

		if (Validator.isNull(content)) {
			throw new KBArticleContentException();
		}
	}

	private static final String _TEMP_DIR_NAME_PREFIX =
		"knowledgebase/temp/attachments";

	private static final long _TICKET_EXPIRATION = Time.HOUR;

	private static Log _log = LogFactoryUtil.getLog(
		KBArticleLocalServiceImpl.class);

}