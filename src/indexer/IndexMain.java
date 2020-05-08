/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import java.util.Scanner;
import mitos.stemmer.Stemmer;
import utilities.Stopwords;

/**
 * @version 20.6
 * @author Kostas
 */
public class IndexMain {
    private static final int MAXFILES = 107871;
    
    public static void main(String[] args) {
        System.out.println("Enter full path of the folder you want to create the index for:");
        Scanner in = new Scanner(System.in);
        String folderpath = in.nextLine();
        System.out.println("Enter maximum files to be processed");
        int maxfiles = in.nextInt();
        if (maxfiles>MAXFILES) {
            maxfiles = MAXFILES;
            System.out.println("Too many files. Only "+MAXFILES+" files will be processed");
        }
        System.out.println("Please wait...");
        
        long start = System.currentTimeMillis() / 1000;
        Stemmer.Initialize();
        Stopwords.initStopwords();
        
        VocabularyFile voc = new VocabularyFile(folderpath, maxfiles);
        
        long end = System.currentTimeMillis() / 1000;
        int numOfWords = voc.getNumberOfWords();
        int filesProcessed = voc.getFilesProcessed();
        System.out.println("Files processed: "+filesProcessed);
        System.out.println("Words found: "+numOfWords);
        System.out.println("Elapsed time: " + (end - start) + " seconds");
        in.close();
    }
}
