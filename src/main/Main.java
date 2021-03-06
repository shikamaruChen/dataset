package main;

import java.io.FileNotFoundException;
import java.io.IOException;

import structure.graph.UGraph;
import structure.matrix.SparseMatrix;

public class Main {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		// UGraph graph = new UGraph();
		// graph.read("/Users/User/Desktop/yifan/dataset/ppi/origin_4952");
		// // graph.read("graph");
		// UGraph subgraph = graph.subgraph(1000, 5000);
		// subgraph.write("/Users/User/Desktop/yifan/dataset/ppi/ppi_4952");
		String dir = "/Users/chenyifan/jianguo/dataset/nips/";
		SparseMatrix matrix = SparseMatrix.readMatrix(dir + "rating");
		try {
			matrix.writeMatrixCSR(dir + "rating.csr");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
