package dataset.feedback;

import static yifan.utils.IOUtils.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import yifan.utils.sscanf.Sscanf;

public class Enron {

	public static final String ALL_MAILS = "E:/dataset/enron_mail/sent_results";

	public Map<String, Integer> address;
	public Map<Integer, Set<Integer>> sent;
	public Map<String, Integer> corpus;
	public Map<Integer, Map<Integer, Integer>> email;
	private StanfordCoreNLP pipeline;

	private int Ni;
	private int Nu;
	private int Nt;

	public Enron() {
		address = Maps.newHashMap();
		sent = Maps.newTreeMap();
		email = Maps.newTreeMap();
		corpus = Maps.newHashMap();
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos");
		pipeline = new StanfordCoreNLP(props);
	}

	public void output(String dir) throws IOException {
		String feature = dir + "feature";
		BufferedWriter writer = bufferWriter(feature);
		writer.write("%%MatrixMarket matrix coordinate real general");
		writer.newLine();
		int nnz = 0;
		for (Map<Integer, Integer> value : email.values())
			nnz += value.size();
		writer.write(String.format("%d %d %d", Ni, Nt, nnz));
		writer.newLine();
		for (Entry<Integer, Map<Integer, Integer>> entry : email.entrySet())
			for (Entry<Integer, Integer> local : entry.getValue().entrySet()) {
				writer.write(String.format("%d %d %d", entry.getKey() + 1,
						local.getKey() + 1, local.getValue()));
				writer.newLine();
			}
		writer.close();

		String rating = dir + "rating";
		writer = bufferWriter(rating);
		writer.write("%%MatrixMarket matrix coordinate real general");
		writer.newLine();
		nnz = 0;
		for (Set<Integer> value : sent.values())
			nnz += value.size();
		writer.write(String.format("%d %d %d", Nu, Ni, nnz));
		writer.newLine();
		for (Entry<Integer, Set<Integer>> entry : sent.entrySet())
			for (int id : entry.getValue()) {
				writer.write(String.format("%d %d 1", entry.getKey() + 1,
						id + 1));
				writer.newLine();
			}
		writer.close();
	}

	public void mailbox(String name) throws FileNotFoundException, IOException {
		BufferedReader reader = bufferReader(ALL_MAILS);
		String line;
		boolean find = false;
		Ni = 0;
		Nu = 0;
		Nt = 0;
		int ln = 0;
		while ((line = reader.readLine()) != null) {
			if (ln % 1000 == 0)
				console("line " + ln);
			ln++;
			if (line.isEmpty())
				continue;
			String[] terms = line.split("= ");
			if (terms[0].equals("filename")) {
				Object variables[] = { "" };
				Sscanf.scan2(terms[1], "./maildir/%s/sent", variables);
				String myname = (String) variables[0];
				if (myname.equals(name)) {
					find = true;
				} else if (find)
					break;
				else {
					for (int i = 0; i < 3; i++)
						reader.readLine();
					ln += 3;
				}
			}
			if (terms[0].equals("maillist")) {
				String maillist = terms[1].substring(1, terms[1].length() - 1);
				maillist = maillist.replaceAll("'", "");
				maillist = maillist.replaceAll(" ", "");
				String[] adds = maillist.split(",");

				for (String add : adds) {
					if (!address.containsKey(add))
						address.put(add, Nu++);
					int id = address.get(add);
					if (!sent.containsKey(id))
						sent.put(id, new TreeSet<Integer>());
					Set<Integer> mailset = sent.get(id);
						mailset.add(Ni);
				}
			}
			if (terms[0].equals("content")) {
				Map<Integer, Integer> feature = Maps.newTreeMap();
				Annotation doc = new Annotation(terms[1]);
				pipeline.annotate(doc);
				List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
				Pattern pattern = Pattern.compile("(.*)(\\W|\\d)+(.*)");
				for (CoreMap sentence : sentences)
					for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
						String word = token.get(TextAnnotation.class);
						// String pos = token.get(PartOfSpeechAnnotation.class);
						if (word.length() > 1
								&& !pattern.matcher(word).matches()) {
							if (!corpus.containsKey(word))
								corpus.put(word, Nt++);
							int id = corpus.get(word);
							if (!feature.containsKey(id))
								feature.put(id, 0);
							feature.put(id, feature.get(id) + 1);
						}
					}
				email.put(Ni++, feature);
			}
		}
		reader.close();
		console(String.format("Nu=%d,Ni=%d.Nt=%d", Nu, Ni, Nt));
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		// TODO Auto-generated method stub
		Enron enron = new Enron();
		enron.mailbox("bass-e");
		enron.output("E:/dataset/enron_mail/");
	}

}
