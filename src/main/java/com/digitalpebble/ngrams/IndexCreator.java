package com.digitalpebble.ngrams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;

public class IndexCreator {

	IndexWriter writer = null;

	/**
	 * Builds a simple index for web-1t. Simply takes the root directory of the
	 * unzipped corpus and create a Lucene index
	 * 
	 * @throws IOException
	 * @throws LockObtainFailedException
	 * @throws CorruptIndexException
	 */
	public static void main(String[] args) throws CorruptIndexException,
			LockObtainFailedException, IOException {

		if (args.length < 1 || args.length > 2) {
			System.err.println("IndexCreator root_ngram_dir index_dir");
			return;
		}

		new IndexCreator(args[0], args[1]);
	}

	public IndexCreator(String corpusLocation, String indexLocation)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		Directory directory = new SimpleFSDirectory(new File(indexLocation));

		writer = new IndexWriter(directory, new WhitespaceAnalyzer(),
				IndexWriter.MaxFieldLength.UNLIMITED);

		// scan files in the corpus and stores the names in an array
		// we will use the position of the array for the terms
		// special case for .idx files and 1gms
		scanCorpus(corpusLocation);

		writer.optimize();
		writer.close();
	}

	private void scanCorpus(String corpusLocation) {
		// get main directories
		File root = new File(corpusLocation);
		String[] mainDirs = root.list();
		Arrays.sort(mainDirs);
		// they need to be of *gms
		for (String d : mainDirs) {
			// special case for 1-gram
			if (d.equals("1gms")) {
				int size = 1;
				// copy file indicating overall number of tokens
				try {
					writeTokenTotalFile(size, new File(corpusLocation
							+ File.separator + d + File.separator + "total"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				// only interested in file vocab
				File target = new File(corpusLocation + File.separator + d
						+ File.separator + "vocab");
				try {
					List<File> files = new ArrayList<File>();
					files.add(target);
					createIndexFileForlengthN("1", files);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (d.matches(".gms")) {
				// get the size
				String size = "" + d.charAt(0);
				// get the files and sort them by alphabetical order
				File subdir = new File(corpusLocation + File.separator + d);
				List<String> subFiles = java.util.Arrays.asList(subdir.list());
				java.util.Collections.sort(subFiles);
				List<File> subFilesF = new ArrayList<File>();
				for (String ff : subFiles) {
					if (ff.endsWith(".idx") == false) {
						File f = new File(subdir + File.separator + ff);
						subFilesF.add(f);
					}
				}
				try {
					createIndexFileForlengthN(size, subFilesF);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private final void writeTokenTotalFile(int size, File ff)
			throws IOException {
		BufferedReader fr = new BufferedReader(new FileReader(ff));
		String totalNumberTokens = fr.readLine();
		fr.close();

		// generate a document containing the number of tokens for
		// this NGram size
		Document doc = new Document();
		Field tokenNumF = new Field("totalTokenNums", "" + size,
				Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
		Field value = new Field("value", totalNumberTokens, Field.Store.YES,
				Field.Index.NO);
		doc.add(tokenNumF);
		doc.add(value);
		writer.addDocument(doc);
	}

	// get a list of files to parse for a given length
	private void createIndexFileForlengthN(String length, List<File> targetFiles)
			throws IOException {
		// open the target files one by one
		// and pass the writer for the index
		int num = 0;
		for (File target : targetFiles) {
			readSourceFile(length, target, num);
			num++;
		}
	}

	private void readSourceFile(String ngram, File source, int fileRank)
			throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(source));
		// read line by line
		String line = null;
		System.out.println("Processing " + source.getAbsolutePath());

		while ((line = reader.readLine()) != null) {
			int postab = line.indexOf("\t");
			String form = line.substring(0, postab);
			String count = line.substring(postab + 1);
			Document doc = new Document();
			Field textF = new Field("text", form, Field.Store.YES,
					Field.Index.ANALYZED_NO_NORMS);
			Field lowerCasedF = new Field("lowercase", form.toLowerCase(), Field.Store.NO,
					Field.Index.ANALYZED_NO_NORMS);
			Field lengthF = new Field("length", ngram, Field.Store.NO,
					Field.Index.NOT_ANALYZED_NO_NORMS);
			Field value = new Field("value", count, Field.Store.YES,
					Field.Index.NO);
			doc.add(textF);
			doc.add(lowerCasedF);
			doc.add(lengthF);
			doc.add(value);
			writer.addDocument(doc);
		}
		reader.close();
	}

}
