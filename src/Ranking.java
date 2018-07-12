import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lu.cifer.mtgviewer.CardAnalyzer;
import lu.cifer.mtgviewer.CardAnalyzer.ReprintInfo;
import lu.cifer.mtgviewer.CardParser;

public class Ranking {

	static class CardRank {
		ReprintInfo reprint;
		float score;
	}

	static Vector<CardRank> ranks = new Vector<>();

	public static void getRank(String set) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("Rankings/" + set + ".txt"));
			String str = null;
			while ((str = reader.readLine()) != null) {
				Pattern p = Pattern.compile("name:\"(.*?)\".*?myrating:\"(.*?)\"");
				Matcher m = p.matcher(str);
				while (m.find()) {
					String name = m.group(1).replaceAll("_", " ");
					float rank = Float.parseFloat(m.group(2));
					// System.out.println(name + " -> " + rank);
					CardRank cr = new CardRank();
					boolean found = false;
					if (CardAnalyzer.get(name) == null) {
						name = name.replace("Sleeping Dragon", "\"Sleeping Dragon\"").replace("Seance", "Séance")
								.replace(" s ", "'s ").replace("Wastes (1)", "Wastes").replace("AEther", "Aether");
						if (CardAnalyzer.get(name) == null) {
							String[] words = name.split(" ");
							if (words.length == 2) {
								name = words[0] + " (" + words[0] + "/" + words[1] + ")";
							}
							if (words.length != 2 || CardAnalyzer.get(name) == null) {
								System.err.println("Can't find card " + name + " in " + set);
								System.exit(0);
							}
						}
					}
					for (ReprintInfo r : CardAnalyzer.get(name).reprints) {
						if (r.code.equals(set)) {
							found = true;
							cr.reprint = r;
							cr.score = rank;
							ranks.add(cr);
							break;
						}
					}
					if (!found) {
						System.err.println("Can't find reprint " + name + " in " + set);
						System.exit(0);
					}
				}
			}
			Collections.sort(ranks, new Comparator<CardRank>() {

				@Override
				public int compare(CardRank cr1, CardRank cr2) {
					if (cr1.score != cr2.score) {
						return (cr1.score - cr2.score) < 0 ? 1 : -1;
					}
					return cr1.reprint.multiverseid - cr2.reprint.multiverseid;
				}
			});
			for (int i = 0; i < ranks.size(); i++) {
				CardRank cr = ranks.get(i);
				ReprintInfo rp = cr.reprint;
				String name = rp.card.name.replaceAll("é", "e");
				if (rp.card.isSplit) {
					name = rp.card.simpleName + " " + CardAnalyzer.get(rp.card.otherPart.get(0)).simpleName;
				}
				System.out.println("#" + (i + 1) + "|" + name + "|" + rp.rarity.charAt(0) + "|" + set);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		CardParser.initOracle();
		CardAnalyzer.initData();
		getRank("EMN");
	}
}
