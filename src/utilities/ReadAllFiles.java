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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kostas
 */
public class ReadAllFiles {
    private String folderName;
    private ArrayList<File> files;

    public ReadAllFiles(String folderName) {
        assert(folderName!=null);
        this.folderName = folderName;
        files = new ArrayList<>();
        File folder = new File(this.folderName);
        listFilesForFolder(folder);
    }
        
    
    private void listFilesForFolder(File folder) {
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
              //  System.out.println(fileEntry.getAbsolutePath());
                files.add(fileEntry);
            }
        }
    }

    public String getFolderName() {
        return folderName;
    }

    public ArrayList<File> getFiles() {
        return files;
    }
    
    //a method used just for testing
    public int getNumberOfLinesInFile(File file) {
        assert(file!=null);
        int lineNumber = 0;
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                ++lineNumber;
            }
            
            return lineNumber;
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReadAllFiles.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        } catch (IOException ex) {
            Logger.getLogger(ReadAllFiles.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }        
        
    }
    
    //a method used just for testing
    public void printLinesNumberForAllFiles() {
        int maxFiles = 1000;
        int lines = 0;
        int i = 0;
        if (this.files==null) return;
        for (File file: this.files) {
            ++i;
            if (i>maxFiles) break;
            lines = getNumberOfLinesInFile(file);
            System.out.println(i+")"+lines);
            lines = 0;            
        }
    }
    
    //a method used just for testing
    public int getSumOfLinesForAllFiles(int maxFiles) {
        boolean isLimited = true;
        if (maxFiles<0) isLimited = false;
        int lines = 0;
        int i = 0;
        if (this.files==null) return -1;
        for (File file: this.files) {
            ++i;
            if (i>maxFiles && isLimited) break;
            lines += getNumberOfLinesInFile(file);                 
        }
        
        return lines;
    }

}
