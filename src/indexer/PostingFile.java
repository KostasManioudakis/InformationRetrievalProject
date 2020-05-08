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
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Kostas
 */
public class PostingFile {
   public static final String postingFileName = "PostingFile.txt";
   public static final int REC_SZ = 3;
   private final String indexFolderName; 
   private RandomAccessFile postingFile;
   
   public PostingFile(String indxfname) throws FileNotFoundException {
       this.indexFolderName = indxfname;
       
       File postfile = new File(this.indexFolderName+File.separator+postingFileName);
       postingFile = new RandomAccessFile(postfile, "rw");
   }
   
   public void closeFile() throws IOException {
       this.postingFile.close();
   }
   
   public long getCurrentPointer() throws IOException {
       return this.postingFile.getFilePointer();
   }
   
   public long writeEntry(String docId, Double tf, TreeMap<String, ArrayList<Integer>> positions, Long pointerToDocsFile) throws IOException {
       long cptr = this.postingFile.getFilePointer();
       
       postingFile.writeBytes(docId);
       postingFile.writeBytes("\t");
       
       postingFile.writeBytes(tf.toString());
       postingFile.writeBytes("\t");
            
       int posSize = positions.size();
       int m = 0;
       for (Map.Entry<String, ArrayList<Integer>> entrySet : positions.entrySet()) {
            String tagname = entrySet.getKey();
            ArrayList<Integer> list = entrySet.getValue();
          
            postingFile.writeBytes(tagname+":");        

            int li = 0;
            for (Integer pos: list) {
                postingFile.writeBytes(pos.toString());
                ++li;
                if (li<list.size()) postingFile.writeBytes(",");
            }
            ++m;
            if (m<posSize) postingFile.writeBytes("&");
        }//end 3rd for
       
       postingFile.writeBytes("\t");
       postingFile.writeBytes(pointerToDocsFile.toString());
       postingFile.writeBytes("\r\n");
       
       return cptr;
   }
   
   public long writeEntryWithoutPositions(String docId, Double tf, Long pointerToDocsFile) throws IOException {
       long cptr = this.postingFile.getFilePointer();
       
       postingFile.writeBytes(docId);
       postingFile.writeBytes("\t");
       
       postingFile.writeBytes(tf.toString());       
            
       //postingFile.writeBytes("\tbody:1\t");
       postingFile.writeBytes("\t");
      
       postingFile.writeBytes(pointerToDocsFile.toString());
       postingFile.writeBytes("\r\n");
       
       return cptr;
   }
   
}
