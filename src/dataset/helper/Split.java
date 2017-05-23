package dataset.helper;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import structure.matrix.MatrixEntry;
import structure.matrix.SparseMatrix;

public class Split {

	private SparseMatrix mat;
	private String dir;

	public static void main(String[] args) {
		try {
			Split split = new Split("D:/dataset/filmtrust");
			split.leaveOneOut(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Split(String path) throws IOException {
		File file = new File(path);
		dir = file.getParent();
		mat = SparseMatrix.readMatrix(path);
	}
	
	private SparseMatrix[] leaveOneOut() throws Exception {
		int[] row_ptr = mat.getRowPointers();
		int[] col_idx = mat.getColumnIndices();
		Random random = new Random(System.currentTimeMillis());
		Table<Integer, Integer, Double> dataTable = HashBasedTable.create();
		Multimap<Integer, Integer> colMap = HashMultimap.create();
		for (int u = 0, um = mat.numRows(); u < um; u++) {
			int start = row_ptr[u], end = row_ptr[u + 1];
			int len = end - start;
			if (len <= 1)
				continue;
			int idx = random.nextInt(len) + start;
			int j = col_idx[idx];
			mat.get(u, j);
			dataTable.put(u, j, 1.0);
			colMap.put(j, u);
		}
		SparseMatrix test = new SparseMatrix(mat.numRows, mat.numColumns, dataTable, colMap);
		SparseMatrix train = getTrainFile(test);
		return new SparseMatrix[] { train, test };
	}

	public void leaveOneOut(int nfold) throws Exception {
		// TODO Auto-generated method stub
		// writeMatrix(feedback, "result");
		File LOO = new File(dir + "/LOO");
		if (!LOO.isDirectory())
			LOO.mkdir();
		for (int f = 1; f <= nfold; f++) {
			SparseMatrix[] datafold = leaveOneOut();
			SparseMatrix.writeMatrix(datafold[0], dir + "/LOO/train" + f);
			SparseMatrix.writeMatrix(datafold[1], dir + "/LOO/test" + f);
		}
	}

	public SparseMatrix getTrainFile(SparseMatrix test) {
		SparseMatrix trainMatrix = new SparseMatrix(mat);
		for (MatrixEntry entry : test) {
			int u = entry.row();
			int i = entry.column();
			trainMatrix.set(u, i, 0.0);
		}
		SparseMatrix.reshape(trainMatrix);
		return trainMatrix;
	}

}
