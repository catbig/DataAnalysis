package com.confident.analysis.lib;

import com.univocity.parsers.conversions.ObjectConversion;

/**
 *
 * @author Catur Adi Nugroho
 */
public class FileNameConversion extends ObjectConversion<String> {
    @Override
    protected String fromString(String string) {
        String s = string.replaceAll("Extract_", "");
        s = s.replaceAll(".htm", "");
        return s;
    }

}
