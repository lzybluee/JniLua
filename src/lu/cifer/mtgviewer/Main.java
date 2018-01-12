package lu.cifer.mtgviewer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import lu.cifer.mtgviewer.CardAnalyzer.CardInfo;
import lu.cifer.mtgviewer.CardAnalyzer.ReprintInfo;

public class Main {

	public static void writeOracleFile(String title) {
		File outFile = new File("MtgOracleAll.txt");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(outFile));
			writer.write(title + "\n\n");
			for (String name : CardAnalyzer.allName) {
				writer.write(CardAnalyzer.get(name).toString() + "\n");
			}
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

	public static void main(String[] args) {
		System.loadLibrary("Jni");

		LuaScript.initLua("Script/global.lua");

		CardParser.initOracle();
		String title = CardAnalyzer.initData();
		
		PictureRepack repack = new PictureRepack();
		System.exit(0);
		
		Vector<String> mageCards = new Vector<>();
		File[] cardFolders = new File("D:\\z7z8\\xMage\\MageX\\Mage.Sets\\src\\mage\\cards").listFiles();
		for(File folder : cardFolders) {
			for(File f : folder.listFiles()) {
				System.out.println();
			}
		}

		/*HashMap<String, Integer> creatures = new HashMap<>();
		for (String s : CardAnalyzer.allName) {
			CardInfo c = CardAnalyzer.get(s);
			if ((c.legal.contains("Modern") || c.banned.contains("Modern")) && c.types.contains("Creature")) {
				for (String t : c.subTypes) {
					if (creatures.containsKey(t)) {
						creatures.put(t, creatures.get(t) + 1);
					} else {
						creatures.put(t, 1);
					}
				}
			}
		}
		for (Map.Entry<String, Integer> e : creatures.entrySet()) {
			System.out.println(e.getKey() + "\t" + e.getValue());
		}
		
		System.exit(0);*/

		/*
		 * writeOracleFile(title);
		 * 
		 * System.exit(0);
		 */

		/*
		 * ShowDeckCards showDeckCards = new ShowDeckCards();
		 * showDeckCards.loadDeckFolder("decks");
		 * 
		 * System.exit(0);
		 */

		/*
		 * for (int i = 0; i < CardAnalyzer.allName.length; i++) { for (int j =
		 * i + 1; j < CardAnalyzer.allName.length; j++) { CardInfo c1 =
		 * CardAnalyzer.get(CardAnalyzer.allName[i]); CardInfo c2 =
		 * CardAnalyzer.get(CardAnalyzer.allName[j]); if
		 * (CardAnalyzer.isSameFunction(c1, c2)) { System.out.println(c1 +
		 * "========================\n" + c2); } } }
		 * 
		 * System.exit(0);
		 */

		// System.out.println("\n================ " + all + "
		// ================\n");

		int count = CardAnalyzer.searchCard(
				"return (c and legend and partIndex <= 1 and (text or cmc == 1)) or (text and string.find(text, 'can be your commander'))",
				false);

		System.out.println("[Commander : " + count + "]\n");

		for (ReprintInfo info : CardAnalyzer.resultCards) {
			count++;
			System.out.println(info.card);
			if (info.card.otherPart.size() > 0) {
				System.out.println("<<<<<<<<-------->>>>>>>>\n");
				for (String s : info.card.otherPart) {
					System.out.println(CardAnalyzer.cardDatabase.get(s));
				}
			}
		}

		/*
		 * String[] ss = { "ccolors == 0", "ccolors == 1 and cw",
		 * "ccolors == 1 and cu", "ccolors == 1 and cb", "ccolors == 1 and cr",
		 * "ccolors == 1 and cg", "ccolors == 2 and cw and cu",
		 * "ccolors == 2 and cu and cb", "ccolors == 2 and cb and cr",
		 * "ccolors == 2 and cr and cg", "ccolors == 2 and cg and cw",
		 * "ccolors == 2 and cw and cb", "ccolors == 2 and cu and cr",
		 * "ccolors == 2 and cb and cg", "ccolors == 2 and cr and cw",
		 * "ccolors == 2 and cg and cu", "ccolors == 3 and cw and cu and cb",
		 * "ccolors == 3 and cu and cb and cr",
		 * "ccolors == 3 and cb and cr and cg",
		 * "ccolors == 3 and cr and cg and cw",
		 * "ccolors == 3 and cg and cw and cu",
		 * "ccolors == 3 and cw and cb and cr",
		 * "ccolors == 3 and cu and cr and cg",
		 * "ccolors == 3 and cb and cg and cw",
		 * "ccolors == 3 and cr and cw and cu",
		 * "ccolors == 3 and cg and cu and cb",
		 * "ccolors == 4 and cu and cb and cr and cg",
		 * "ccolors == 4 and cw and cb and cr and cg",
		 * "ccolors == 4 and cw and cu and cr and cg",
		 * "ccolors == 4 and cw and cu and cb and cg",
		 * "ccolors == 4 and cw and cu and cb and cr", "ccolors == 5", };
		 * 
		 * for(String s : ss) { int n = CardAnalyzer.searchCard("return " + s,
		 * false); System.out.println("\n=============== " + s +
		 * " =============="); System.out.println("Found " + n + "\n"); }
		 */

	}
}
