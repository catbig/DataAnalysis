package com.confident.analysis.lib;

import java.util.Date;

import com.univocity.parsers.conversions.DateConversion;

/**
 *
 * @author Catur Adi Nugroho
 */
public class ObjectToDate extends DateConversion {

    public ObjectToDate(String... dateFormats) {
        super(null, null, dateFormats);
    }

    @Override
    public Date fromString(String string) {
        return super.fromString(string);
    }
}
