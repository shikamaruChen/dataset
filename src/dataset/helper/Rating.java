package dataset.helper;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import edu.stanford.nlp.util.ArrayUtils;
import structure.matrix.MatrixEntry;
import structure.matrix.SparseMatrix;

public class Rating {
	private SparseMatrix rating;
	private String dir;

	public Rating(String dir) throws IOException {
		this.dir = dir;
		rating = SparseMatrix.readMatrix(dir + "/rating.txt");
	}

	public void write() throws Exception {
		rating.writeMatrix(dir + "/subrating.txt");
	}

	public void filterUser(int threshold) throws Exception {
		List<Integer> list = Lists.newArrayList();
		int[] row = rating.getRowPointers();
		int m = rating.numRows;
		for (int i = 0; i < m; i++) {
			int nnz = row[i + 1] - row[i];
			if (nnz >= threshold)
				list.add(i);
		}
		Integer[] select = new Integer[list.size()];
		list.toArray(select);
		// select = ArrayUtils.toPrimitive(select);
		rating = rating.selectRow(ArrayUtils.toPrimitive(select));
	}

	public void filterItem(int threshold) throws Exception {
		List<Integer> list = Lists.newArrayList();
		int[] column = rating.getColPointers();
		int m = rating.numColumns;
		for (int i = 0; i < m; i++) {
			int nnz = column[i + 1] - column[i];
			if (nnz >= threshold)
				list.add(i);
		}
		Integer[] select = new Integer[list.size()];
		list.toArray(select);
		rating = rating.selectColumn(ArrayUtils.toPrimitive(select));
	}

	private SparseMatrix[] leaveOneOut() throws Exception {
		int[] row_ptr = rating.getRowPointers();
		int[] col_idx = rating.getColumnIndices();
		Random random = new Random(System.currentTimeMillis());
		Table<Integer, Integer, Double> dataTable = HashBasedTable.create();
		Multimap<Integer, Integer> colMap = HashMultimap.create();
		for (int u = 0, um = rating.numRows(); u < um; u++) {
			int start = row_ptr[u], end = row_ptr[u + 1];
			int len = end - start;
			if (len <= 1)
				continue;
			int idx = random.nextInt(len) + start;
			int j = col_idx[idx];
			rating.get(u, j);
			dataTable.put(u, j, 1.0);
			colMap.put(j, u);
		}
		SparseMatrix test = new SparseMatrix(rating.numRows, rating.numColumns, dataTable, colMap);
		SparseMatrix train = getTrainFile(test);
		return new SparseMatrix[] { train, test };
	}

	public void leaveOneOut(int nfold) throws Exception {
		// TODO Auto-generated method stub
		// writeMatrix(feedback, "result");
		File LOO = new File(dir + "/loo");
		if (!LOO.isDirectory())
			LOO.mkdir();
		for (int f = 1; f <= nfold; f++) {
			SparseMatrix[] datafold = leaveOneOut();
			datafold[0].writeMatrix(dir + "/loo/train" + f + ".txt");
			datafold[1].writeMatrix(dir + "/loo/test" + f + ".txt");
		}
	}

	private SparseMatrix getTrainFile(SparseMatrix test) {
		SparseMatrix trainMatrix = new SparseMatrix(rating);
		for (MatrixEntry entry : test) {
			int u = entry.row();
			int i = entry.column();
			trainMatrix.set(u, i, 0.0);
		}
		SparseMatrix.reshape(trainMatrix);
		return trainMatrix;
	}

	private SparseMatrix[] disjointSplit(int train, int valid, int test) {
		int total = train + valid + test;
		int[] fold = new int[total];
		for (int i = 0; i < train; i++)
			fold[i] = 0;
		int len = train + valid;
		for (int i = train; i < len; i++)
			fold[i] = 1;
		for (int i = len; i < total; i++)
			fold[i] = 2;
		List<Integer> assign = Lists.newArrayList();
		for (int c = 0; c < rating.numColumns; c++)
			assign.add(fold[c % total]);
		// console(assign.size());
		// console(rating.numColumns);
		Collections.shuffle(assign);
		Integer[] column = new Integer[rating.numColumns];
		assign.toArray(column);
		// console(Arrays.toString(column));
		SparseMatrix[] ms = rating.splitColumn(3, ArrayUtils.toPrimitive(column));
		return ms;
	}

