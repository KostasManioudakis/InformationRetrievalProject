
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import gr.uoc.csd.hy463.NXMLFileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import mitos.stemmer.Stemmer;
import static utilities.Stopwords.isStopWord;
import utilities.StringOperations;
import static utilities.StringOperations.isValidWord;

/**
 * Represents a document of the collection
 * B1
 * @author Kostas
 */
public class Document implements Comparable<Document> { //implements comparable so that it can be used as key in a map
    private String documentPath;
    private String documenName;
    private String pmcid;
    private String title;
    private String abstr;
    private String body;
    private String journal;
    private String publisher;    
    private ArrayList<String> authors;
    private HashSet<String> categories;    
    
    private double squareOfNorm; // = norm*norm, see lecture 03_RetrievalModelsI slides 25-40
    
    private TreeMap<String, Integer> termFrequencies; //<word, frequency = occurences in doc>   
    private int maxTermFrequency; //the maximum value in termFrequencies
    
    private TreeMap<String, TreeMap<String, ArrayList<Integer>>> positions; //<word, <tag name, list of positions in tag where word occurs>>
    
    public static final String[] tagNames = {"pmcid", "title", "abstr", "body", "journal", "publisher", "authors", "categories"};

    public Document(String documentPath) {
        this.documentPath = documentPath;
        this.squareOfNorm = 0.0;
        this.maxTermFrequency = 0;
    }

    public Document(String documentPath, String pmcid, String title, String abstr, String body, String journal, 
            String publisher, ArrayList<String> authors, HashSet<String> categories) {
        
        this.documentPath = documentPath;
        this.pmcid = pmcid;
        this.title = title;
        this.abstr = abstr;
        this.body = body;
        this.journal = journal;
        this.publisher = publisher;
        this.authors = authors;
        this.categories = categories;
        
        this.squareOfNorm = 0.0;
        this.maxTermFrequency = 0;
    }

    public Document(String documentPath, String documenName, String pmcid, String title, String abstr, String body, String journal, String publisher, ArrayList<String> authors, HashSet<String> categories) {
        this.documentPath = documentPath;
        this.documenName = documenName;
        this.pmcid = pmcid;
        this.title = title;
        this.abstr = abstr;
        this.body = body;
        this.journal = journal;
        this.publisher = publisher;
        this.authors = authors;
        this.categories = categories;
        
        this.squareOfNorm = 0.0;
        this.maxTermFrequency = 0;
    }   
    
