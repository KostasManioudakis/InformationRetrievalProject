package queryevaluator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.ArrayList;

import mitos.stemmer.Stemmer;
import indexer.DocumentsFile;
import indexer.PostingFile;
import indexer.VocabularyFile;
import utilities.StringOperations;
import utilities.Stopwords;

import gr.uoc.csd.hy463.Topic;
import gr.uoc.csd.hy463.TopicsReader;

/**
 * @version 20.6
 * @author Kostas
 */

public class Searcher {
	
	static HashMap<String, HashSet<Long>> vocabulary = new HashMap<String, HashSet<Long>>(); //vocabulary file brought to memory
	static int MAX_FILES;
	
	private static Scanner scanner;
	private static RandomAccessFile postFile;
    private static RandomAccessFile docsFile;
	
	
	public static void main(String[] args) {
		Stemmer.Initialize();
		Stopwords.initStopwords();
		loadVocabulary();		
		
		try {			
			postFile = new RandomAccessFile(VocabularyFile.indexFolderName+"\\"+PostingFile.postingFileName, "r");
			docsFile = new RandomAccessFile(VocabularyFile.indexFolderName+"\\"+DocumentsFile.documentsFileName, "r");
			scanner = new Scanner(System.in);
			
			searchOrTest();
			
			scanner.close();
			postFile.close();
			docsFile.close();		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Program terminated.");
	}
	
	private static void loadVocabulary() {
		System.out.println("Loading vocabulary...");
		long start_time = Calendar.getInstance().getTimeInMillis();
		long end_time = 0;
		//copy vocFile to vocabulary so it is available in RAM
		File vocFile = new File(VocabularyFile.indexFolderName+"\\"+VocabularyFile.vocabularyFileName);
		try {
			FileReader fileReader = new FileReader(vocFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String vocLine;
			while ((vocLine = bufferedReader.readLine()) != null) {
				String[] vocRecord = vocLine.split("\t");
				if (vocRecord == null || vocRecord.length != VocabularyFile.REC_SZ) 
					error("Error when reading vocabuary file"); //must have word and at least one pointer to posting file
					
				HashSet<Long> pointers = new HashSet<Long>();
				for(String ptr: vocRecord[1].split(";"))
					pointers.add(Long.parseLong(ptr));
				vocabulary.put(vocRecord[0], pointers);
			}
			end_time = Calendar.getInstance().getTimeInMillis();
			bufferedReader.close();
			fileReader.close();
		}
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("OK (" + (end_time - start_time) + " ms)\n");
	}
	
	
	private static void searchOrTest() {		
		while(true) {
			System.out.println("What do you want to do?\n1. Search\n2. Test the IR system\n3. Exit");
			
			int mode = Integer.parseInt(scanner.nextLine());
			
			while(mode < 1 || mode > 3) {
				System.out.println("Wrong input. Try again");
				mode = Integer.parseInt(scanner.nextLine());
			}
			
			if(mode == 1) {
				try {
					doSearch();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(mode == 2) {
				testIRSystem();
			}
			else {
				return;
			}
		}
	}
	
	
	private static void testIRSystem() {
		long start_time, end_time;
		start_time = Calendar.getInstance().getTimeInMillis();
		try {
			File f = new File("results.txt");
			if(f.exists() && !f.isDirectory())
				f.delete();
			
			ArrayList<Topic> topics = TopicsReader.readTopics("topics.xml");

			for(Topic topic: topics.subList(0, 10))
				findDocs(topic.getSummary(), 1, topic.getNumber());
			for(Topic topic: topics.subList(10, 20))
				findDocs(topic.getSummary(), 2, topic.getNumber());
			for(Topic topic: topics.subList(20, 30))
				findDocs(topic.getSummary(), 3, topic.getNumber());
			
			end_time = Calendar.getInstance().getTimeInMillis();
			System.out.println("Searching test completed. Check results.txt (" + (end_time - start_time) + " ms)");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static void doSearch() throws IOException {
		System.out.println("Which mode do you need?\n1. Diagnosis\n2. Test\n3. Treatment\n4. Simple search");
		int mode = Integer.parseInt(scanner.nextLine());
		while(mode < 1 || mode > 4) {
			System.out.println("Wrong input. Try again");
			mode = Integer.parseInt(scanner.nextLine());
		}
		System.out.println("What are you searching for ?");		
		
		String query = scanner.nextLine();
		System.out.println("Query: "+query);
		findDocs(query, mode, -1);
	}
	
	
	@SuppressWarnings("unchecked")
	private static void findDocs(String query, int mode, int topicNo) {
		if (query == null) return;
		
		//get number of files indexed
		try {
			FileReader fileReader = new FileReader("max_files_no.txt");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			MAX_FILES = Integer.valueOf(bufferedReader.readLine());
			bufferedReader.close();
			fileReader.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {			        
	        long start_time = Calendar.getInstance().getTimeInMillis();
	        
	        Query queryObj = new Query(query);
	        int maxFrequency = queryObj.getMaxFrequency();	//highest frequency of query's terms	
	        
	        if(mode != -1) {
		        switch (mode) {
		        	case 1: 
		        		queryObj.addTerm(Stemmer.Stem("Diagnosis"), maxFrequency);
		        		break;
		        	case 2:
		        		queryObj.addTerm(Stemmer.Stem("Test"), maxFrequency);
		        		break;
		        	case 3:
		        		queryObj.addTerm(Stemmer.Stem("Treatment"), maxFrequency);
		        		break;
		        }
	        }
	        
	        Object[] sortedDocIDs = getBestMatchDocs(queryObj);
	        
	        long end_time = Calendar.getInstance().getTimeInMillis();
	        long time_length = end_time - start_time;
	        	        
	        printResults(sortedDocIDs, topicNo, time_length);	        	       
        	
	    } catch(IOException e) {
	    	e.printStackTrace();
	    	return;
	    }
	}
	
	
	private static Object[] getBestMatchDocs(Query queryObj) throws IOException {
		
        TreeMap<String, TreeMap<String, Long>> docsWithWords = getDocsWithWords(queryObj);   
        TreeMap<String, Double> docIDs = scoreDocumentsVSM(queryObj, docsWithWords);      
        
        //sort the documents by score
        Object[] sortedDocIDs = docIDs.entrySet().toArray();
        Arrays.sort(sortedDocIDs, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return ((Entry<Long, Double>) o2).getValue().compareTo(((Entry<Long, Double>) o1).getValue());
			}
		});
        
        return sortedDocIDs;
	}
	
	
	private static TreeMap<String, TreeMap<String, Long>> getDocsWithWords(Query queryObj) throws IOException {
		
		TreeMap<String, Integer> queryTerms = queryObj.getTermFreqs();	//query given by user divided by word. word -> number of appearances
		TreeMap<String, TreeMap<String, Long>> docsWithWords = new TreeMap<String, TreeMap<String, Long>>(); //keep which doc contains which word of the query
		
		for(Entry<String, Integer> termFreq: queryTerms.entrySet()) {
        	String word = termFreq.getKey();
        	if(vocabulary.containsKey(word)) {

	           	for(Long pointer: vocabulary.get(word)) {
	           		postFile.seek(pointer);
                	String[] postRec = postFile.readLine().split("\t");
                	if (postRec == null || postRec.length != PostingFile.REC_SZ) 
                		error("Error when reading posting file");
                		
                	docsFile.seek(Long.parseLong(postRec[2]));
			        String[] docsRec = docsFile.readLine().split("\t");
			        if (docsRec == null || docsRec.length != DocumentsFile.REC_SZ) 
			        	error("Error when reading documents file");
                	String docName = docsRec[1];	                	
                		
                	if(docsWithWords.containsKey(docName)) //if there is info for a document
                		//store { docName -> { common word in query and doc -> pointer to postingFile } }
			        	docsWithWords.get(docName).put(word, pointer);
			        else { //else create info and store it
			        	TreeMap<String, Long> tmp = new TreeMap<String, Long>();
			        	tmp.put(word, pointer);			        		
			        	docsWithWords.put(docName, tmp);
			        }
	           	}
        	}
        }
		
		return docsWithWords;
	}
	
	
	private static TreeMap<String, Double> scoreDocumentsVSM(Query queryObj, TreeMap<String, TreeMap<String, Long>> docsWithWords) throws IOException {
		
		TreeMap<String, Double> queryWeights = queryObj.getWeights();	//weight of each term of query	        
        double query_norm = queryObj.getNorm();
		TreeMap<String, Double> docIDs = new TreeMap<String, Double>();	//Path of Docs having the word(s) from the query and their score to query
		
		// calculate inner_product and score the documents
        for(Entry<String, TreeMap<String, Long>> documentWords: docsWithWords.entrySet()) {
        	double inner_product = 0.0;
        	String[] postRec = null;
        	for(Entry<String, Long> wordAndPointer: documentWords.getValue().entrySet()) {
        		String term = wordAndPointer.getKey(); //store the word to avoid get multiple times
        		postFile.seek(wordAndPointer.getValue());
        		postRec = postFile.readLine().split("\t");
        		if (postRec == null || postRec.length != PostingFile.REC_SZ) 
        			error("Error when reading posting file");
        		double docfreq = (double) vocabulary.get(term).size();
        		double IDF = (double) MAX_FILES / docfreq;
        		double docTF = Double.parseDouble(postRec[1]);
        		double dw = docTF * IDF;
        		double qw = queryWeights.get(term);
        		inner_product += dw * qw;
        	}
        	//all the words refer to the same group in docsFile so I seek it in the end
        	docsFile.seek(Long.parseLong(postRec[2]));
    		String[] docsRec = docsFile.readLine().split("\t");
    		if (docsRec == null || docsRec.length != DocumentsFile.REC_SZ) 
    			error("Error when reading documents file");
    		double doc_norm = Double.parseDouble(docsRec[2]);
    		double cosSim = inner_product / (query_norm * doc_norm);
    		docIDs.put(documentWords.getKey(), cosSim);
        }
		
		return docIDs;
	}
	

	private static void printResults(Object[] sortedDocIDs, int topicNo, long time_length) throws IOException {
		FileWriter fileWriter = new FileWriter("results.txt", true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        
        int counter = 1; //counter for printing the documents
        
        if(topicNo != -1) {
        	for(Object e: sortedDocIDs) {
        		if (counter > 1000)
        			break;
        		Entry<Long, Double> entry = ((Entry<Long, Double>) e);
	        	bufferedWriter.write(topicNo + "\t" + 0 + "\t" + entry.getKey() + "\t" + counter + "\t" + entry.getValue() + "\n");
				++counter;
        	}
        	System.out.println(sortedDocIDs.length + " results (" + time_length + " ms) for topic" + topicNo + "\n\n");
        }
        else {
        	System.out.println("Results:\n");
        	for(Object e: sortedDocIDs) {
        		if (counter > 1000)
        			break;
        		Entry<Long, Double> entry = ((Entry<Long, Double>) e);
        		if(counter <= MAX_FILES && entry.getValue() > 0) {
        			System.out.println(counter + ") " + entry.getKey() + "\t" + entry.getValue() + "\n");	        			
        			bufferedWriter.write(counter + ") " + entry.getKey() + "\t" + entry.getValue() + "\n");
        			++counter;
        		}
        	}
        	System.out.println("\n" + sortedDocIDs.length + " results (" + time_length + " ms)\n\n");
        }
        
        bufferedWriter.close();
    	fileWriter.close();
	}
	
	private static void error(String msg) {
		System.err.println(msg);
		System.exit(-1);
	}
}