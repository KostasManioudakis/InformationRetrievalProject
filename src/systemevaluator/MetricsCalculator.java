/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package systemevaluator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Calculates bpref, AveP and nDCG for a given topic
 * @author Kostas
 */
public class MetricsCalculator {
    private final int topicNumber;
    private CondensedList cl;
    private double bpref;
    private double AveP;
    private double nDCG;
    
    public MetricsCalculator(int topicNum) {
        if (topicNum<=1) this.topicNumber = 1;
        else if (topicNum>=30) this.topicNumber = 30;
        else this.topicNumber = topicNum;
        
        this.cl = new CondensedList((this.topicNumber));
        
        calculateBprefAveP();
        calculateNDCG();
    }

    private void calculateBprefAveP() {
        LinkedHashMap<Integer, String> judgedDocs = cl.getJudgedDocs();
        
        RelevanceLists rl = cl.getRelevanceLists();
        ArrayList<String> relDocs = rl.getRelevantDocs();
        ArrayList<String> veryRelDocs= rl.getVeryRelevantDocs(); 
        ArrayList<String> nonrelDocs = rl.getNonrelevantDocs();
        
        int R = relDocs.size() + veryRelDocs.size();
        int N = nonrelDocs.size();
        
        this.bpref = 0.0;
        this.AveP = 0.0;       
        
        for (Map.Entry<Integer, String> entrySet : judgedDocs.entrySet()) {
            Integer rank = entrySet.getKey();
            String docID = entrySet.getValue();
            
            int isrel = 0;
            if (relDocs.contains(docID) || veryRelDocs.contains(docID)) {
                isrel = 1;
            }
            
            int count = 0; //number of relavant docs with higher rank           
            for (int r=rank; r>=1; --r) {
                String currentDoc = judgedDocs.get(r);
                if (relDocs.contains(currentDoc) || veryRelDocs.contains(currentDoc)) {
                    ++count;
                }
            }
            
            AveP += isrel * (count/(double)rank);
            
            double penalty = 0.0;
            if (N>R) { //bpref_N
                if (N>0) {
                    penalty = (rank - count)/(double)N;
                }
                else {
                    penalty = 0.0;
                }
            }
            else {//bpref_R
                int nom = rank - count;
                if (R<nom) nom = R;
                if (R>0) {
                    penalty = nom/(double)R;
                }
                else {
                    penalty = 1.0;
                }
            }//end if-else
            
            bpref += isrel * (1.0 - penalty);

        }//end for
        
        
        if (R>0) {
            AveP = (1/(double)R)*AveP;
            bpref = (1/(double)R)*bpref;
        }
        else {
            AveP = 0.0;
            bpref = 0.0;
        }
    }
    
    private void calculateNDCG() {
        this.nDCG = 0.0;
       
        double dcg = 0.0;	//discounted cumulative gain
    	double idcg = 0.0;	//ideal discounted cumulative gain
    	   	
        LinkedHashMap<Integer, String> judgedDocs = cl.getJudgedDocs();
        RelevanceLists rl = cl.getRelevanceLists();
        ArrayList<String> relDocs = rl.getRelevantDocs();
        ArrayList<String> veryRelDocs= rl.getVeryRelevantDocs();
        
        int counter = 1;
        //iterate over judged docs
        for(Map.Entry<Integer, String> iterator: judgedDocs.entrySet()) {
            //find dcg
            if(relDocs.contains(iterator.getValue()))
                    dcg = dcg + (1 / logBase2(counter + 1));
            else if(veryRelDocs.contains(iterator.getValue()))
                    dcg = dcg + (3 / logBase2(counter + 1));
            //find idcg
            if(counter <= veryRelDocs.size())
                    idcg = idcg + (3 / logBase2(counter + 1));
            else if(counter > veryRelDocs.size() && counter <= (relDocs.size() + veryRelDocs.size()))
                    idcg = idcg + (1 / logBase2(counter + 1));

            ++counter;
        }
        
        if (idcg > 0.0) {
            this.nDCG = dcg / idcg;
        }
        
    }
    
    /**
     * Calculate log base 2
     * @param number - Number to be logged
     * @return number logged base 2
     */
    private static double logBase2(double number) {
    	return Math.log(number)/Math.log(2);
    }

    public int getTopicNumber() {
        return topicNumber;
    }

    public CondensedList getCl() {
        return cl;
    }

    public double getBpref() {
        return bpref;
    }

    public double getAveP() {
        return AveP;
    }

    public double getnDCG() {
        return nDCG;
    }
   
}
