package yifan;

import static yifan.utils.IOUtils.*;

import java.io.BufferedReader;
import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			//GPA1("data/score");
			GPA2("data/score");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void GPA2(String file) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader reader = bufferReader(file);
		String line = reader.readLine();
		reader.close();
		double sum = 0;
		int n = 0;
		String[] scores = line.split("\t");
		for (String score : scores) {
			double gpa = 0;
			if (score.equals("A"))
				gpa = 4.0;
			if (score.equals("B+"))
				gpa = 3.3;
			if (score.equals("B"))
				gpa = 3.0;
			sum += gpa;
			System.out.print(gpa + "\t");
			n++;
		}
		console(sum / n);
	}

	private static void GPA1(String file) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader reader = bufferReader(file);
		String line;
		double sum = 0;
		int ln = 0;
		while ((line = reader.readLine()) != null) {
			double score = Double.parseDouble(line);
			double gpa = 0;
			if (score >= 93)
				gpa = 4.0;
			else if (score >= 90)
				gpa = 3.7;
			else if (score >= 87)
				gpa = 3.3;
			else if (score >= 83)
				gpa = 3.0;
			else if (score >= 80)
				gpa = 2.7;
			else if (score >= 77)
				gpa = 2.3;
			else if (score >= 73)
				gpa = 2.0;
			else if (score >= 70)
				gpa = 1.7;
			else if (score >= 67)
				gpa = 1.3;
			else if (score >= 65)
				gpa = 1.0;
			sum += gpa;
			System.out.print(gpa + "\t");
			ln++;
		}
		reader.close();
		console(sum);
		console(sum / ln);
	}

}
