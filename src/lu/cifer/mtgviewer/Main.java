package lu.cifer.mtgviewer;

import lu.cifer.mtgviewer.CardAnalyzer.CardInfo;
import lu.cifer.mtgviewer.CardAnalyzer.ReprintInfo;

public class Main {
	public static void main(String[] args) {
		System.loadLibrary("Jni");

		LuaScript.initLua("Script/global.lua");

		CardParser.initOracle();
		for (String[] ss : CardParser.SetList) {
			for (String s : ss) {
				System.out.print(s + ",");
			}
			System.out.println();
		}

		String all = CardAnalyzer.initData();
		// CardAnalyzer.javaSearch();

		System.out.println("\n================ " + all + " ================\n");

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
