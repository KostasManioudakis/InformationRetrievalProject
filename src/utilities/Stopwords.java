/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Handles stopwords
 * @author Kostas
 */
public class Stopwords {
    public static final String enStopwordsFilename = "stopwordsEn.txt";
    public static final String grStopwordsFilename = "stopwordsGr.txt";
    
    public static HashSet<String> enStopWords;
    public static HashSet<String> grStopWords;
    
    private static boolean initialized = false;
    
    public static void initStopwords() {
        enStopWords = new HashSet<>();
        grStopWords = new HashSet<>();
        initEnStopwords();
        initGrStopwords();
        initialized = true;
    }
    
    private static void initEnStopwords() {
        FileReader fileReader = null;
        try {
            File stopwordsFile = new File(enStopwordsFilename);
            fileReader = new FileReader(stopwordsFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.replaceAll("\n", "");
                if (!enStopWords.contains(line)) enStopWords.add(line);
            }    
            fileReader.close();
          
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Stopwords.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Stopwords.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    private static void initGrStopwords() {
        FileReader fileReader = null;
        try {
            File stopwordsFile = new File(grStopwordsFilename);
            fileReader = new FileReader(stopwordsFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.replaceAll("\n", "");
                if (!grStopWords.contains(line)) grStopWords.add(line);
            }    
            fileReader.close();
          
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Stopwords.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Stopwords.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    private static boolean isEnStopWord(String word) {
        if (word==null || !initialized) return false;
        else return enStopWords.contains(word);
    }
    
    private static boolean isGrStopWord(String word) {
        if (word==null || !initialized) return false;
        else return grStopWords.contains(word);
    }
    
    public static boolean isStopWord(String word) {
        return (isEnStopWord(word) || isGrStopWord(word));
    }
    
}
