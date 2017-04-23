package dataset;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.IntCounter;
import static yifan.utils.IOUtils.*;

public class ItemSet {
	private List<Profile> profiles;
	private StanfordCoreNLP pipeline;
	private Counter<String> globalCounter;
	private double[][] sims;
	
	public int size(){
		return profiles.size();
	}

	public void initial() {
		// initial NLP
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos");
		pipeline = new StanfordCoreNLP(props);
		// other initializations
		profiles = Lists.newArrayList();
		globalCounter = new IntCounter<String>();
	}

	public void addProfile(String line) {
		Profile profile = new Profile(line, pipeline);
		profiles.add(profile);
	}

	public void globalWeight() {
		for (Profile profile : profiles)
			for (String word : profile.words())
				globalCounter.incrementCount(word);
		Set<String> removewords = Sets.newHashSet();
		Iterator<String> iterator = globalCounter.keySet().iterator();
		while (iterator.hasNext()) {
			String word = iterator.next();
			int count = (int) globalCounter.getCount(word);
			if (count < 5) {
				removewords.add(word);
				iterator.remove();
			}
		}
		for (Profile profile : profiles)
			profile.removeWords(removewords);
		console("get global counts finished!");
	}

	public void tfidfWeight() {
		for (Profile profile : profiles)
			profile.tfidf(globalCounter, profiles.size());
		console("settting the term weights finished!");
	}

	public void similarity() {
		int n = profiles.size();
		sims = new double[n][];
		for (int i = 0; i < n; i++) {
			sims[i] = new double[n];
			sims[i][i] = 1;
		}
		for (int i = 0; i < n; i++)
			for (int j = i + 1; j < n; j++)
				sims[i][j] = sims[j][i] = cosine(i, j);
		console("calculating similarities finished!");
	}

	public void output_wordlist(String out) throws IOException {
		BufferedWriter writer = bufferWriter(out);
		for (String word : globalCounter.keySet()) {
			writer.write(word);
			writer.newLine();
		}
		writer.close();
	}

	public void output_local(String out) throws IOException {
		BufferedWriter writer = bufferWriter(out);
		int nzeros = 0;
		for (Profile profile : profiles)
			nzeros += profile.words().size();
		writer.write("%%MatrixMarket matrix coordinate real general");
		writer.newLine();
		writer.write(String.format("%d %d %d", profiles.size(),
				globalCounter.size(), nzeros));
		writer.newLine();
		int wid;
		int pid = 1;
		for (Profile profile : profiles) {
			wid = 1;
			for (String word : globalCounter.keySet()) {
				int count = profile.getWeight(word);
				if (count > 0) {
					writer.write(String.format("%d %d %d", pid, wid, count));
					writer.newLine();
				}
				wid++;
			}
			pid++;
		}
		writer.close();
	}

	public void output_sim(String out) throws IOException {
		// write the similarity matrix
		BufferedWriter writer = bufferWriter(out + "/S");
		int n = sims.length;
		StringBuilder s;
		for (int i = 0; i < n; i++) {
			s = new StringBuilder();
			for (int j = 0; j < n; j++) {
				s.append(sims[i][j]);
				s.append(" ");
			}
			writer.write(s.toString());
			writer.newLine();
		}
		writer.close();

	}

	public double cosine(int i, int j) {
		Profile p1 = profiles.get(i);
		Profile p2 = profiles.get(j);
		return p1.cosine(p2);
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {

	}
}
