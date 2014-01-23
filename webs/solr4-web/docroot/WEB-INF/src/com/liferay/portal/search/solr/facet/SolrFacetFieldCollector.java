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

package com.liferay.portal.search.solr.facet;

import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;

/**
 * @author Raymond Augé
 */
public class SolrFacetFieldCollector implements FacetCollector {

	public SolrFacetFieldCollector(String fieldName, FacetField facetField) {
		_fieldName = fieldName;

		List<Count> counts = facetField.getValues();

		if (counts != null) {
			for (Count count : counts) {
				_counts.put(count.getName(), count);
			}
		}
	}

	@Override
	public String getFieldName() {
		return _fieldName;
	}

	@Override
	public TermCollector getTermCollector(String term) {
		Count count = _counts.get(term);

		int occurences = 0;

		if (count != null) {
			occurences = (int)count.getCount();
		}

		return new SolrTermCollector(term, occurences);
	}

	@Override
	public List<TermCollector> getTermCollectors() {
		if (_termCollectors != null) {
			return _termCollectors;
		}

		List<TermCollector> termCollectors = new ArrayList<TermCollector>();

		for (Map.Entry<String, Count> entry : _counts.entrySet()) {
			Count count = entry.getValue();

			TermCollector termCollector = new SolrTermCollector(
				entry.getKey(), (int)count.getCount());

			termCollectors.add(termCollector);
		}

		_termCollectors = termCollectors;

		return _termCollectors;
	}

	private Map<String, Count> _counts = new LinkedHashMap<String, Count>();
	private String _fieldName;
	private List<TermCollector> _termCollectors;

}