/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package systemevaluator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Creates the condensed list of retrieved documents for the given topic, according to results.txt
 * @author Kostas
 */
public class CondensedList {
    static final String resultsPath = "results.txt";
    private final int topicNumber;
    private RelevanceLists rl; //the relevance lists for topic according to qrels
    private LinkedHashMap<Integer, String> judgedDocs; //<rank of doc, id of doc> (the rank is different from original)
    
    public CondensedList(int topicNum) {
        if (topicNum<=1) this.topicNumber = 1;
        else if (topicNum>=30) this.topicNumber = 30;
        else this.topicNumber = topicNum;
        
        rl = new RelevanceLists(this.topicNumber);
        
        this.judgedDocs = new LinkedHashMap<>();
        initRetrievedDocs();
    }

    private void initRetrievedDocs() {
        ArrayList<String> relDocs = rl.getRelevantDocs();
        ArrayList<String> veryRelDocs= rl.getVeryRelevantDocs(); 
        ArrayList<String> nonrelDocs = rl.getNonrelevantDocs();
        
        try {
            File resultsfile = new File(resultsPath);
            BufferedReader reader = new BufferedReader(new FileReader(resultsfile));
            String line;
            int current_rank = 1;
            while((line = reader.readLine())!=null) {
                String[] columns = line.split("\t");
                int tn = Integer.parseInt(columns[0]);
                
                if (tn==topicNumber) {
                    String docPath = columns[2]; //must extract the id of doc (name of file)
                    Pattern pattern = Pattern.compile("[0-9]{5,}");
                    Matcher matcher = pattern.matcher(docPath);
                    if (matcher.find()) {
                        String docID = matcher.group(0);
                        //checking if judged doc or unjudged doc
                        if (relDocs.contains(docID) || veryRelDocs.contains(docID) || nonrelDocs.contains(docID)) { //judged
                            if (!judgedDocs.containsKey(current_rank) && !judgedDocs.containsValue(docID)) {                                                           
                                judgedDocs.put(current_rank, docID);
                                ++current_rank;
                            }//end if 4                           
                        }//end if 3                     
                    }//end if 2
                }//end if 1               
            }//end while
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RelevanceLists.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RelevanceLists.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
    }

    public static String getResultsPath() {
        return resultsPath;
    }

    public int getTopicNumber() {
        return topicNumber;
    }

    public RelevanceLists getRelevanceLists() {
        return rl;
    }

    public LinkedHashMap<Integer, String> getJudgedDocs() {
        return judgedDocs;
    }
    
    
    
}