	/**
	 * split dataset according to the proportion
	 * 
	 * @param nfold
	 *            number of folders
	 * @param train
	 * @param valid
	 * @param test
	 * @param disjoint
	 * @throws Exception
	 */
	public void split(int nfold, int train, int valid, int test, boolean disjoint) throws Exception {
		String split = disjoint ? "cold" : "split";
		File splitDir = new File(dir + "/" + split);
		if (splitDir.exists()) {
			File[] files = splitDir.listFiles();
			for (File file : files)
				file.delete();
			splitDir.delete();
		}
		splitDir.mkdir();
		for (int f = 1; f <= nfold; f++) {
			SparseMatrix[] ms = disjoint ? disjointSplit(train, valid, test) : split(train, valid, test);
			ms[0].writeMatrix(dir + "/" + split + "/train" + f + ".txt");
			ms[1].writeMatrix(dir + "/" + split + "/test" + f + ".txt");
			ms[2].writeMatrix(dir + "/" + split + "/valid" + f + ".txt");
		}
	}

	// private SparseMatrix[] split(int train, int valid, int test) throws Exception
	// {
	// int total = train + test + valid;
	// int[] fold = new int[total];
	// for (int i = 0; i < train; i++)
	// fold[i] = 0;
	// int len = train + valid;
	// for (int i = train; i < len; i++)
	// fold[i] = 1;
	// for (int i = len; i < total; i++)
	// fold[i] = 2;
	// List<Integer> assign = Lists.newArrayList();
	// // int[] assign = new int[rating.size()];
	// int i = 0;
	//
	// for (MatrixEntry entry : rating)
	// assign.add(fold[i++ % total]);
	// Collections.shuffle(assign);
	// Integer[] assignArray = new Integer[assign.size()];
	// assign.toArray(assignArray);
	// SparseMatrix[] ms = splitFold(3, ArrayUtils.toPrimitive(assignArray));
	// return ms;
	// }

	public SparseMatrix[] split(int train, int valid, int test) {
		double total = train + valid + test;
		double validRate = valid / total;
		double testRate = test / total;

		int nfold = 3;
		int[] row_ptr = rating.getRowPointers();
		int[] col_idx = rating.getColumnIndices();
		SparseMatrix[] ms = new SparseMatrix[nfold];
		for (int k = 0; k < nfold; k++)
			ms[k] = new SparseMatrix(rating);
		for (int r = 0, um = rating.numRows(); r < um; r++) {
			int start = row_ptr[r], end = row_ptr[r + 1];
			int len = end - start;
			test = (int) (len * testRate);
			test = test == 0 ? 1 : test;
			valid = (int) (len * validRate);
			valid = valid == 0 ? 1 : valid;
			train = len - test - valid;
			List<Integer> assign = Lists.newArrayList();

			for (int i = 0; i < train; i++)
				assign.add(0);
			for (int i = 0; i < valid; i++)
				assign.add(1);
			for (int i = 0; i < test; i++)
				assign.add(2);
			Collections.shuffle(assign);
			for (int c = 0; c < len; c++) {
				for (int k = 0; k < nfold; k++)
					if (assign.get(c) != k)
						ms[k].set(r, col_idx[c + start], 0.0);
			}
		}
		for (int k = 0; k < nfold; k++)
			SparseMatrix.reshape(ms[k]);
		return ms;
	}

	public void randUser(int threshold) throws Exception {
		List<Integer> range = IntStream.range(0, rating.numRows).boxed().collect(Collectors.toList());
		Collections.shuffle(range);
		Integer[] select = new Integer[threshold];
		range.subList(0, threshold).toArray(select);
		rating = rating.selectRow(ArrayUtils.toPrimitive(select));
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Rating rating = new Rating("/Users/chenyifan/jianguo/dataset/sports");
		// rating.filterItem(20);
		// rating.filterUser(10);
		// rating.write();
		rating.split(1, 18, 1, 1, false);
	}

}
