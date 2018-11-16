package com.confident.analysis;

import static java.lang.Thread.sleep;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.confident.analysis.lib.Compute;
import com.confident.analysis.lib.DatePointer;
import com.confident.analysis.lib.DateToString;
import com.confident.analysis.lib.FileNameConversion;
import com.confident.analysis.lib.Finder;
import com.confident.analysis.lib.LetterPairSimilarity;
import com.confident.analysis.lib.NameConversion;
import com.confident.analysis.lib.ResultConversion;
import com.confident.analysis.lib.ResultPointer;
import com.google.code.externalsorting.ExternalSort;
import com.univocity.parsers.common.processor.ObjectRowListProcessor;
import com.univocity.parsers.conversions.Conversions;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

/**
 *
 * @author Catur Adi Nugroho
 */
public class Loader {

    private static final String CLEAN_FILENAME = "\\clean data.txt";
    private static final String SORT_FILENAME = "\\sort data.txt";
    private static final String RESULT_FILENAME = "\\result.txt";
    private static final char FIELD_DELIMITER = '|';
    public static final int INITIAL_SIZE = 100000;
    public static int N_THREAD = 1;

    /**
     *
     */
    public static final char NAME_DELIMITER = (char) 31;

    /**
     *
     * @param file
     */
    public void cleanUp(File file) {
        String s1 = "AA ALPINE STORAGE PROVO LLC";
        String s2 = "ADAMS MICHAEL DAVID & CYNTHIA LYNN";
        LetterPairSimilarity lps = new LetterPairSimilarity();
        double d = lps.compareStrings(s1, s2);
        log("LetterPairSimilarity: " + d);
        if (d != 2) {
            //            return;
        }
        log("===================================================");
        long time1 = System.currentTimeMillis();
        List<Object[]> rows = cleanName(file);
        rows = splitDuplicateName(rows);
        File out = new File(file.getParent() + CLEAN_FILENAME);
        File out2 = new File(file.getParent() + SORT_FILENAME);
        long time2 = writeToFile(rows, FIELD_DELIMITER, out.getAbsolutePath());
        try {
            time2 = ExternalSort.sort(out, out2);
        } catch (IOException ex) {
        }
        log("Total execution time for clean up: " + (time2 - time1) + " ms");
        //======================================================================
    }

    /**
     *
     * @param file
     */
    public void process(File file) {
        log("===================================================");
        long time1 = System.currentTimeMillis();
        List<Object[]> rows = read(file, NAME_DELIMITER);
        List<Object[]> dateRow = read(file, FIELD_DELIMITER);
        rows = compute(rows, dateRow);
        long time2 = writeToFile(rows, NAME_DELIMITER, file.getParent() + RESULT_FILENAME);
        log("Total process execution time: " + (time2 - time1) + " ms");
        //======================================================================        
    }

