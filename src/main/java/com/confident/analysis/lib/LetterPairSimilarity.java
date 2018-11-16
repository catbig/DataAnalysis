package com.confident.analysis.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author Catur Adi Nugroho
 */
public class LetterPairSimilarity {

    /// <summary>
    /// Compares the two Strings based on letter pair matches
    /// </summary>
    /// <param name="str1"></param>
    /// <param name="str2"></param>
    /// <returns>The percentage match from 0.0 to 1.0 where 1.0 is
    /// 100%</returns>

    //^[0-9]+ L?LC
    private static final Pattern COMPANY_NUMBER = Pattern.compile("^[0-9]+ L?LC");

    /**
     *
     * @param str1
     * @param str2
     * @return
     */
    public double compareStrings(String str1, String str2) {
        if (str1.equals(str2)) {
            return 1;
        } else if (COMPANY_NUMBER.matcher(str1).find()) {
            return 0;
        }

        List<String> pairs1 = wordLetterPairs(str1);
        List<String> pairs2 = wordLetterPairs(str2);
        pairs1.equals(pairs2);
        int intersection = 0;
        int union = pairs1.size() + pairs2.size();

        for (int i = 0; i < pairs1.size(); i++) {
            for (int j = 0; j < pairs2.size(); j++) {
                if (pairs1.get(i).equals(pairs2.get(j))) {
                    intersection++;
                    pairs2.remove(j);// Must remove the match to prevent "GGGG"
                    // from appearing to match "GG" with
                    // 100% success
                    break;
                }
            }
        }

        return (2.0 * intersection) / union;
    }

    /// <summary>
    /// Gets all letter pairs for each
    /// individual word in the String
    /// </summary>
    /// <param name="str"></param>
    /// <returns></returns>
    private List<String> wordLetterPairs(String str) {

        // Tokenize the String and put the tokens/words into an array
        String[] words = str.split("\\s");

        List<String> allPairs = new ArrayList<String>(str.length() - 1);
        // For each word
        for (String word : words) {
            String s = word.trim();
            if (s != null && s.length() > 0) {
                // Find the pairs of characters
                String[] pairsInWord = letterPairs(s);
                allPairs.addAll(Arrays.asList(pairsInWord));
            }
        }
        return allPairs;
    }

    /// <summary>
    /// Generates an array containing every
    /// two consecutive letters in the input String
    /// </summary>
    /// <param name="str"></param>
    /// <returns></returns>
    private String[] letterPairs(String str) {
        int numPairs = str.length() - 1;
        String[] pairs = new String[numPairs];
        for (int i = 0; i < numPairs; i++) {
            pairs[i] = str.substring(i, i + 2);
        }
        return pairs;
    }

    /**
     *
     * @param name1
     * @param name2
     * @param input
     * @return
     */
    public Object[] compare2Names(String name1, String name2, Object input) {
        boolean splitExist1 = name1.indexOf(" & ") > 2;
        boolean splitExist2 = name2.indexOf(" & ") > 2;
        Object[] o = null;
        if (splitExist1 || splitExist2) {
            String[] split1 = name1.split(" & ");
            String[] split2 = name2.split(" & ");
            for (String string1 : split1) {
                for (String string2 : split2) {
                    double d = compareStrings(string1, string2);
                    if (d > Compute.THRESHOLD) {
                        o = new Object[1];
                        o[0] = input;
                        return o;
                    }
                }
            }
        }
        return o;
    }
}
