package com.confident.analysis.lib;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Catur Adi Nugroho
 * @since
 */
public class ResultPointer {
	public ResultPointer(ResultPointer rp) {
		this.id = rp.id;
		this.isOverlap = rp.isOverlap;
	}
	public ResultPointer() {
		// TODO Auto-generated constructor stub
	}
	private static final int INITIAL_SIZE = 100;
	public int id = 0;
	public boolean isOverlap = false;
	public List<DatePointer> lines = new ArrayList<DatePointer>(INITIAL_SIZE);
}
