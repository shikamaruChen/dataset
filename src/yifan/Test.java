package yifan;

import static yifan.utils.IOUtils.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class Test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String path = "/Users/chenyifan/Desktop/SOGFS/code/hyper.txt";
		BufferedReader reader = bufferReader(path);
		path = "/Users/chenyifan/Desktop/SOGFS/code/run";
		BufferedWriter writer = bufferWriter(path);
		String line;
		String command = "matlab -nodisplay -nosplash -nodesktop -r \"run('dir','../dataset','maxiter',10,'gpu',1,'out','SOGFS_%d',%s); exit;\"";
		int i = 1;
		while ((line = reader.readLine()) != null) {
			writer.write(String.format(command, i, line));
			writer.newLine();
			i++;
		}
		reader.close();
		writer.close();
	}

}
