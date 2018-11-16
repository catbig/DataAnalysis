package com.confident.analysis.lib;

import com.univocity.parsers.conversions.ObjectConversion;

/**
 *
 * @author Catur Adi Nugroho
 */
public class ResultConversion extends ObjectConversion<String> {
    @Override
    protected String fromString(String string) {
        return string.replaceAll("\\|", " ");
    }

}
