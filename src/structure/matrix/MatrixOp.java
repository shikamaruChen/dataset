package structure.matrix;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

public class MatrixOp {
	public static SparseMatrix selectRow(SparseMatrix m, int[] row) {
		int[] row_ptr = m.getRowPointers();
		int[] col_idx = m.getColumnIndices();
		double[] data = m.getData();
		Table<Integer, Integer, Double> dataTable = HashBasedTable.create();
		Multimap<Integer, Integer> colMap = HashMultimap.create();
		int s = 0;
		for (int r : row) {
			int start = row_ptr[r], end = row_ptr[r + 1];
			int len = end - start;
			for (int c = 0; c < len; c++) {
				int j = start + c;
				dataTable.put(s, col_idx[j], data[j]);
				colMap.put(col_idx[j], s);
			}
			s++;
		}
		SparseMatrix sub = new SparseMatrix(s, m.numColumns, dataTable, colMap);
		return sub;
	}
}
