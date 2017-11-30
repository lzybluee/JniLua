package lu.cifer.mtgviewer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
		if (CardAnalyzer.cardDatabase.containsKey(name)) {
			return CardAnalyzer.cardDatabase.get(name);
		}
		for (String s : CardAnalyzer.allName) {
			if (name.toLowerCase().equals(s.replaceAll("[àáâ]", "a").replaceAll("é", "e").replaceAll("í", "i")
					.replaceAll("ö", "o").replaceAll("[úû]", "u").replaceAll("Æ", "AE").toLowerCase())) {
				return CardAnalyzer.cardDatabase.get(s);
			}
		}
		return null;
	}

	public String printCard(String name) {
		String text = "";
		Pattern pattern = Pattern.compile("^(.*?) // (.*?)$");
		Matcher matcher = pattern.matcher(name);
		if (matcher.find()) {
			name = matcher.group(1) + " (" + matcher.group(1) + "/" + matcher.group(2) + ")";
		}
		CardInfo card = getCard(name);
		if (card != null) {
			text += card.toSimpleString();
			if (card.otherPart.size() > 0) {
				text += "<<------------------------------->>\n";
				for (String s : card.otherPart) {
					CardInfo other = CardAnalyzer.cardDatabase.get(s);
					if (!other.isSplit) {
						text += other.simpleName + "\n";
					}
					text += other.toSimpleString();
				}
			}
			text += "\n";
		} else {
			System.err.println("Not found!!! - " + name);
		}
		return text;
	}

	public String loadDeck(File file) {
		System.out.println("Loading ... " + file.getName() + "\n");
		BufferedReader reader = null;
		String text = "";
		try {
			reader = new BufferedReader(new FileReader(file));
			String str;
			while ((str = reader.readLine()) != null) {
				Pattern pattern = Pattern.compile("^(\\d+)\\s+(.*)$");
				Matcher matcher = pattern.matcher(str);
				if (matcher.find()) {
					text += matcher.group(2) + " x " + matcher.group(1) + "\n";
					text += printCard(matcher.group(2));
				} else {
					str = str.toLowerCase();
					if (str.startsWith("[commander]") || str.startsWith("[main]") || str.startsWith("[sideboard]")) {
						text += str + "\n\n";
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
