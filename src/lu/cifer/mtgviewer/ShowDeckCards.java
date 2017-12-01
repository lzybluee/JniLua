package lu.cifer.mtgviewer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lu.cifer.mtgviewer.CardAnalyzer.CardInfo;

public class ShowDeckCards {

	public void saveDeck(File file, String text) {
		String name = file.getName();
		name = name.substring(0, name.lastIndexOf(".")) + " - CardList.txt";
		File outFolder = new File(file.getParentFile().getAbsolutePath()
				.replace(File.separator + "decks" + File.separator, File.separator + "cardlist" + File.separator));
		if (!outFolder.exists()) {
			outFolder.mkdirs();
		}
		File outFile = new File(outFolder + File.separator + name);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(outFile));
			writer.write(text);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	public CardInfo getCard(String name) {
		Pattern pattern = Pattern.compile("^(.*?) // (.*?)$");
		Matcher matcher = pattern.matcher(name);
		if (matcher.find()) {
			name = matcher.group(1) + " (" + matcher.group(1) + "/" + matcher.group(2) + ")";
		}
		if (CardAnalyzer.cardDatabase.containsKey(name)) {
			return CardAnalyzer.get(name);
		}
		for (String s : CardAnalyzer.allName) {
			if (name.toLowerCase().equals(s.replaceAll("[àáâ]", "a").replaceAll("é", "e").replaceAll("í", "i")
					.replaceAll("ö", "o").replaceAll("[úû]", "u").replaceAll("Æ", "AE").toLowerCase())) {
				return CardAnalyzer.get(s);
			}
		}
		return null;
	}

	public String printCard(CardInfo card) {
		String text = "";
		text += card.toSimpleString();
		if (card.otherPart.size() > 0) {
			text += "<<------------------------------->>\n";
			for (String s : card.otherPart) {
				CardInfo other = CardAnalyzer.get(s);
				if (!other.isSplit) {
					text += other.simpleName + "\n";
				}
				text += other.toSimpleString();
			}
		}
		return text;
	}

	int getRank(CardInfo card) {
		if (card.types.contains("Creature")) {
			return 0;
		} else if (card.types.contains("Instant")) {
			return 1;
		} else if (card.types.contains("Sorcery")) {
			return 2;
		} else if (card.types.contains("Enchantment")) {
			return 3;
		} else if (card.types.contains("Planeswalker")) {
			return 4;
		} else if (card.types.contains("Artifact")) {
			return 5;
		} else if (card.types.contains("Land")) {
			if (!card.superTypes.contains("Basic")) {
				return 6;
			} else {
				return 7;
			}
		}
		return 8;
	}

	Comparator<CardInfo> comparator = new Comparator<CardInfo>() {

		@Override
		public int compare(CardInfo c1, CardInfo c2) {
			int rank1 = getRank(c1);
			int rank2 = getRank(c2);
			if (rank1 == rank2) {
				if(c1.converted == c2.converted) {
					return c1.name.compareTo(c2.name);
				}
				return c1.converted - c2.converted;
			}
			return rank1 - rank2;
		}
	};

	public String printSection(String section, HashMap<CardInfo, Integer> map) {
		String text = "";
		if (map.size() == 0) {
			return "";
		}
		text += "[" + section + "]\n\n";
		Vector<CardInfo> cards = new Vector<>();
		for (CardInfo card : map.keySet()) {
			cards.add(card);
		}

		Collections.sort(cards, comparator);

		for (CardInfo card : cards) {
			if (card.isSplit) {
				text += card.simpleName + " // " + CardAnalyzer.get(card.otherPart.get(0)).simpleName;
			} else {
				text += card.simpleName;
			}
			text += " x " + map.get(card) + "\n";
			text += printCard(card) + "\n";
		}
		return text;
	}

	String[] sections = { "Commander", "Main", "Sideboard", "Scheme", "Planar" };

	public String loadDeck(File file) {
		System.out.println("Loading ... " + file.getAbsolutePath() + "\n");
		BufferedReader reader = null;
		String text = "";

		HashMap<String, HashMap<CardInfo, Integer>> cards = new HashMap<>();
		for (String sec : sections) {
			cards.put(sec.toLowerCase(), new HashMap<>());
		}

		String tag = "";
		int cardNum = 0;
		int landNum = 0;
		float totalCmc = 0;
		String mana = "";

		try {
			reader = new BufferedReader(new FileReader(file));
			String str;
			while ((str = reader.readLine()) != null) {
				Pattern pattern = Pattern.compile("^(\\d+)\\s+(.*)$");
				Matcher matcher = pattern.matcher(str);
				if (matcher.find()) {
					String name = matcher.group(2);
					int num = Integer.parseInt(matcher.group(1));
					CardInfo card = getCard(name);
					if (card == null) {
						System.err.println("Not Found!!! " + matcher.group(2) + " <- " + file.getAbsolutePath());
						System.exit(0);
					}

					if(card.types.contains("Land")) {
						landNum += num;
					}
					int cmc = card.converted;
					if(card.isSplit) {
						cmc += CardAnalyzer.get(card.otherPart.get(0)).converted;
					}
					totalCmc += cmc * num;
					cardNum += num;

					if (card.mana != null) {
						mana += card.mana;
					}

					if (cards.get(tag).containsKey(card)) {
						num += cards.get(tag).get(card);
					}
					cards.get(tag).put(card, num);
				} else {
					str = str.toLowerCase();
					for (String sec : sections) {
						if (str.contains("[" + sec.toLowerCase())) {
							tag = str.substring(1, str.indexOf("]"));
							break;
						}
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}

		for (String sec : sections) {
			text += printSection(sec, cards.get(sec.toLowerCase()));
		}

		text = "Avg CMC : " + String.format("%.2f", totalCmc / cardNum) + 
				"  Avg Spell CMC : " + String.format("%.2f", totalCmc / (cardNum - landNum)) + "\n\n" + text;

		String colors = "";
		if(mana.contains("W")) {
			colors += "White ";
		}
		if(mana.contains("U")) {
			colors += "Blue ";
		}
		if(mana.contains("B")) {
			colors += "Black ";
		}
		if(mana.contains("R")) {
			colors += "Red ";
		}
		if(mana.contains("G")) {
			colors += "Green ";
		}
		if(colors.endsWith(" ")) {
			colors = colors.substring(0, colors.length() - 1);
		}
		
		text = "Colors : " + colors + "\n" + text;

		return text;
	}

	public void loadFile(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				loadFile(f);
			}
		} else if (file.getName().toLowerCase().endsWith(".dck")) {
			String text = loadDeck(file);
			saveDeck(file, text);
		}
	}

	public void loadDeckFolder(String folder) {
		File file = new File(folder);
		if (!file.exists()) {
			return;
		}
		loadFile(file);
	}
}
