package dataset;

import java.io.File;
import java.util.Random;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import structure.matrix.MatrixEntry;
import structure.matrix.SparseMatrix;

public class Split {


	private SparseMatrix mat;
	private SparseMatrix[] tests;

	public void leaveOneOut(String dir) throws Exception {
		// TODO Auto-generated method stub
		mat = SparseMatrix.readMatrix(dir + "rating");
		// writeMatrix(feedback, "result");
		File LOO = new File(dir + "LOO");
		if (!LOO.isDirectory())
			LOO.mkdir();
		int fold = 5;
		looSplitFolds(fold);
		for (int f = 1; f <= fold; f++) {
			SparseMatrix[] datafold = looGetKthFold(f);
			SparseMatrix.writeMatrix(datafold[0], dir + "LOO/train" + f);
			SparseMatrix.writeMatrix(datafold[1], dir + "LOO/test" + f);
		}
	}

	public void looSplitFolds(int kfold) {
		assert kfold > 0;
		tests = new SparseMatrix[kfold];
		int[] row_ptr = mat.getRowPointers();
		int[] col_idx = mat.getColumnIndices();
		Random random = new Random(System.currentTimeMillis());

		for (int f = 0; f < kfold; f++) {
			Table<Integer, Integer, Double> dataTable = HashBasedTable.create();
			Multimap<Integer, Integer> colMap = HashMultimap.create();
			for (int u = 0, um = mat.numRows(); u < um; u++) {
				int start = row_ptr[u], end = row_ptr[u + 1];
				int len = end - start;
				if (len <= 1)
					continue;
				int idx = random.nextInt(len) + start;
				int j = col_idx[idx];
				dataTable.put(u, j, 1.0);
				colMap.put(j, u);
			}
			tests[f] = new SparseMatrix(mat.numRows, mat.numColumns, dataTable, colMap);
		}
	}

	public SparseMatrix[] looGetKthFold(int fold) {
		SparseMatrix trainMatrix = new SparseMatrix(mat);
		SparseMatrix testMatrix = tests[fold - 1];
		for (MatrixEntry entry : testMatrix) {
			int u = entry.row();
			int i = entry.column();
			trainMatrix.set(u, i, 0.0);
		}
		SparseMatrix.reshape(trainMatrix);
		return new SparseMatrix[] { trainMatrix, testMatrix };
	}

}
