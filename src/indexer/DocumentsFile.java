/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates the documents file in specified directory
 * @author Kostas
 */
public class DocumentsFile {   
   public static final String documentsFileName = "DocumentsFile.txt";
   public static final int REC_SZ = 3;
   private final String indexFolderName; 
   private RandomAccessFile documentsFile;     
   
   
   public DocumentsFile(String ifn, TreeMap<String, ArrayList<Long>> docFreqs) {
       this.indexFolderName = ifn;           
       
       File dfile = new File(indexFolderName+File.separator+documentsFileName);
       try {
           documentsFile = new RandomAccessFile(dfile, "rw");
       } catch (FileNotFoundException ex) {
           Logger.getLogger(DocumentsFile.class.getName()).log(Level.SEVERE, null, ex);
           System.exit(1);
       }
   }
 
   
   public long getCurrentPointer() {
       try {
           return this.documentsFile.getFilePointer();
       } catch (IOException ex) {
           Logger.getLogger(DocumentsFile.class.getName()).log(Level.SEVERE, null, ex);
           System.exit(1);
       }
       return -1;
   }
   
   public void closeFile() {
       try {
           this.documentsFile.close();
       } catch (IOException ex) {
           Logger.getLogger(DocumentsFile.class.getName()).log(Level.SEVERE, null, ex);
       }
   }
    
   public long writeEntry(String identifier, String path, Double norm) {
       long currptr = -1;
       
       try {
            currptr = documentsFile.getFilePointer();           
            documentsFile.writeBytes(identifier+"\t");
            documentsFile.writeBytes(path+"\t");                
            documentsFile.writeBytes(norm.toString()+"\r\n");                                 
       } catch (IOException ex) {
           Logger.getLogger(DocumentsFile.class.getName()).log(Level.SEVERE, null, ex);
           System.exit(1);
       }
       return currptr;
   }
   
}
