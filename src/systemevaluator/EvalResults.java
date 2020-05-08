/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package systemevaluator;

import gr.uoc.csd.hy463.Topic;
import gr.uoc.csd.hy463.TopicsReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * For each topic, finds the metrics and writes them to eval_results.txt
 * It also calculates statistics about these metrics and writes them to stats.txt
 * @author Kostas
 */
public class EvalResults {
    final String evalResultsPath = "eval_results.txt";
    final String statsPath = "stats.txt";
    
    private double max_bpref;
    private double min_bpref;
    private double av_bpref;
    private double max_AveP;
    private double min_AveP;
    private double av_AveP;
    private double max_nDCG;
    private double min_nDCG;
    private double av_nDCG;
            
    
    public EvalResults() {       
        this.av_bpref = 0.0;               
        this.av_AveP = 0.0;
        this.av_nDCG = 0.0;      
        
        evaluateResults();
        createStats();
    }
    
    private void evaluateResults() {
        try {
            File evalFile = new File(evalResultsPath);
            FileWriter writer = new FileWriter(evalFile);       
            
            ArrayList<Topic> topics = TopicsReader.readTopics("topics.xml");
            Topic t1 = topics.get(0);
            MetricsCalculator mc1 = new MetricsCalculator(t1.getNumber());
            max_bpref = min_bpref = mc1.getBpref();
            max_AveP = min_AveP = mc1.getAveP();
            max_nDCG = min_nDCG = mc1.getnDCG();
            
            for (Topic topic: topics) {
                MetricsCalculator mc = new MetricsCalculator(topic.getNumber());
                writer.append(topic.getNumber()+"\t");
                String bpref_str = ((Double)mc.getBpref()).toString().replaceAll("\\.", ",");
                writer.append(bpref_str+"\t");
                String avep_str = ((Double)mc.getAveP()).toString().replaceAll("\\.", ",");
                writer.append(avep_str+"\t");
                String ndcg_str = ((Double)mc.getnDCG()).toString().replaceAll("\\.", ",");
                writer.append(ndcg_str+"\r\n");
                
                //calculating stats
                if (mc.getBpref()>max_bpref) max_bpref = mc.getBpref();
                if (mc.getBpref()<min_bpref) min_bpref = mc.getBpref();
                av_bpref += mc.getBpref();                            
                
                if (mc.getAveP()>max_AveP) max_AveP = mc.getAveP();
                if (mc.getAveP()<min_AveP) min_AveP = mc.getAveP();
                av_AveP += mc.getAveP();
                
                if (mc.getnDCG()>max_nDCG) max_nDCG = mc.getnDCG();
                if (mc.getnDCG()<min_nDCG) min_nDCG = mc.getnDCG();
                av_nDCG += mc.getnDCG();
                
            }//end for
            
            av_bpref = av_bpref/(double)topics.size();
            av_AveP = av_AveP/(double)topics.size();
            av_nDCG = av_nDCG/(double)topics.size();
            
            writer.close();
        } catch (Exception ex) {
            Logger.getLogger(EvalResults.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createStats() {
        FileWriter statswriter = null;
        try {
            File statsFile = new File(statsPath);
            statswriter = new FileWriter(statsFile);
            
            statswriter.append("METRIC\tMAX\tMIN\tAVERAGE\r\n\r\n");
            statswriter.append("bpref\t"+max_bpref+"\t"+min_bpref+"\t"+av_bpref+"\r\n");
            statswriter.append("AveP\t"+max_AveP+"\t"+min_AveP+"\t"+av_AveP+"\r\n");
            statswriter.append("nDCG\t"+max_nDCG+"\t"+min_nDCG+"\t"+av_nDCG+"\r\n");
            
            statswriter.close();
        } catch (IOException ex) {
            Logger.getLogger(EvalResults.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
}
