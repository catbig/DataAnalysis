package com.confident.analysis.lib;

/**
 *
 * @author Catur Adi Nugroho
 * @since
 */
public class DatePointer implements Comparable<DatePointer>{
	public long id;
	public int rowId;
	public String d1;
	public String d2;
	public int compareTo(DatePointer d) {
		if (id < d.id) {
			return -1;
		} else if (id == d.id) {
			if (d1.compareTo(d.d1) == 0) {
				return d2.compareTo(d.d2);
			}
			return d1.compareTo(d.d1);
		}
		return 1;
	}
}
