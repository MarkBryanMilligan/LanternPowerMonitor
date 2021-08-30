package com.lanternsoftware.util.csv;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;

public abstract class CSVWriter {
	public static void writeCSV(CSV _csv, String _file) {
		ResourceLoader.writeFile(_file, toString(_csv));
	}

	public static String toString(CSV _csv) {
		StringBuilder out = new StringBuilder("\uFEFF");
		if (CollectionUtils.isNotEmpty(_csv.getHeaders())) {
			out.append(CollectionUtils.transformToCommaSeparated(_csv.getHeaders(), _h -> "\"" + _h + "\""));
			out.append("\r\n");
		}
		for (int r = 0; r < _csv.rows; r++) {
			for (int c = 0; c < _csv.getColumns(); c++) {
				if (c > 0)
					out.append(",");
				out.append(_csv.cell(r, c));
			}
			out.append("\r\n");
		}
		return out.toString();
	}

	public static byte[] toByteArray(CSV _csv) {
		return NullUtils.toByteArray(toString(_csv));
	}
}
