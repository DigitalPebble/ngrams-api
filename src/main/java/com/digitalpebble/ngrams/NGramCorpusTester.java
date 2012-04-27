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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class NGramCorpusTester {

	/***************************************************************************
	 * Load a file of frequencies at the google format e.g. term \t occurrences
	 * and compare the results with what is retrieved by the API
	 * 
	 * @throws IOException
	 * @throws NumberFormatException
	 **************************************************************************/
	public static void main(String[] args) throws NumberFormatException,
			IOException {
		if (args.length != 2) {
			System.err.println("NGramCorpusTester ngram_file index_Directory");
			return;
		}
		new NGramCorpusTester(args[0], args[1]);
	}

	public NGramCorpusTester(String file, String indexDi)
			throws NumberFormatException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		// read line by line
		String line = null;

		NGramCorpus index = new NGramCorpus(indexDi);

		long l0 = System.currentTimeMillis();

		int numEntities = 0;

		while ((line = reader.readLine()) != null) {
			if (numEntities % 10000 == 0 && numEntities != 0) {
				long l1 = System.currentTimeMillis();
				int timesec = (int) (l1 - l0) / 1000;
				int qps = numEntities / timesec;
				System.out.println("Obtained data for " + numEntities + " in "
						+ (l1 - l0) + " msec [" + qps + " queries per sec]");
			}
			int sep = line.indexOf("\t");
			if (sep == -1) {
				System.out.println("Incorrect input : " + line);
				continue;
			}

			String form = line.substring(0, sep);
			String freq = line.substring(sep + 1);
			Long expected = Long.parseLong(freq);
			double obtained = index.getOccurrences(form);
			if (obtained != expected) {
				System.out.println(form + "\texpected: " + expected
						+ "\tbut got: " + obtained);
				// break;
			}
			numEntities++;

		}

		long l1 = System.currentTimeMillis();
		int timesec = (int) (l1 - l0) / 1000;
		int qps = numEntities / timesec;
		System.out.println("Obtained data for " + numEntities + " in "
				+ (l1 - l0) + " msec [" + qps + " queries per sec]");
	}

}
