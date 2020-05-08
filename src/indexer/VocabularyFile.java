/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kostas
 */
public class VocabularyFile {
    public static final String indexFolderName = "CollectionIndex";
    public static final String vocabularyFileName = "VocabularyFile.txt";  
    public static final int REC_SZ = 2;
    
    final int MAX_FILES;
    private File collectionfolder;
    private int filesProcessed;
    private final int totalFilesProcessed; 
    private TreeMap<String, ArrayList<Long>> vocMap; //<word, pointer list to postings>  
    private long currDocPointer;
    private DocumentsFile docsFile;
    private PostingFile postingFile;
    

    public VocabularyFile(String collectionFolderName, int maxfiles) {
    	
        this.MAX_FILES = maxfiles;
        vocMap = new TreeMap<>();            
        collectionfolder = new File(collectionFolderName);   
        
        this.filesProcessed = 0;
        findDocumentFrequencies(collectionfolder);
        //System.out.println("processed files = "+filesProcessed);
        this.totalFilesProcessed = this.filesProcessed;
        createDocsNumberFile(this.totalFilesProcessed);
        
        this.currDocPointer = 0;
        
        createIndexFolder();
        try {
            this.postingFile = new PostingFile(indexFolderName);           
            filesProcessed = 0;
            
            createDocumentsAndPostingFiles();
            
            createVocabularyFile();
            
            this.postingFile.closeFile();
            this.docsFile.closeFile();
        } catch (IOException ex) {
            Logger.getLogger(VocabularyFile.class.getName()).log(Level.SEVERE, null, ex);
        } 
       
    }
    
    private void createDocsNumberFile(int docsNumber) {
        try {
            FileWriter fileWriter = new FileWriter("max_files_no.txt", false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(String.valueOf(docsNumber));
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void createIndexFolder() {
        File dir = new File(this.indexFolderName);
        if (dir.exists()) {
            for(File file: dir.listFiles()) {
                file.delete();
            }
        }
        else dir.mkdir();
        
    }

    public int getFilesProcessed() {
        return filesProcessed;
    }
    
    public int getNumberOfWords() {
        return vocMap.size();
    }
     
    
    private void findDocumentFrequencies(File folder) {
        if (this.filesProcessed>=MAX_FILES) return;
        for (File fileEntry : folder.listFiles()) {
            if (this.filesProcessed>=MAX_FILES) return;
            
            if (fileEntry.isDirectory()) {
                findDocumentFrequencies(fileEntry);               
            } else {
                countWordsInFile(fileEntry);
                ++this.filesProcessed;               
            }            
        }
    }

    private void countWordsInFile(File fileEntry) {
        if (fileEntry==null) return;
        String filepath = fileEntry.getAbsolutePath();
        try {
            Document doc = Document.createInstanceFromFile(filepath);    
            doc.findWords();
            TreeMap<String, Integer> docTermFreqs = doc.getTermFrequencies();
            
            for (Map.Entry<String, Integer> entrySet : docTermFreqs.entrySet()) {
                String word = entrySet.getKey();
                
                if (vocMap.containsKey(word)) {
                    ArrayList<Long> list = vocMap.get(word);
                    list.add(new Long(-1));//temp value                    
                }
                else {
                    if (!word.isEmpty() && !word.equals("")) {
                        ArrayList<Long> newlist = new ArrayList<>();
                        newlist.add(new Long(-1)); //temp value
                        vocMap.put(word, newlist);
                    }
                }
                
            }
            
        } catch (IOException ex) {
            Logger.getLogger(VocabularyFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void printDocumentFrequencies() {
        int i = 0;
        for (Map.Entry<String, ArrayList<Long>> entrySet : vocMap.entrySet()) {
            String key = entrySet.getKey();
            ArrayList<Long> value = entrySet.getValue();
            ++i;
            System.out.println(i+")"+key+": "+value.size());
        }
    }

    
    private void createDocumentsAndPostingFiles() throws IOException {
        docsFile = new DocumentsFile(indexFolderName, vocMap);
        filesProcessed = 0;
        writeEntriesOfDocumentsAndPostingFile(collectionfolder);
    }
    
    private void writeEntriesOfDocumentsAndPostingFile(File folder) throws IOException {
        if (this.filesProcessed>=MAX_FILES) return;
        for (File fileEntry : folder.listFiles()) {
            if (this.filesProcessed>=MAX_FILES) return;
            
            if (fileEntry.isDirectory()) {
                writeEntriesOfDocumentsAndPostingFile(fileEntry);               
            } else {
                Document doc = Document.createInstanceFromFile(fileEntry.getAbsolutePath());
                //doc.findWordsAndPositions();
                doc.findWords();
                doc.calculateSquareNorm(vocMap, this.totalFilesProcessed);                    
                
                writeEntryToDocumentsFileForDocument(doc);
                writeEntriesToPostingFileForDocument(doc);
                
                ++this.filesProcessed;               
            }            
        }
    }
    
    private void writeEntryToDocumentsFileForDocument(Document doc) {
        String identifier = doc.getDocumenName().replaceAll(".nxml", "");
        Double norm = doc.getNorm();
        Long newptr = docsFile.writeEntry(identifier, doc.getDocumentPath(), norm);
        this.currDocPointer = newptr;
    }
    
     
    
    private void writeEntriesToPostingFileForDocument(Document doc) {
        if (doc==null) return;
        
        TreeMap<String, Integer> termFreqs = doc.getTermFrequencies();
        int maxfreq = doc.getMaxTermFrequency();
        String docID = doc.getDocumenName().replaceAll(".nxml", "");
        Long ptr2docs = currDocPointer;
        //TreeMap<String, TreeMap<String, ArrayList<Integer>>> allpositions = doc.getPositions();
        
        for (Map.Entry<String, Integer> entrySet : termFreqs.entrySet()) {
            String word = entrySet.getKey();
            Integer frequency = entrySet.getValue();
            Double tf = 0.0;
            if (maxfreq!=0) {
                tf = ((double)frequency)/((double)maxfreq);
            }
            
            if (this.vocMap.containsKey(word)) {
                //TreeMap<String, ArrayList<Integer>> positions = allpositions.get(word);
                try {
                    long currptr = this.postingFile.writeEntryWithoutPositions(docID, tf, ptr2docs);
                    ArrayList<Long> list = vocMap.get(word);
                    list.remove(new Long(-1));
                    list.add(currptr);
                } catch (IOException ex) {
                    Logger.getLogger(VocabularyFile.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
    }
    
    private void createVocabularyFile() {
        File vocabularyFile = new File(indexFolderName+File.separator+vocabularyFileName);
        FileWriter writer;              
        
        try {
            writer = new FileWriter(vocabularyFile);
            
            for (Map.Entry<String, ArrayList<Long>> entrySet : vocMap.entrySet()) {
                String word = entrySet.getKey();
                ArrayList<Long> list = entrySet.getValue();
                
          /*      if (list.size()>this.MAX_FILES/2) {
                    System.out.println(word);
                }
                  */
                
                writer.append(word).append('\t');
                int i = 0;
                for (Long ptr: list) {
                    writer.append(ptr.toString());
                    ++i;
                    if (i<list.size()) writer.append(';');
                }
                writer.append('\r').append('\n');
            }
            
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(VocabularyFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    

    
}
