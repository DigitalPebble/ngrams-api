/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.digitalpebble.ngrams;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

/**
 * Returns a map containing the strings of the Ngrams matching the query and
 * their related frequency if setCollectNgrams is set to true. Returns the total
 * number of occurrences for the hits.
 **/

public class NGramCollector extends Collector {

	private Map<String, Long> ngramForms = new HashMap<String, Long>();
	private IndexReader reader;
	private long totalOccurrences = 0l;

	boolean collectNgrams = false;

	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	public void collect(int docNum) throws IOException {
		Document doc = reader.document(docNum);
		Long value = Long.parseLong(doc.get("value"));
		if (isCollectNgrams()) {
			String key = doc.get("text");
			ngramForms.put(key, value);
		}
		totalOccurrences += value;
	}

	public void setNextReader(IndexReader reader, int docBase)
			throws IOException {
		this.reader = reader;
	}

	public void setScorer(Scorer scorer) throws IOException {
	}

	public Map<String, Long> getMap() {
		return ngramForms;
	}

	public long getTotalOccurrences() {
		return totalOccurrences;
	}

	public void clear() {
		totalOccurrences = 0l;
		ngramForms = new HashMap<String, Long>();
	}

	public boolean isCollectNgrams() {
		return collectNgrams;
	}

	public void setCollectNgrams(boolean collectNgrams) {
		this.collectNgrams = collectNgrams;
	}

}
