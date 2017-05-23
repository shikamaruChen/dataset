package yifan;

import static yifan.utils.IOUtils.*;
import structure.matrix.SparseMatrix;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			SparseMatrix s = SparseMatrix.readMatrix("dataset/feature");
			SparseMatrix sub = s.selectColumn(new int[] { 0, 1, 2, 3 });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
