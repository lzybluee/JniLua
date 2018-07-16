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
import lu.cifer.mtgviewer.CardAnalyzer.ReprintInfo;

public class ShowDeckCards {

	File rootFolder;
	Vector<Vector<String>> standards = new Vector<>();
	Vector<String> modern = new Vector<>();
	Vector<String> frontier = new Vector<>();
	Vector<CardInfo> allCards = new Vector<>();

	public ShowDeckCards() {
		initStandard();
		initFrontier();
		initModern();
	}

	public Vector<CardInfo> getAllCards() {
		return allCards;
	}

	public void initModern() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("Script/modern.txt"));
			String str = null;
			while ((str = reader.readLine()) != null) {
				modern.add(str);
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
	}

	public void initFrontier() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("Script/frontier.txt"));
			String str = null;
			while ((str = reader.readLine()) != null) {
				frontier.add(str);
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
	}

	public void initStandard() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("Script/standard.txt"));
			String str = null;
			Vector<String> sets = new Vector<>();
			while ((str = reader.readLine()) != null) {
				if (str.isEmpty()) {
					standards.add(sets);
					sets = new Vector<>();
				} else {
					sets.add(str);
				}
			}
			if (!sets.isEmpty()) {
				standards.add(sets);
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
	}

	public String getDeckListName(String fileName) {
		fileName = fileName.replaceAll("[^ -~]", "");
		return fileName.substring(0, fileName.lastIndexOf(".")) + ".txt";
	}

	public void saveDeck(File file, String text) {
		String name = getDeckListName(file.getName());

		File outFolder = new File(
				file.getParentFile().getAbsolutePath().replace(rootFolder.getAbsolutePath(), "cardlist"));
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

		outFile.setLastModified(file.lastModified());
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
				if (c1.converted == c2.converted) {
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

	public void checkStandard(Vector<Vector<String>> list, CardInfo card) {
		Vector<Vector<String>> remove = new Vector<>();
		for (Vector<String> sets : list) {
			boolean flag = false;
			for (String set : sets) {
				for (ReprintInfo r : card.reprints) {
					if (r.set.equals(set)) {
						flag = true;
						break;
					}
				}
			}
			if (!flag) {
				remove.add(sets);
			}
		}
		for (Vector<String> v : remove) {
			list.remove(v);
		}
	}

	public boolean checkModern(CardInfo card) {
		for (String set : modern) {
			for (ReprintInfo r : card.reprints) {
				if (r.set.equals(set)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean checkFrontier(CardInfo card) {
		for (String set : frontier) {
			for (ReprintInfo r : card.reprints) {
				if (r.set.equals(set)) {
					return true;
				}
			}
		}
		return false;
	}

	String[] sections = { "Commander", "Main", "Sideboard", "Schemes", "Planes" };

	public String checkName(String name) {
		name = name.replace("Lim-D?l", "Lim-Dul").replace("Æ", "Ae").replaceAll("[^ -~]ther", "Aether");
		return name;
	}

	public boolean isAvatar(String name) {
		return name.endsWith(" Avatar") || name.equals("Maraxus") || name.equals("Karn") || name.equals("Tahngarth");
	}

	public void checkColor(String mana, Vector<String> colors) {
		if (mana.contains("P")) {
			return;
		}
		if (mana.contains("W") && !colors.contains("W")) {
			colors.add("W");
		}
		if (mana.contains("U") && !colors.contains("U")) {
			colors.add("U");
		}
		if (mana.contains("B") && !colors.contains("B")) {
			colors.add("B");
		}
		if (mana.contains("R") && !colors.contains("R")) {
			colors.add("R");
		}
		if (mana.contains("G") && !colors.contains("G")) {
			colors.add("G");
		}
	}

	public String loadDeck(File file) {
		System.out.println("Loading ... " + file.getAbsolutePath());
		BufferedReader reader = null;

		HashMap<String, HashMap<CardInfo, Integer>> cards = new HashMap<>();
		for (String sec : sections) {
			cards.put(sec.toLowerCase(), new HashMap<>());
		}

		Vector<Vector<String>> t2List = new Vector<>();
		for (Vector<String> v : standards) {
			t2List.add(v);
		}

		String tag = "";
		int cardNum = 0;
		int landNum = 0;
		float totalCmc = 0;

		boolean isModern = true;
		boolean isFrontier = true;
		Vector<String> deckColors = new Vector<>();

		try {
			reader = new BufferedReader(new FileReader(file));
			String str;
			while ((str = reader.readLine()) != null) {
				Pattern pattern = Pattern.compile("^(\\d+)\\s+(.*?)(\\+?)(\\|.*)?$");
				Matcher matcher = pattern.matcher(str.trim());
				if (matcher.find()) {
					String name = checkName(matcher.group(2));
					if (!name.equals(matcher.group(2))) {
						System.err.println("[Fix] Name '" + matcher.group(2) + "' -> '" + name + "'");
					}
					int num = Integer.parseInt(matcher.group(1));
					CardInfo card = getCard(name);
					if (card == null) {
						if (isAvatar(name)) {
							continue;
						} else {
							System.err.println("Not Found!!! " + name + " <- " + file.getAbsolutePath());
							System.exit(0);
						}
					}

					if (cards.get(tag).containsKey(card)) {
						num += cards.get(tag).get(card);
					}
					cards.get(tag).put(card, num);

					if (!allCards.contains(card)) {
						allCards.add(card);
					}

					if (tag.equals("main")) {
						if (card.mana != null) {
							String mana = card.mana;
							if (card.isSplit) {
								mana += CardAnalyzer.get(card.otherPart.get(0)).mana;
							}
							checkColor(mana, deckColors);
						}

						if (card.types.contains("Land")) {
							landNum += num;
						}
						int cmc = card.converted;
						if (card.isSplit) {
							cmc += CardAnalyzer.get(card.otherPart.get(0)).converted;
						}
						totalCmc += cmc * num;
						cardNum += num;
					}

					checkStandard(t2List, card);

					isModern = isModern && checkModern(card);
					isFrontier = isFrontier && checkFrontier(card);
				} else {
					str = str.toLowerCase();
					if(str.equals("[scheme]")) {
						str = "[schemes]";
						System.err.println("[Fix] Section name -> schemes");
					}
					if(str.equals("[planar]")) {
						str = "[planes]";
						System.err.println("[Fix] Section name -> planes");
					}
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

		String text = "";

		if (!t2List.isEmpty()) {
			String s = t2List.get(0).toString();
			s = s.substring(1, s.length() - 1);
			text += "<Standard> " + s + "\n";
		}

		if (isModern) {
			text += "<Modern>\n";
		}

		if (isFrontier) {
			text += "<Frontier>\n";
		}

		if (!text.isEmpty()) {
			text += "\n";
		}

		String colors = "";

		if (deckColors.isEmpty()) {
			colors = "Colorless";
		} else {
			if (deckColors.contains("W")) {
				colors += "White ";
			}
			if (deckColors.contains("U")) {
				colors += "Blue ";
			}
			if (deckColors.contains("B")) {
				colors += "Black ";
			}
			if (deckColors.contains("R")) {
				colors += "Red ";
			}
			if (deckColors.contains("G")) {
				colors += "Green ";
			}
			colors = colors.substring(0, colors.length() - 1);
		}

		text += "Colors : " + colors + "\n";

		text += "Avg CMC : " + String.format("%.2f", totalCmc / cardNum) + "  Avg Spell CMC : "
				+ String.format("%.2f", totalCmc / (cardNum - landNum)) + "\n\n";

		for (String sec : sections) {
			text += printSection(sec, cards.get(sec.toLowerCase()));
		}

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
		rootFolder = new File(folder);
		if (!rootFolder.exists()) {
			return;
		}
		loadFile(rootFolder);
	}
}
