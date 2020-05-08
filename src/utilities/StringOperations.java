/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for strings
 * @author Kostas
 */
public class StringOperations {
    
    public static final int MAX_LENGTH = 30;
    public static final String noPunctOrWsPattern = "[^a-zA-Z0-9\\-]";
    
    public static final String[] badPatterns = {".*[0-9]+[^\\-][a-zA-Z]+.*", ".*[a-zA-Z]+[0-9]{2,}.*", ".*[0-9]{5,}.*", "0{2,}",               
        ".*((tt)|(cc)|(gg)|(gc)|(tc)|(tg)|(gt)|(cg)|(ct)|(ga)|(ca)){3,}.*"};
    
    public static final String[] badSequences = {"xx", "yy", "hhh", "uu", "aaa", "bbb", "ccc", "ddd", "eee", "fff", "ggg", "iii", "jjj", "kkk", "lll", "mmm", "nnn", "ooo", 
        "ppp", "qqq", "rrr", "sss", "ttt", "vvv", "www", "zzz", "bk", "bm", "gq", "gx", "jx", "kx", "lx", "mx", "nx", "qx", "zx", "xf"};
    
    /**
     * @pre The string must not be null
     * @post The string returned has no punctuation or whitespace characters
     * @param input
     * @return the string without punctuation or whitespace characters
     */
    public static String removePunctuationAndWhitespace(String input) {
        assert(input!=null);       
        
        String output = input.replaceAll(noPunctOrWsPattern, "");
        output = output.replaceAll("(\\-){2,}", "");
        if (!output.isEmpty() && output.charAt(0)=='-') {
            output = output.substring(1);
        }
        
        return output.toLowerCase().trim();
    }
    
    public static boolean isValidWord(String word) {       
        /*
        if (word==null) return false;
        if (word.isEmpty()) return false;
        if (word.equals("")) return false;
        if (word.length()>MAX_LENGTH) return false;
        if (word.length()<=1) return false;
        
        for (String bseq: badSequences) {
            if (word.contains(bseq)) {
                return false;
            }
        }
        
        for (String patstr: badPatterns) {
            Pattern p = Pattern.compile(patstr, Pattern.DOTALL);
            Matcher m = p.matcher(word);
            if (m.matches()) {                 
                return false;
            }
        }//end for
           */    
        
      //  System.out.println(word);
        return true;       
    }
    
}
