package dataset.helper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.IntCounter;
import edu.stanford.nlp.util.CoreMap;

public class Profile {

	private Counter<String> localCounter;
	private Map<String, Double> weights;
	private double norm;

	public Profile(String text, StanfordCoreNLP pipeline) {
		localWeight(text, pipeline);
	}

	public Set<String> words() {
		return localCounter.keySet();
	}

	public void localWeight(String text, StanfordCoreNLP pipeline) {
		tfWeight(text, pipeline);
	}

	private void tfWeight(String text, StanfordCoreNLP pipeline) {
		localCounter = new IntCounter<String>();
		Annotation doc = new Annotation(text);
		pipeline.annotate(doc);
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		Pattern pattern = Pattern.compile("(.*)(\\W|\\d)+(.*)");
		for (CoreMap sentence : sentences)
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String word = token.get(TextAnnotation.class);
				String pos = token.get(PartOfSpeechAnnotation.class);
				if (pos.startsWith("N") && word.length() > 1
						&& !pattern.matcher(word).matches())
					localCounter.incrementCount(word);
			}
	}

	public void removeWords(Set<String> words) {
		for (String word : words)
			localCounter.remove(word);
	}

	public void tfidf(Counter<String> globalCounter, int ndoc) {
		weights = Maps.newHashMap();
		norm = 0;
		for (String word : localCounter.keySet()) {
			double tf = 1 + Math.log(localCounter.getCount(word));
			double idf = Math.log(ndoc * 1.0 / globalCounter.getCount(word));
			double w = tf * idf;
			norm += w * w;
			weights.put(word, w);
		}
		norm = Math.sqrt(norm);
	}

	public double cosine(Profile p) {
		if (norm * p.norm == 0)
			return 0;
		double cos = 0;
		for (String word : localCounter.keySet())
			if (p.localCounter.containsKey(word))
				cos += weights.get(word) * p.weights.get(word);
		return cos / (norm * p.norm);
	}

	public boolean containWord(String word) {
		return localCounter.containsKey(word);
	}

	public int getWeight(String word) {
		int count = 0;
		if (localCounter.containsKey(word))
			count = (int) localCounter.getCount(word);
		return count;
	}

	public String vectorized(Counter<String> globalCounter) {
		StringBuilder s = new StringBuilder();
		for (String word : globalCounter.keySet()) {
			if (weights.containsKey(word))
				s.append(weights.get(word));
			else
				s.append(0);
			s.append(" ");
		}
		return s.toString();
	}
}
