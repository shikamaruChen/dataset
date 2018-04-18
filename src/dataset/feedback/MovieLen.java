package dataset.feedback;

import static yifan.utils.IOUtils.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Maps;

import dataset.helper.ItemSet;

public class MovieLen {

	private String dir;
	private Map<Integer, Integer> items;
	private int numUser = 0;
	private int nnz = 0;

	public MovieLen(String dir) {
		this.dir = dir;
		items = Maps.newHashMap();
	}

	public void movie() throws IOException {
		BufferedReader reader = bufferReader(dir + "raw/links.csv");
		String line = reader.readLine();
		int i = 1;
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split(",");
			int item = Integer.parseInt(terms[0]);
			items.put(item, i++);
		}
		reader.close();
	}

	public void plot() throws IOException {
		ItemSet itemset = new ItemSet();
		itemset.initial();
		BufferedReader reader = bufferReader(dir + "plot_all.txt");
		String line;
		while ((line = reader.readLine()) != null)
			itemset.addProfile(line);
		reader.close();
		itemset.globalWeight();
		itemset.output_wordlist(dir + "corpus");
		itemset.output_local(dir + "feature.txt");
	}

	public void user() throws IOException {
		BufferedReader reader = bufferReader(dir + "raw/ratings.csv");
		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split(",");
			int user = Integer.parseInt(terms[0]);
			numUser = (numUser < user) ? user : numUser;
			float rating = Float.parseFloat(terms[2]);
			if (rating == 5.0)
				nnz++;
		}
		reader.close();
	}

	public void rating() throws IOException {
		BufferedReader reader = bufferReader(dir + "raw/ratings.csv");
		BufferedWriter writer = bufferWriter(dir + "rating.txt");
		writer.write("%%MatrixMarket matrix coordinate real general");
		writer.newLine();
		writer.write(String.format("%d %d %d", numUser, items.size(), nnz));
		writer.newLine();
		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split(",");
			int item = Integer.parseInt(terms[1]);
			if (!items.containsKey(item))
				continue;
			float rating = Float.parseFloat(terms[2]);
			if (rating < 5)
				continue;
			int user = Integer.parseInt(terms[0]);
			int i = items.get(item);
			writer.write(String.format("%d %d 1", user, i));
			writer.newLine();
		}
		reader.close();
		writer.close();
	}

	public void crawler() throws FileNotFoundException, IOException {
		BufferedReader reader = bufferReader(dir + "raw/link.left");
		PrintWriter writer = printWriter(dir + "plot_part3.txt");

		// BufferedWriter writer = bufferWriter(dir + "plot_part2.txt");
		String line;
		while ((line = reader.readLine()) != null) {
			String imdb = line.split(",")[1];
			String url = "http://www.imdb.com/title/tt" + imdb + "/plotsummary?ref_=tt_stry_pl";
			String text = crawler(url);
			writer.write(text);
			writer.write("\r\n");
			console(url);
		}
		reader.close();
		writer.close();
	}

	private String crawler(String url) throws IOException {
		Document doc = Jsoup.connect(url).timeout(500000).ignoreHttpErrors(true).get();
		Elements plot = doc.select("#plot-synopsis-content li");
		if (plot.size() == 0)
			return "";
		String text = plot.get(0).text();
		if (text.contains("we don't have a Synopsis"))
			text = "";
		plot = doc.select("li.ipl-zebra-list__item p");
		for (Element e : plot)
			text += e.text() + " ";
		return text;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MovieLen movielen = new MovieLen("/Users/chenyifan/jianguo/dataset/movie-len/ml-20m/");
		try {
//			movielen.movie();
//			movielen.user();
//			movielen.rating();
			movielen.plot();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
