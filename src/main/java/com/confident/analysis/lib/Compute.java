package com.confident.analysis.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import com.confident.analysis.Form;

/**
 *
 * @author Catur Adi Nugroho
 * @since 20160327
 */
public class Compute extends Thread {

    public static double THRESHOLD = 0.79;
    public static List<Object[]> rows;
    public static List<Object[]> dateRow;
    private static final int INITIAL_SIZE = 100000;
    public static int process = 0;
    public boolean isProcess = false;
    public boolean isOverlap = false;
    public int id, start, end;
    public List<ResultPointer> lines = new ArrayList<ResultPointer>(INITIAL_SIZE);
    public List<ResultPointer> overlapLines = new ArrayList<ResultPointer>(10);

    public void run() {
        long time1 = System.currentTimeMillis();
        Form.log(getName() + " start process data");
        compute();
        mergeDate(lines);
        mergeDate(overlapLines);
        long time2 = System.currentTimeMillis();
        Form.log(getName() + " finish process data");
        Form.log(getName() + " execution time: " + (time2 - time1) + " ms");
    }

    private void mergeDate(List<ResultPointer> lines) {
        int a = id == 0 ? 0 : 1;
        for (int i = a; i < lines.size(); i++) {
            ResultPointer rp = lines.get(i);
            ResultPointer newRp = new ResultPointer(rp);
            int x = 0;
            boolean isContinue = false;
            boolean isSet = false;
            Collections.sort(rp.lines);
            for (int k = 0; k < rp.lines.size() - 1; k++) {
                DatePointer dp1 = rp.lines.get(k);
                DatePointer dp2 = rp.lines.get(k + 1);
                if (dp1.id == dp2.id && (dp1.d2.equals(dp2.d1) || (dp1.d1.equals(dp2.d1) && dp1.d2.equals(dp2.d2)))) {
                    if (!isContinue) {
                        x = k;
                        isContinue = true;
                        isSet = true;
                    }
                } else {
                    dp2 = rp.lines.get(x);
                    x = k + 1;
                    if (isContinue) {
                        Object[] o = rows.get(dp2.rowId);
                        String s = (String) o[1];
                        s = s.replaceAll(dp2.d2, dp1.d2);
                        o[1] = s;
                        rows.set(dp2.rowId, o);
                        dp2.d2 = dp1.d2;
                    }
                    newRp.lines.add(dp2);
                    isContinue = false;
                }
            }
            if (isSet) {
                DatePointer dp1 = rp.lines.get(x);
                if (isContinue) {
                	DatePointer dp2 = rp.lines.get(rp.lines.size() - 1);
                    Object[] o = rows.get(dp1.rowId);
                    String s = (String) o[1];
                    s = s.replaceAll(dp1.d2, dp2.d2);
                    o[1] = s;
                    rows.set(dp1.rowId, o);
                    dp1.d2 = dp2.d2;
                }
                newRp.lines.add(dp1);
                lines.set(i, newRp);
            }
        }
    }

    /**
     *
     * @param file
     */
    private void compute() {
        int j = start;
        int n = rows.size();
        int m = 0;
        int status = 0;

        // for add / remove element use LinkedList O(n) instead of ArrayList O(n^2)
        List<Integer> skip = new LinkedList<Integer>();

        ResultPointer rp = new ResultPointer();
        DatePointer dp1 = new DatePointer();
        Object[] o1 = dateRow.get(start);
        dp1.rowId = start;
        dp1.id = (Long) o1[0];
        dp1.d1 = (String) o1[1];
        dp1.d2 = (String) o1[2];

        rp.id = m++;
        rp.lines.add(dp1);
//        for (int i = 118360; i < 118400; i++) {
        for (int i = start; i < end - 1; i++) {
            if (status > 9999) {
                process = process + 10000;
                Form.log(getName() + " processed line: " + process);
                status = 0;
            }
            status++;
            if (skip.size() > 0 && skip.remove(new Integer(i))) {
                continue;
            }
            String name1 = (String) rows.get(j)[0];
            String name2 = (String) rows.get(i + 1)[0];
            LetterPairSimilarity lps = new LetterPairSimilarity();
            double d = lps.compareStrings(name1, name2);
            if (d > THRESHOLD) {
                DatePointer dp = new DatePointer();
                Object[] o = dateRow.get(i + 1);
                dp.rowId = i + 1;
                dp.id = (Long) o[0];
                dp.d1 = (String) o[1];
                dp.d2 = (String) o[2];

                rp.lines.add(dp);
                continue;
            }

            //forward check
            if (i < n - 1) {
                int idx = name1.indexOf(" ", 2);
                String nickName = name1;
                boolean i2 = false;
                if (idx > 2) {
                    nickName = name1.substring(0, idx);
                }

                // find at start string
                Pattern nickPattern = Pattern.compile("^" + nickName);
                for (int k = i + 2; k < n - 1; k++) {
                    if (skip.size() > 0 && skip.contains(k)) {
                        if (i + 2 == k) {
                            i2 = true;
                        }
                        continue;
                    }
                    String name3 = (String) rows.get(k)[0];
                    if (!nickPattern.matcher(name3).find()) {
                        break;
                    }
                    d = lps.compareStrings(name1, name3);
                    if (d > THRESHOLD) {
                        if (i + 2 == k) {
                            i2 = true;
                        }
                        DatePointer dp = new DatePointer();
                        Object[] o = dateRow.get(k);
                        dp.rowId = k;
                        dp.id = (Long) o[0];
                        dp.d1 = (String) o[1];
                        dp.d2 = (String) o[2];

                        rp.lines.add(dp);
                        skip.add(k);
                        if (k > end) {
                            rp.isOverlap = true;
                        }
                    }
                }
                if (i2) {
                    continue;
                }
            }
            if (rp.isOverlap) {
                isOverlap = true;
                overlapLines.add(rp);
            } else {
                lines.add(rp);
            }

            j = i + 1;
            // set new pointer
            DatePointer dp = new DatePointer();
            Object[] o = dateRow.get(j);
            dp.rowId = j;
            dp.id = (Long) o[0];
            dp.d1 = (String) o[1];
            dp.d2 = (String) o[2];

            rp = new ResultPointer();
            rp.id = m++;
            rp.lines.add(dp);
        }
    }
}
