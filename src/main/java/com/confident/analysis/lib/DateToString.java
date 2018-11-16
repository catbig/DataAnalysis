package com.confident.analysis.lib;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.univocity.parsers.conversions.ObjectConversion;

/**
 *
 * @author Catur Adi Nugroho
 */
public class DateToString extends ObjectConversion<String> {

	private static ObjectToDate od;

    public DateToString(String... dateFormats) {
        od = new ObjectToDate(dateFormats);
    }

    @Override
    public String fromString(String string) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date d = od.fromString(string);
        return dateFormat.format(d);
    }
}
