package dataset.feedback;

import static yifan.utils.IOUtils.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

import org.json.JSONObject;

import com.google.common.collect.Maps;

public class Yelp {

	public void review(String dir) throws IOException {
		BufferedReader reader = bufferReader(dir + "review.json");
		BufferedWriter writer = bufferWriter(dir + "rating.txt");
		String line = reader.readLine();
		Map<String, Integer> userMap = Maps.newHashMap();
		Map<String, Integer> itemMap = Maps.newHashMap();
		int u = 0;
		int i = 0;
		while ((line = reader.readLine()) != null) {
			JSONObject json = new JSONObject(line);
			String user = json.getString("user_id");
			String item = json.getString("business_id");
			String text = json.getString("text");
			int rating = json.getInt("stars");
			if (!userMap.containsKey(user))
				userMap.put(user, u++);
			if (!itemMap.containsKey(item))
				itemMap.put(item, i++);
			int uId = userMap.get(user);
			int iId = itemMap.get(item);
			writer.write(String.format("%d %d %d", uId, iId, rating));
			writer.newLine();
		}
		reader.close();
		writer.close();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Yelp yelp = new Yelp();
		try {
			yelp.review("/Users/chenyifan/jianguo/dataset/yelp/dataset/");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
