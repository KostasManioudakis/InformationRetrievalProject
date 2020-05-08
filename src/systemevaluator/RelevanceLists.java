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
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Finds relevant, very relevant and nonrelavant documents for a given topic, according to qrels.txt
 * @author Kostas
 */
public class RelevanceLists {
    static final String qrelsPath = "qrels.txt";
    
    private final int topicNumber;
    private ArrayList<String> relevantDocs; //<id of doc>
    private ArrayList<String> veryRelevantDocs; //<id of doc, rank of doc>  
    private ArrayList<String> nonrelevantDocs; //<id of doc, rank of doc>
    
    public RelevanceLists(int topicNum) {
        if (topicNum<=1) this.topicNumber = 1;
        else if (topicNum>=30) this.topicNumber = 30;
        else this.topicNumber = topicNum;
        
        relevantDocs = new ArrayList<String>();
        veryRelevantDocs = new ArrayList<String>();
        nonrelevantDocs = new ArrayList<String>();
        
        initRelevanceLists();
        
    }

    private void initRelevanceLists() {
        
        try {
            File qrelsfile = new File(qrelsPath);
            BufferedReader reader = new BufferedReader(new FileReader(qrelsfile));
            String line;
            while((line = reader.readLine())!=null) {
                String[] columns = line.split("\t");
                int tn = Integer.parseInt(columns[0]);
                if (tn==topicNumber) {
                    int rel = Integer.parseInt(columns[3]);
                    if (rel==0) {
                        nonrelevantDocs.add(columns[2]);
                    }
                    else if (rel==1) {
                        relevantDocs.add(columns[2]);
                    }
                    else if (rel==2) {
                        veryRelevantDocs.add(columns[2]);
                    }
                }//end if
            }//end while
            
            reader.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RelevanceLists.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RelevanceLists.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
    }

    public static String getQrelsPath() {
        return qrelsPath;
    }

    public int getTopicNumber() {
        return topicNumber;
    }

    public ArrayList<String> getRelevantDocs() {
        return relevantDocs;
    }

    public ArrayList<String> getVeryRelevantDocs() {
        return veryRelevantDocs;
    }

    public ArrayList<String> getNonrelevantDocs() {
        return nonrelevantDocs;
    }
    
    
    
}
