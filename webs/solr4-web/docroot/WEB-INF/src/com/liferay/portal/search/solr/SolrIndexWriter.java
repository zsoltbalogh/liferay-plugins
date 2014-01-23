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

package com.liferay.portal.search.solr;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BaseIndexWriter;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;

/**
 * @author Bruno Farache
 */
public class SolrIndexWriter extends BaseIndexWriter {

	@Override
	public void addDocument(SearchContext searchContext, Document document)
		throws SearchException {

		try {
			_solrServer.add(getSolrInputDocument(document));

			if (_commit) {
				_solrServer.commit();
			}
		}
		catch (Exception e) {
			_log.error(e, e);

			throw new SearchException(e.getMessage());
		}
	}

	@Override
	public void addDocuments(
			SearchContext searchContext, Collection<Document> documents)
		throws SearchException {

		try {
			Collection<SolrInputDocument> solrInputDocuments =
				getSolrInputDocuments(documents);

			if (solrInputDocuments.isEmpty()) {
				return;
			}

			_solrServer.add(solrInputDocuments);

			if (_commit) {
				_solrServer.commit();
			}
		}
		catch (Exception e) {
			_log.error(e, e);

			throw new SearchException(e.getMessage());
		}
	}

	@Override
	public void deleteDocument(SearchContext searchContext, String uid)
		throws SearchException {

		try {
			_solrServer.deleteById(uid);

			if (_commit) {
				_solrServer.commit();
			}
		}
		catch (Exception e) {
			_log.error(e, e);

			throw new SearchException(e.getMessage());
		}
	}

	@Override
	public void deleteDocuments(
			SearchContext searchContext, Collection<String> uids)
		throws SearchException {

		for (String uid : uids) {
			deleteDocument(searchContext, uid);
		}
	}

	@Override
	public void deletePortletDocuments(
			SearchContext searchContext, String portletId)
		throws SearchException {

		try {
			long companyId = searchContext.getCompanyId();

			StringBundler sb = null;

			if (companyId > 0) {
				sb = new StringBundler(9);

				sb.append(StringPool.PLUS);
				sb.append(Field.COMPANY_ID);
				sb.append(StringPool.COLON);
				sb.append(companyId);
				sb.append(StringPool.SPACE);
			}

			if (sb == null) {
				sb = new StringBundler(4);
			}

			sb.append(StringPool.PLUS);
			sb.append(Field.PORTLET_ID);
			sb.append(StringPool.COLON);
			sb.append(portletId);

			_solrServer.deleteByQuery(sb.toString());

			if (_commit) {
				_solrServer.commit();
			}
		}
		catch (Exception e) {
			_log.error(e, e);

			throw new SearchException(e.getMessage());
		}
	}

	public void setCommit(boolean commit) {
		_commit = commit;
	}

	public void setSolrServer(SolrServer solrServer) {
		_solrServer = solrServer;
	}

	@Override
	public void updateDocument(SearchContext searchContext, Document document)
		throws SearchException {

		deleteDocument(searchContext, document.getUID());

		addDocument(searchContext, document);
	}

	@Override
	public void updateDocuments(
			SearchContext searchContext, Collection<Document> documents)
		throws SearchException {

		if (documents.isEmpty()) {
			return;
		}

		// LPS-41388

		Iterator<Document> itr = documents.iterator();

		Document firstDocument = itr.next();

		String uid = firstDocument.getUID();

		int pos = uid.indexOf(StringPool.UNDERLINE);

		String portletId = uid.substring(0, pos);

		deletePortletDocuments(searchContext, portletId);

		addDocuments(searchContext, documents);
	}

	protected SolrInputDocument getSolrInputDocument(Document document) {
		SolrInputDocument solrInputDocument = new SolrInputDocument();

		Collection<Field> fields = document.getFields().values();

		for (Field field : fields) {
			String name = field.getName();
			float boost = field.getBoost();

			if (ArrayUtil.contains(Field.UNSCORED_FIELD_NAMES, name)) {
				boost = _UNSCORED_FIELDS_BOOST;
			}

			if (!field.isLocalized()) {
				for (String value : field.getValues()) {
					if (Validator.isNull(value)) {
						continue;
					}

					solrInputDocument.addField(name, value.trim(), boost);
				}
			}
			else {
				Map<Locale, String> localizedValues =
					field.getLocalizedValues();

				for (Map.Entry<Locale, String> entry :
						localizedValues.entrySet()) {

					String value = entry.getValue();

					if (Validator.isNull(value)) {
						continue;
					}

					Locale locale = entry.getKey();

					String languageId = LocaleUtil.toLanguageId(locale);

					String defaultLanguageId = LocaleUtil.toLanguageId(
						LocaleUtil.getDefault());

					if (languageId.equals(defaultLanguageId)) {
						solrInputDocument.addField(name, value.trim(), boost);
					}

					String localizedName = DocumentImpl.getLocalizedName(
						locale, name);

					solrInputDocument.addField(
						localizedName, value.trim(), boost);
				}
			}
		}

		return solrInputDocument;
	}

	protected Collection<SolrInputDocument> getSolrInputDocuments(
		Collection<Document> documents) {

		List<SolrInputDocument> solrInputDocuments =
			new ArrayList<SolrInputDocument>(documents.size());

		for (Document document : documents) {
			SolrInputDocument solrInputDocument = getSolrInputDocument(
				document);

			solrInputDocuments.add(solrInputDocument);
		}

		return solrInputDocuments;
	}

	private static final float _UNSCORED_FIELDS_BOOST = 1;

	private static Log _log = LogFactoryUtil.getLog(SolrIndexWriter.class);

	private boolean _commit;
	private SolrServer _solrServer;

}