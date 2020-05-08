package queryevaluator;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import mitos.stemmer.Stemmer;
import utilities.Stopwords;
import utilities.StringOperations;

public class Query {
	public static int MAX_SIZE = 100; //maximum number of terms allowed
	private String query;
	private TreeMap<String, Integer> termFreqs;
	private TreeMap<String, Double> weights;
	private TreeMap<String, Double> TFs;
	private double norm;
	private int maxFrequency;
	
	
	public Query(String q) {
		assert(Searcher.vocabulary!=null);
		assert(q!=null && !q.isEmpty());
		query = q;
		
		termFreqs = new TreeMap<String, Integer>();
		TFs = new TreeMap<String, Double>();
		weights = new TreeMap<String, Double>();
		norm = 0.0;
		maxFrequency = 0;
		
		findTermFreqs();
		calculateWeights();
		
	}
	
	
	public void addTerm(String term, int freq) {
		if (termFreqs.size() < MAX_SIZE) {
			termFreqs.put(term, freq);
			if (freq > maxFrequency) {
				maxFrequency = freq;
			}
			calculateWeights();
		}		
	}
	
	private void findTermFreqs() {
		termFreqs.clear();
		StringTokenizer tokenizer = new StringTokenizer(query, "\t\n\r\f ");
        while(tokenizer.hasMoreTokens() && termFreqs.size() <= MAX_SIZE) {
        	String term = tokenizer.nextToken();
        	term = StringOperations.removePunctuationAndWhitespace(term);
        	if(!Stopwords.isStopWord(term) && StringOperations.isValidWord(term)) {
        		term = Stemmer.Stem(term);
        		if (Searcher.vocabulary.containsKey(term)) {
        			if(!termFreqs.containsKey(term)) {
            			termFreqs.put(term, 1);
            			if(maxFrequency < 1)
            				maxFrequency = 1;
            		}
            		else {
            			termFreqs.put(term, termFreqs.get(term) + 1);
            			if(maxFrequency < termFreqs.get(term))
            				maxFrequency = termFreqs.get(term);
            		}
        		}   		
        	}
        }
	}
	
	private void calculateWeights() {
		TFs.clear();
		weights.clear();
		double sum = 0.0;
		for (Map.Entry<String, Integer> termFreq: termFreqs.entrySet()) {
			String word = termFreq.getKey();
			double tf = termFreq.getValue() / (double) maxFrequency;
			TFs.put(word, tf);
			int docFreq = Searcher.vocabulary.get(word).size();
			double idf = Searcher.MAX_FILES / (double) docFreq;
			double w = tf*idf;
			weights.put(word, w);
			sum += w*w;
		}				
		
		assert(TFs.containsValue(1.0));	//assert at least 1 term of query has tf equal to 1. Otherwise sth wrong.
		
		norm = Math.sqrt(sum);
	}
	

	public String getQuery() {
		return query;
	}

	public TreeMap<String, Integer> getTermFreqs() {
		return termFreqs;
	}

	public TreeMap<String, Double> getWeights() {
		return weights;
	}

	public double getNorm() {
		return norm;
	}

	public int getMaxFrequency() {
		return maxFrequency;
	}

}
