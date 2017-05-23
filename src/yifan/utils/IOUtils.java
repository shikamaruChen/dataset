package yifan.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class IOUtils {
	public static BufferedReader bufferReader(String file) throws FileNotFoundException, IOException {
		FileInputStream stream = new FileInputStream(new File(file));
		InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
		BufferedReader bufferedReader = new BufferedReader(reader);
		return bufferedReader;
	}
	public static BufferedWriter bufferWriter(String file) throws IOException {
		FileOutputStream stream = new FileOutputStream(new File(file));
		OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
		BufferedWriter writer2 = new BufferedWriter(writer);
		return writer2;
	}
	public static PrintWriter printWriter(String file) throws IOException{
		FileOutputStream stream = new FileOutputStream(new File(file));
		OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
		PrintWriter writer2 = new PrintWriter(writer);
		return writer2;
	}
//	public static void console(String s){
//		System.out.println(s);
//	}
//	public static void console(int i){
//		System.out.println(i);
//	}
	public static void console(Object o){
		System.out.println(o);
	}
}
