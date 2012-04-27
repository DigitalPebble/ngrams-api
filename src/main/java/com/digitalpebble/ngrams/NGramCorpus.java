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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

public class NGramCorpus {

	// total number of tokens in corpus
	long totalNumberTokens = -1;

	IndexSearcher searcher = null;

	/**
	 * Loads the indexReaders from the directory
	 * 
	 * @throws IOException
	 **/
	public NGramCorpus(String indexDirectory) throws IOException {
		Directory directory = new SimpleFSDirectory(new File(indexDirectory));
		searcher = new IndexSearcher(directory);
		// get the total number of tokens for this corpus
		// which is stored for 1-grams
		TermQuery tq = new TermQuery(new Term("totalTokenNums", "1"));
		totalNumberTokens = getOccurrences(tq);
	}

	/**
	 * Returns the total number of tokens in the collection
	 **/
	public long getTotalNumberTokens() {
		return totalNumberTokens;
	}

	public long getOccurrences(String term) throws IOException {
		return getOccurrences(term, false);
	}

	/**
	 * Returns the frequency of a term. The length in ngrams is determined by
	 * splitting the input string on space characters
	 */

	public long getOccurrences(String term, boolean lowercase)
			throws IOException {
		String[] tokens = term.split("\\s");
		List<String> tokList = new ArrayList<String>();
		// ignore empty cells
		int length = 0;
		for (String s : tokens) {
			s = s.trim();
			if (s.length() > 0) {
				length++;
				tokList.add(s);
			}
		}
		return getOccurrences(tokList, lowercase);
	}

	/** Returns the frequency of a term given a list of its tokens **/
	public long getOccurrences(final List<String> tokens, boolean lowercase)
			throws IOException {
		// queries the Lucene index for the occurrences
		// for the exact sequence of ngrams

		String ngrams = Integer.toString(tokens.size());

		PhraseQuery pq = new PhraseQuery();
		pq.setSlop(0);

		String fieldName = "text";
		if (lowercase)
			fieldName = "lowercase";

		for (String s : tokens) {
			if (lowercase)
				s = s.toLowerCase();
			Term t = new Term(fieldName, s);
			pq.add(t);
		}

		TermQuery tq = new TermQuery(new Term("length", ngrams));

		BooleanQuery bq = new BooleanQuery();
		bq.add(pq, Occur.MUST);
		bq.add(tq, Occur.MUST);

		return getOccurrences(bq);
	}

	public long getOccurrences(final Query query) throws IOException {
		NGramCollector collector = new NGramCollector();
		collector.setCollectNgrams(false);
		searcher.search(query, collector);
		return collector.getTotalOccurrences();
	}

	public Map<String, Long> getForms(final Query query) throws IOException {
		NGramCollector collector = new NGramCollector();
		collector.setCollectNgrams(true);
		searcher.search(query, collector);
		return collector.getMap();
	}

	public static void main(String[] args) {
		try {
			NGramCorpus ngrams = new NGramCorpus(args[0]);
			long occ = ngrams.getOccurrences(args[1]);
			long total = ngrams.getTotalNumberTokens();
			System.out.println("Found " + occ + " for : " + args[1]);
			System.out.println("Total tokens for whole corpus " + total);
		} catch (IOException e) {
			e.printStackTrace(System.err);
			return;
		}

	}

}