    /**
     *
     * @param dateRow
     * @param file
     */
    private List<Object[]> compute(List<Object[]> rows, List<Object[]> dateRow) {
        long time1 = System.currentTimeMillis();
        long time2 = time1;
        log("Start process data");
        int n = rows.size();
        int pace = n % N_THREAD == 0 ? n / N_THREAD : n / (N_THREAD - 1);
        List<Object[]> results = new ArrayList<Object[]>(rows.size());
        Compute[] computeThread = new Compute[N_THREAD];
        Compute.rows = rows;
        Compute.dateRow = dateRow;
        Compute.process = 0;
        for (int i = 0, j = 0; i < N_THREAD; i++) {
            computeThread[i] = new Compute();
            computeThread[i].setName("Thread" + i);
            computeThread[i].id = i;
            computeThread[i].start = j;
//            computeThread[i].start = 127875;
            j = j + pace;
            computeThread[i].end = j;
        }
        computeThread[N_THREAD - 1].end = n;
//        computeThread[N_THREAD - 1].end = 127900;
        for (int i = 0; i < N_THREAD; i++) {
            computeThread[i].start();
        }

        //build result list
        boolean isAllProcess = false;
        boolean isLog = false;
        int id = 1;
        while (!isAllProcess) {
            for (int i = 0; i < N_THREAD; i++) {
                Compute c = computeThread[i];
                if (c.isAlive() || c.isProcess) {
                    continue;
                }
                if (!isLog) {
                    time2 = System.currentTimeMillis();
                    log("Start build result data");
                    isLog = true;
                }
                c.isProcess = true;

                // Process all line one shot if only one thread or first thread
                int a = i == 0 ? 0 : 1;
                //process first group outside this loop
                for (int j = a; j < c.lines.size(); j++) {
                    ResultPointer rp = c.lines.get(j);
                    String head = id++ + ":";
                    c.lines.get(j).id = results.size();
                    Object[] h = {head};
                    results.add(h);
                    for (int k = 0; k < rp.lines.size(); k++) {
                        DatePointer dp = rp.lines.get(k);
                        Object[] o = {rows.get(dp.rowId)[1]};
                        results.add(o);
                    }

                }
            }

            // check all thread has been processed
            int p = 0;
            for (int i = 0; i < N_THREAD; i++) {
                Compute c = computeThread[i];
                if (c.isProcess) {
                    p++;
                }
            }
            if (p < N_THREAD) {
                try {
                    sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } else {
                isAllProcess = true;
            }
        }

        // compute overlaped group
        for (int i = 0; i < N_THREAD - 1; i++) {
            Compute c1 = computeThread[i];
            Compute c2 = computeThread[i + 1];
            if (c1.isOverlap) {
                for (int j = 0; j < c1.overlapLines.size(); j++) {
                    ResultPointer rp = c1.overlapLines.get(j);
                    String head = id++ + ":";
                    c1.overlapLines.get(j).id = results.size();
                    Object[] h = {head};
                    results.add(h);
                    for (int k = 0; k < rp.lines.size(); k++) {
                        DatePointer dp = rp.lines.get(k);
                        Object[] o = {rows.get(dp.rowId)[1]};
                        results.add(o);
                    }
                }
            } else {
                ResultPointer rp = c2.lines.get(0);
                String head = id++ + ":";
                c2.lines.get(0).id = results.size();
                Object[] h = {head};
                results.add(h);
                for (int k = 0; k < rp.lines.size(); k++) {
                    DatePointer dp = rp.lines.get(k);
                    Object[] o = {rows.get(dp.rowId)[1]};
                    results.add(o);
                }
            }
        }

        log("Finish build result data");
        log("Execution time: " + (time2 - time1) + " ms");
        return results;
    }

    private List<Object[]> cleanName(File file) {
        log("Start clean name");
        long start = System.currentTimeMillis();
        long time1 = start;
        // ObjectRowProcessor converts the parsed values and gives you the resulting row.
        ObjectRowListProcessor rowProcessor = new ObjectRowListProcessor();
        rowProcessor.convertIndexes(new FileNameConversion()).add(0);
        rowProcessor.convertIndexes(new NameConversion()).add(5);
        rowProcessor.convertIndexes(new DateToString("M/d/yyyy")).add(2, 3);
        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setDelimiter(FIELD_DELIMITER);
        settings.setHeaderExtractionEnabled(true);
        settings.setRowProcessor(rowProcessor);
        settings.selectIndexes(5, 0, 2, 3);
        CsvParser parser = new CsvParser(settings);
        parser.parse(file);
        List<Object[]> rows = rowProcessor.getRows();
        int n = rows.size();
        log("Line: " + n);
        long time2 = System.currentTimeMillis();
        log("Finish clean name");
        log("Execution time: " + (time2 - time1) + " ms");
        log("---------------------------------------------------");
        return rows;
    }

    private List<Object[]> splitDuplicateName(List<Object[]> rows) {
        long time1 = System.currentTimeMillis();
        log("Start split & duplicate name");
        int n = rows.size();
        int colSize = rows.get(0).length;
        List<Object[]> result = new ArrayList<Object[]>(n + 3 * INITIAL_SIZE);
        for (int i = 0; i < n; i++) {
//        for (int i = 69250; i < 69350; i++) {
            Finder f = new Finder();
            Object[] o = rows.get(i);
            String name = (String) o[0];
            if (Finder.isCompanyName(name)) {
                name = f.duplicateName(name);
                o[0] = name;
                result.add(o);
            } else {
                int iName = name.indexOf(" & ", 2);
                if (iName > 2) {
                    String[] split = name.split(" & ");
                    String nickName = "";
                    int iNick = name.indexOf(',', 2);
                    if (iNick < iName) {
                        nickName = split[0].substring(0, iNick).trim();
//                        try {
//                            nickName = split[0].substring(0, iNick).trim();
//                        } catch (Exception e) {
//                            System.out.println(i + ":" + name);
//                        }
                    }
                    Object[] a = Arrays.copyOf(o, colSize);
                    name = f.duplicateName(split[0].trim());
                    a[0] = name;
                    result.add(a);
                    for (int j = 1; j < split.length; j++) {
                        Finder f1 = new Finder();
                        String s = split[j].trim();
                        if (nickName.length() > 0) {
                            if (!s.equals(nickName)) {
                                s = s.replaceAll(nickName, "");
//                                try {
//                                    s = s.replaceAll(nickName, "");
//                                } catch (Exception e) {
//                                    System.out.println(i + ": " + nickName + "|" + name);
//                                }
                                s = nickName.concat(", " + s);
                            }
                        }
                        s = f1.duplicateName(s);
                        Object[] b = Arrays.copyOf(o, colSize);
                        b[0] = s;
                        result.add(b);
                    }
                } else {
                    name = f.duplicateName(name);
                    o[0] = name;
                    result.add(o);
                }
            }
        }
        log("Produced line: " + result.size());
        log("Finish split & duplicate name");
        long time2 = System.currentTimeMillis();
        log("Execution time: " + (time2 - time1) + " ms");
        log("---------------------------------------------------");
        return result;
    }

    private List<Object[]> read(File file, char delimiter) {
        log("Start reading data");
        long time1 = System.currentTimeMillis();
        file = new File(file.getParent() + SORT_FILENAME);
        // ObjectRowProcessor converts the parsed values and gives you the resulting row.
        ObjectRowListProcessor rowProcessor = new ObjectRowListProcessor();
        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setDelimiter(delimiter);
        if (delimiter == NAME_DELIMITER) {
            rowProcessor.convertIndexes(new ResultConversion()).add(1);
        } else {
            rowProcessor.convertIndexes(Conversions.toLong()).set(1);
            settings.selectIndexes(1, 2, 3);
            settings.setNullValue("");
        }
        settings.setRowProcessor(rowProcessor);
        CsvParser parser = new CsvParser(settings);
        parser.parse(file);
        List<Object[]> rows = rowProcessor.getRows();
        log("Line: " + rows.size());
        long time2 = System.currentTimeMillis();
        log("Finish reading data");
        log("Execution time: " + (time2 - time1) + " ms");
        log("---------------------------------------------------");
        return rows;
    }

    private long writeToFile(List<Object[]> rows, char delimiter, String ouputFile) {
        long time1 = System.currentTimeMillis();
        log("Start writing file");
        CsvWriterSettings writerSetting = new CsvWriterSettings();
        writerSetting.getFormat().setDelimiter(delimiter);
        File out = new File(ouputFile);
        CsvWriter writer = new CsvWriter(out, writerSetting);
        writer.writeRowsAndClose(rows);
        log("Finish writing file");
        long time2 = System.currentTimeMillis();
        log("Execution time: " + (time2 - time1) + " ms");
        log("---------------------------------------------------");
        return time2;
    }

    private void log(String s) {
//        System.out.println(s);
        Form.log(s);
    }
}
