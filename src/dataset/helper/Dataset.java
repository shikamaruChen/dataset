package dataset.helper;

import java.io.File;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import edu.stanford.nlp.util.ArrayUtils;
import structure.matrix.MatrixEntry;
import structure.matrix.SparseMatrix;
import yifan.utils.FileIO;
import static yifan.utils.IOUtils.*;

public class Dataset {

	private SparseMatrix rating;
	private SparseMatrix feature;
	private String dir;

	public static void main(String[] args) {
		try {
			Dataset dataset = new Dataset("/Users/User/Desktop/yifan/dataset/nips");
			dataset.splitTTV(3, 6, 2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Dataset(String dir) throws IOException {
		this.dir = dir;
		rating = SparseMatrix.readMatrix(dir + "/rating");
		feature = SparseMatrix.readMatrix(dir + "/feature");
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
		File LOO = new File(dir + "/LOO");
		if (!LOO.isDirectory())
			LOO.mkdir();
		for (int f = 1; f <= nfold; f++) {
			SparseMatrix[] datafold = leaveOneOut();
			datafold[0].writeMatrix(dir + "/LOO/train" + f);
			datafold[1].writeMatrix(dir + "/LOO/test" + f);
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

	public void selectFeature(String selectfile) throws Exception {
		String line = FileIO.readAsString(selectfile);
		String[] terms = line.split(" ");
		List<Integer> list = Lists.newArrayList();
		for (int i = 0; i < terms.length; i++) {
			double v = Double.parseDouble(terms[i]);
			if (v > 0)
				list.add(i);
		}
		// console(list);
		Integer[] select = new Integer[list.size()];
		list.toArray(select);

		SparseMatrix sub = feature.selectColumn(ArrayUtils.toPrimitive(select));
		sub.writeMatrix(dir + "/subfeature");
	}

	public void splitTTV(int nfold, int train, int test) throws Exception {
		File splitDir = new File(dir + "/split");
		if (!splitDir.isDirectory())
			splitDir.mkdir();
		for (int f = 1; f <= nfold; f++) {
			SparseMatrix[] ms = splitTTV(train, test);
			ms[0].writeMatrix(dir + "/split/train" + f);
			ms[1].writeMatrix(dir + "/split/test" + f);
			ms[2].writeMatrix(dir + "/split/valid" + f);
		}
	}

	private SparseMatrix[] splitTTV(int train, int test) throws Exception {
		if (train + test > 10)
			return null;
		int[] fold = new int[10];
		for (int i = 0; i < train; i++)
			fold[i] = 0;
		int len = train + test;
		for (int i = train; i < len; i++)
			fold[i] = 1;
		for (int i = len; i < 10; i++)
			fold[i] = 2;
		List<Integer> assign = Lists.newArrayList();
		// int[] assign = new int[rating.size()];
		int i = 0;
		for (MatrixEntry entry : rating)
			assign.add(fold[i++ % 10]);
		Collections.shuffle(assign);
		Integer[] assignArray = new Integer[assign.size()];
		assign.toArray(assignArray);
		SparseMatrix[] ms = splitFold(3, ArrayUtils.toPrimitive(assignArray));
		return ms;
	}

	private SparseMatrix[] splitFold(int nfold, int[] assign) {
		int i = 0;
		int[] row_ptr = rating.getRowPointers();
		int[] col_idx = rating.getColumnIndices();
		SparseMatrix[] ms = new SparseMatrix[nfold];
		for (int k = 0; k < nfold; k++)
			ms[k] = new SparseMatrix(rating);
		for (int r = 0, um = rating.numRows(); r < um; r++) {
			int start = row_ptr[r], end = row_ptr[r + 1];
			int len = end - start;
			for (int c = 0; c < len; c++) {
				int fold = assign[i++];
				for (int k = 0; k < nfold; k++)
					if (fold != k)
						ms[k].set(r, col_idx[c + start], 0.0);
			}
		}
		for (int k = 0; k < nfold; k++)
			SparseMatrix.reshape(ms[k]);
		return ms;
	}

}
