import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tome {

	static String readUrl(String http) {
		String content = "";
		try {
			URL url = new URL(http);
			URLConnection URLconnection = url.openConnection();
			URLconnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			URLconnection.setConnectTimeout(60000);
			URLconnection.setReadTimeout(60000);
			HttpURLConnection httpConnection = (HttpURLConnection) URLconnection;
			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream in = httpConnection.getInputStream();
				InputStreamReader isr = new InputStreamReader(in);
				BufferedReader bufr = new BufferedReader(isr);
				String str;
				while ((str = bufr.readLine()) != null) {
					content += str + "\n";
				}
				bufr.close();
			} else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
				return "";
			} else if (responseCode == HttpURLConnection.HTTP_UNAVAILABLE) {
				return null;
			} else {
				System.out.println("Error " + responseCode + " : " + url);
			}
		} catch (Exception e) {
			return null;
		}
		return content;
	}

	static void processUrl(Vector<String> name, Vector<String> flavor, String url) {
		String setPage = readUrl(url);
		Pattern pattern = Pattern.compile("<td class=\"ellipsis\"><a href=\"(/card/m19/[^\"]+)\">(.*?)</a></td>");
		Matcher matcher = pattern.matcher(setPage);

		while (matcher.find()) {
			name.add(matcher.group(2).replaceAll("&#39;", "'"));
			System.out.println(matcher.group(2));
			String cardPage = readUrl("https://scryfall.com" + matcher.group(1));
			Pattern p = Pattern.compile("<div class=\"card-text-flavor\">(.*?)</div>", Pattern.DOTALL);
			Matcher m = p.matcher(cardPage);
			if (m.find()) {
				p = Pattern.compile("<p>(.*?)</p>", Pattern.DOTALL);
				m = p.matcher(m.group(1));
				if (m.find()) {
					flavor.add(m.group(1).replaceAll("“", "\"").replaceAll("”", "\"").replaceAll("’", "'")
							.replaceAll("<br />", ""));
					System.out.println(m.group(1));
				} else {
					System.err.println("Error!");
					System.exit(0);
				}
			} else {
				flavor.add("");
			}
		}
	}

	public static void main(String[] args) {
		Vector<String> englishName = new Vector<>();
		Vector<String> chineseName = new Vector<>();
		Vector<String> englishFlavor = new Vector<>();
		Vector<String> chineseFlavor = new Vector<>();

		processUrl(englishName, englishFlavor, "https://scryfall.com/sets/m19?order=set&as=checklist");
		processUrl(chineseName, chineseFlavor, "https://scryfall.com/sets/m19/zhs?order=set&as=checklist");

		System.out.println("===========================");

		for (int i = 0; i < englishName.size(); i++) {
			System.out.println(englishName.get(i));
			System.out.println(chineseName.get(i));
			if (!englishFlavor.get(i).isEmpty()) {
				System.out.println(englishFlavor.get(i));
				System.out.println(chineseFlavor.get(i));
			}
			System.out.println();
		}
	}

}
