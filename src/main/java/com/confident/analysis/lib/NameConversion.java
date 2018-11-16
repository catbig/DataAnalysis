package com.confident.analysis.lib;

import java.util.regex.Pattern;

import com.univocity.parsers.conversions.ObjectConversion;

/**
 *
 * @author Catur Adi Nugroho
 */
public class NameConversion extends ObjectConversion<String> {
    @Override
    protected String fromString(String string) {
        return clean(string);
    }
    
    //^,\s*|[\[\]()]
    private static final Pattern ESCAPE = Pattern.compile("^,\\s*|[\\[\\]()]");
    
    //(\sAND\s)?\s?(@|&amp;)\s?(&amp;\s)?|\sAND\s(AND\s)?
    private static final Pattern AND = Pattern.compile("(\\sAND\\s)?\\s?(@|&amp;)\\s?(&amp;\\s)?|\\sAND\\s(AND\\s)?");
    private String clean(String s) {
        s = ESCAPE.matcher(s).replaceAll("");
        s = AND.matcher(s).replaceAll(" & ");
        return s;
    }
}
