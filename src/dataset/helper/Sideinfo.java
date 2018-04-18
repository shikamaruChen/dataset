package dataset.helper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.Lists;

import edu.stanford.nlp.util.ArrayUtils;

import structure.matrix.SparseMatrix;
import yifan.utils.FileIO;

public class Sideinfo {

	private SparseMatrix rating;
	private SparseMatrix feature;
	private String dir;

	public static void main(String[] args) {
		try {
			Sideinfo dataset = new Sideinfo("/Users/chenyifan/jianguo/dataset/sports");
			 dataset.filterItem(20);
			 dataset.filterUser(10);
			 dataset.write();
			// dataset.leaveOneOut(5);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Sideinfo(String dir) throws IOException {
		this.dir = dir;
		rating = SparseMatrix.readMatrix(dir + "/raw/rating.txt");
		feature = SparseMatrix.readMatrix(dir + "/raw/feature.txt");
	}

	public void write() throws Exception {
		rating.writeMatrix(dir + "/rating.txt");
		feature.writeMatrix(dir + "/feature.txt");
	}

	public void filterFeature(int threshold) throws Exception {
		List<Integer> list = Lists.newArrayList();
		int[] column = feature.getColPointers();
		int m = feature.numColumns;
		for (int i = 0; i < m; i++) {
			int nnz = column[i + 1] - column[i];
			if (nnz >= threshold)
				list.add(i);
		}
		Integer[] select = new Integer[list.size()];
		list.toArray(select);
		feature = feature.selectColumn(ArrayUtils.toPrimitive(select));
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
		SparseMatrix sub = rating.selectRow(ArrayUtils.toPrimitive(select));
		rating = sub;
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
		feature = feature.selectRow(ArrayUtils.toPrimitive(select));
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

}
