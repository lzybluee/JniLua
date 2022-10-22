package lu.cifer.mtgviewer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class Json {

	public HashMap<String, String> map = new HashMap<>();

	Json(String file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			StringBuffer buffer = new StringBuffer();
			String str = null;
			while ((str = reader.readLine()) != null) {
				buffer.append(str);
			}
			reader.close();

			JSONObject all = new JSONObject(buffer.toString());

			for (String k : all.keySet()) {
				System.out.println(k + " : " + all.getJSONObject(k).getString("name"));
				if (k.equals("UGL") || k.equals("UNH") || k.equals("UST")) {
					continue;
				}
				JSONArray cards = all.getJSONObject(k).getJSONArray("cards");
				for (int i = 0; i < cards.length(); i++) {
					JSONObject card = cards.getJSONObject(i);
					String name = card.getString("name");
					if (!map.containsKey(name)) {
						map.put(name, card.has("subtypes") ? card.get("subtypes").toString() : "");
					}
				}
			}
			System.out.println();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