    /**
    *
    * Reads contents of file with specified name and creates a Document instance.
    * @param filepath the path of file to read
    * @return the created Document instance
    * @throws java.io.IOException
    */
    public static Document createInstanceFromFile(String filepath) throws IOException {
        try {
            File example = new File(filepath);
            
            NXMLFileReader xmlFile = new NXMLFileReader(example);
            String pmcid = xmlFile.getPMCID();
            String title = xmlFile.getTitle();
            String abstr = xmlFile.getAbstr();
            String body = xmlFile.getBody();
            String journal = xmlFile.getJournal();
            String publisher = xmlFile.getPublisher();
            ArrayList<String> authors = xmlFile.getAuthors();
            HashSet<String> categories =xmlFile.getCategories();
            
            Document doc = new Document(filepath);
            doc.setDocumenName(example.getName());
            doc.setTitle(title);
            doc.setAbstr(abstr);
            doc.setAuthors(authors);
            doc.setBody(body);
            doc.setCategories(categories);
            doc.setJournal(journal);
            doc.setPmcid(pmcid);
            doc.setPublisher(publisher);
            
            return doc;
            
        } catch (IOException ex) {
            Logger.getLogger(Document.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }
    

    public void findWords() {
        if (termFrequencies!=null) {
            if (!termFrequencies.isEmpty()) return;
        }
        termFrequencies = new TreeMap<>();
        maxTermFrequency = 0;
        for (String tagname: tagNames) {
            findWordsInTag(tagname);
        }
        assert(termFrequencies.containsValue(maxTermFrequency));
    }
    
    private void findWordsInTag(String tagname) {       
        String tagContent;
        switch (tagname) {
            case "body":
                tagContent = body;
                break;
            case "title":
                tagContent = title;
                break;
            case "abstr":
                tagContent = abstr;
                break;
            case "pmcid":
                tagContent = pmcid;
                break;
            case "journal":
                tagContent = journal;
                break;
            case "publisher":
                tagContent = publisher;
                break;
            case "authors": {
                for (String author: authors) {         
                    author = StringOperations.removePunctuationAndWhitespace(author);
                    if (!isStopWord(author)) if(isValidWord(author)) {
                        
                        author = Stemmer.Stem(author);

                        if (!termFrequencies.containsKey(author)) {
                            termFrequencies.put(author, 1); 
                            if (maxTermFrequency<1) maxTermFrequency = 1;
                        }
                        else {
                            int oldval = termFrequencies.get(author);
                            termFrequencies.put(author, (oldval+1));
                            if (maxTermFrequency<oldval+1) maxTermFrequency = oldval+1;
                        }//end if-else
                    }   
                        
                }//end for
                return;
            }
            case "categories": {
                for (String categ: categories) {
                    categ = StringOperations.removePunctuationAndWhitespace(categ);   
                    if (!isStopWord(categ)) if(isValidWord(categ)) {
                                                           
                        categ = Stemmer.Stem(categ);                    

                        if (!termFrequencies.containsKey(categ)) {
                            termFrequencies.put(categ, 1);
                            if (maxTermFrequency<1) maxTermFrequency = 1;
                        }
                        else {
                            int oldval = termFrequencies.get(categ);
                            termFrequencies.put(categ, (oldval+1));
                            if (maxTermFrequency<oldval+1) maxTermFrequency = oldval+1;
                        }//end if-else
                    }
                        
                }//end for
                return;
            }
            default:{
                return;
            }
        }//end switch
        
        if (tagContent==null) return;
        if (tagContent.isEmpty()) return;
        
        String delimiter = "\t\n\r\f ";
        StringTokenizer tokenizer = new StringTokenizer(tagContent, delimiter);
        
        while(tokenizer.hasMoreTokens() ) {
            String currentToken = tokenizer.nextToken();
            currentToken = StringOperations.removePunctuationAndWhitespace(currentToken);  
            if (!isStopWord(currentToken)) if (isValidWord(currentToken)) {                                           
                currentToken = Stemmer.Stem(currentToken);

                if (!termFrequencies.containsKey(currentToken)) {
                    termFrequencies.put(currentToken, 1);
                    if (maxTermFrequency<1) maxTermFrequency = 1;
                }
                else {
                    int oldval = termFrequencies.get(currentToken);
                    termFrequencies.put(currentToken, (oldval+1));
                    if (maxTermFrequency<oldval+1) maxTermFrequency = oldval+1;
                }//end if-else
            }
                
        }//end while
        
        
    }
    
    public void findWordsAndPositions() {
        this.positions = new TreeMap();
        this.termFrequencies = new TreeMap<>();
        this.maxTermFrequency = 0;
        for (String tagname: tagNames) {
            findWordsAndPositionsInTag(tagname);
        }
    }
    
    public void findWordsAndPositionsInTag(String tagname) {
        String tagContent;
        switch (tagname) {
            case "body":
                tagContent = body;
                break;
            case "title":
                tagContent = title;
                break;
            case "abstr":
                tagContent = abstr;
                break;
            case "pmcid":
                tagContent = pmcid;
                break;
            case "journal":
                tagContent = journal;
                break;
            case "publisher":
                tagContent = publisher;
                break;
            case "authors": {
                int counter = 0;
                for (String author: authors) {
                    ++counter;
                    author = StringOperations.removePunctuationAndWhitespace(author);
                    if (!isStopWord(author)) if(isValidWord(author)) {                        
                        author = Stemmer.Stem(author);

                        if (!termFrequencies.containsKey(author)) {
                            termFrequencies.put(author, 1); 
                            if (maxTermFrequency<1) maxTermFrequency = 1;                                                                    
                        }
                        else {
                            int oldval = termFrequencies.get(author);
                            termFrequencies.put(author, (oldval+1));
                            if (maxTermFrequency<oldval+1) maxTermFrequency = oldval+1;                                          
                        }//end if-else

                        if (!positions.containsKey(author)) {
                            ArrayList<Integer> list = new ArrayList<>();
                            list.add(counter);
                            TreeMap<String, ArrayList<Integer>> tagMap = new TreeMap();
                            tagMap.put(tagname, list);
                            positions.put(author, tagMap);
                        }
                        else {
                            TreeMap<String, ArrayList<Integer>> tagMap = positions.get(author);
                            if (!tagMap.containsKey(tagname)) {
                                ArrayList<Integer> list = new ArrayList<>();
                                list.add(counter);
                                tagMap.put(tagname, list);
                            }
                            else {
                                ArrayList<Integer> list = tagMap.get(tagname);
                                list.add(counter);    
                            }
                        }//end if-else
                    }
                        
                  
                }//end for
                return;
            }
            case "categories": {
                int counter = 0;
                for (String categ: categories) {
                    ++counter;
                    categ = StringOperations.removePunctuationAndWhitespace(categ);
                    if (!isStopWord(categ)) if(isValidWord(categ)) {
                        
                        categ = Stemmer.Stem(categ);                    

                        if (!termFrequencies.containsKey(categ)) {
                            termFrequencies.put(categ, 1);
                            if (maxTermFrequency<1) maxTermFrequency = 1;                                                               
                        }
                        else {
                            int oldval = termFrequencies.get(categ);
                            termFrequencies.put(categ, (oldval+1));
                            if (maxTermFrequency<oldval+1) maxTermFrequency = oldval+1;                                            
                        }//end if-else    

                        if (!positions.containsKey(categ)) {
                            ArrayList<Integer> list = new ArrayList<>();
                            list.add(counter);
                            TreeMap<String, ArrayList<Integer>> tagMap = new TreeMap();
                            tagMap.put(tagname, list);
                            positions.put(categ, tagMap);
                        }
                        else {
                            TreeMap<String, ArrayList<Integer>> tagMap = positions.get(categ);
                            if (!tagMap.containsKey(tagname)) {
                                ArrayList<Integer> list = new ArrayList<>();
                                list.add(counter);
                                tagMap.put(tagname, list);
                            }
                            else {
                                ArrayList<Integer> list = tagMap.get(tagname);
                                list.add(counter);    
                            }
                        }//end if-else
                    }
                        
                }//end for
                return;
            }
            default:{
                return;
            }
        }//end switch
        
        if (tagContent==null) return;
        if (tagContent.isEmpty()) return;
        
        String delimiter = "\t\n\r\f ";
        StringTokenizer tokenizer = new StringTokenizer(tagContent, delimiter);
        int counter = 0;
        while(tokenizer.hasMoreTokens()) {
            ++counter;
            String currentToken = tokenizer.nextToken();
            currentToken = StringOperations.removePunctuationAndWhitespace(currentToken);  
            if (!isStopWord(currentToken)) if(isValidWord(currentToken)) {
                     
                currentToken = Stemmer.Stem(currentToken);

                if (!termFrequencies.containsKey(currentToken)) {
                    termFrequencies.put(currentToken, 1);
                    if (maxTermFrequency<1) maxTermFrequency = 1;            
                }
                else {
                    int oldval = termFrequencies.get(currentToken);
                    termFrequencies.put(currentToken, (oldval+1));
                    if (maxTermFrequency<oldval+1) maxTermFrequency = oldval+1;                            
                }//end if-else           

                if (!positions.containsKey(currentToken)) {
                    ArrayList<Integer> list = new ArrayList<>();
                    list.add(counter);
                    TreeMap<String, ArrayList<Integer>> tagMap = new TreeMap();
                    tagMap.put(tagname, list);
                    positions.put(currentToken, tagMap);
                }
                else {
                    TreeMap<String, ArrayList<Integer>> tagMap = positions.get(currentToken);
                    if (!tagMap.containsKey(tagname)) {
                        ArrayList<Integer> list = new ArrayList<>();
                        list.add(counter);
                        tagMap.put(tagname, list);
                    }
                    else {
                        ArrayList<Integer> list = tagMap.get(tagname);
                        list.add(counter);    
                    }
                }//end if-else
            }
                
            
        }//end while
        
    }
    
    public void calculateSquareNorm(TreeMap<String, ArrayList<Long>> docFreqs, int maxfiles) {
        if (docFreqs==null) {
            System.err.println("Null document frequencies");
            return;
        }
        if (maxTermFrequency==0) return;
       // if (squareOfNorm>0.0) return;
        
        for (Map.Entry<String, Integer> entrySet1: termFrequencies.entrySet()) {
            String word = entrySet1.getKey();  
            Integer freq = entrySet1.getValue();
            
            if (docFreqs.containsKey(word)) {
                Integer df = docFreqs.get(word).size();
               
                double idf = 0.0;
                if (df!=0) {
                    idf = ((double)maxfiles)/((double)df);
                }
                
                double tf = ((double)freq)/((double)maxTermFrequency);
                double weight = tf*idf;
                this.squareOfNorm += weight*weight;               
            }//end if           
        }//end for
        
    }

    public TreeMap<String, TreeMap<String, ArrayList<Integer>>> getPositions() {
        return positions;
    }
    
    

    public String getDocumentPath() {
        return documentPath;
    }

    public String getDocumenName() {
        return documenName;
    }

    public String getPmcid() {
        return pmcid;
    }

    public String getTitle() {
        return title;
    }

    public String getAbstr() {
        return abstr;
    }

    public String getBody() {
        return body;
    }

    public String getJournal() {
        return journal;
    }

    public String getPublisher() {
        return publisher;
    }

    public ArrayList<String> getAuthors() {
        return authors;
    }

    public HashSet<String> getCategories() {
        return categories;
    }

    public double getNorm() {
        return Math.sqrt(squareOfNorm);
    }

    public double getSquareOfNorm() {
        return squareOfNorm;
    }

    public TreeMap<String, Integer> getTermFrequencies() {
        return termFrequencies;
    }

    public int getMaxTermFrequency() {
        return maxTermFrequency;
    }

    
    public void setTermFrequencies(TreeMap<String, Integer> termFrequencies) {
        this.termFrequencies = termFrequencies;
    }

    public void setMaxTermFrequency(int maxTermFrequency) {
        this.maxTermFrequency = maxTermFrequency;
    }  

    public void setSquareOfNorm(double squareOfNorm) {
        this.squareOfNorm = squareOfNorm;
    }
    
    public void addToSquareOfNorm(double val) {
        this.squareOfNorm += val;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public void setDocumenName(String documenName) {
        this.documenName = documenName;
    }
    

    public void setPmcid(String pmcid) {
        this.pmcid = pmcid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAbstr(String abstr) {
        this.abstr = abstr;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setAuthors(ArrayList<String> authors) {
        this.authors = authors;
    }

    public void setCategories(HashSet<String> categories) {
        this.categories = categories;
    }
    
    
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder(documentPath);
        builder.append("\r\n");

        builder.append("- PMC ID: ").append(pmcid);
        builder.append("\n");
        builder.append("- Title: ").append(title);
        builder.append("\n");
        builder.append("- Abstract: ").append(abstr);
        builder.append("\n");
        builder.append("- Body: ").append(body);
        builder.append("\n");
        builder.append("- Journal: ").append(journal);
        builder.append("\n");
        builder.append("- Publisher: ").append(publisher);
        builder.append("\n");
        builder.append("- Authors: ").append(authors);
        builder.append("\n");
        builder.append("- Categories: ").append(categories);
        builder.append("\n");
        
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().equals(Document.class)) {
            Document doc = (Document)obj;
            return (this.documentPath.equals(doc.getDocumentPath()));
        }
        return false;
    }
    
    @Override
    public int compareTo(Document doc) {
        
        int retval = this.documentPath.compareTo(doc.getDocumentPath());
        return retval;
       
    }
    
    
    public void printTermFrequencies() {
        if (!(termFrequencies!=null && !termFrequencies.isEmpty())) return;
        
        int i = 0;
        for (Map.Entry<String, Integer> entrySet : termFrequencies.entrySet()) {
            String key = entrySet.getKey();
            Integer value = entrySet.getValue();
            ++i;
            System.out.println(i+")"+key+": "+value);            
        }
    }
    
    public void cleanSomeFields() {
        this.title = null;
        this.abstr = null;
        this.body = null;
        this.journal = null;
        this.publisher = null;
        this.authors.clear();
        this.authors = null;
        this.categories.clear();
        this.categories = null;
    }

    
    
}
