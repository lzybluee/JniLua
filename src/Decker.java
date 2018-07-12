import lu.cifer.mtgviewer.CardAnalyzer;
import lu.cifer.mtgviewer.CardAnalyzer.CardInfo;
import lu.cifer.mtgviewer.CardParser;
import lu.cifer.mtgviewer.LuaScript;
import lu.cifer.mtgviewer.ShowDeckCards;

public class Decker {

	public static void main(String[] args) {
		System.loadLibrary("Jni");

		LuaScript.initLua("Script/global.lua");

		CardParser.initOracle();
		CardAnalyzer.initData();

		ShowDeckCards showDeckCards = new ShowDeckCards();
		showDeckCards.loadDeckFolder("decks/constructed/Modern");

		System.out.println(showDeckCards.getAllCards().size() + " cards");

		System.out.println();

		int n = 0;
		int nn = 0;

		for (CardInfo c : showDeckCards.getAllCards()) {
			if (c.types.contains("Creature") && !c.isLegendary) {
				n++;
			}
		}

		System.out.println("non-legned " + n);

		n = 0;
		for (CardInfo c : showDeckCards.getAllCards()) {
			if (c.types.contains("Creature") && !((c.mana != null && c.mana.contains("B"))
					|| (c.colorIndicator != null && c.colorIndicator.contains("Black")))) {
				n++;
			}
		}

		System.out.println("non-black " + n);

		System.out.println();

		n = 0;
		nn = 0;
		for (CardInfo c : showDeckCards.getAllCards()) {
			if ((c.mana != null && c.mana.contains("W"))
					|| (c.colorIndicator != null && c.colorIndicator.contains("White"))) {
				if (c.types.contains("Creature")) {
					n++;
				} else {
					nn++;
				}
			}
		}

		System.out.println("White " + n + ", " + nn);

		n = 0;
		nn = 0;
		for (CardInfo c : showDeckCards.getAllCards()) {
			if ((c.mana != null && c.mana.contains("U"))
					|| (c.colorIndicator != null && c.colorIndicator.contains("Blue"))) {
				if (c.types.contains("Creature")) {
					n++;
				} else {
					nn++;
				}
			}
		}

		System.out.println("Blue " + n + ", " + nn);

		n = 0;
		nn = 0;
		for (CardInfo c : showDeckCards.getAllCards()) {
			if ((c.mana != null && c.mana.contains("B"))
					|| (c.colorIndicator != null && c.colorIndicator.contains("Black"))) {
				if (c.types.contains("Creature")) {
					n++;
				} else {
					nn++;
				}
			}
		}

		System.out.println("Black " + n + ", " + nn);

		n = 0;
		nn = 0;
		for (CardInfo c : showDeckCards.getAllCards()) {
			if ((c.mana != null && c.mana.contains("R"))
					|| (c.colorIndicator != null && c.colorIndicator.contains("Red"))) {
				if (c.types.contains("Creature")) {
					n++;
				} else {
					nn++;
				}
			}
		}

		System.out.println("Red " + n + ", " + nn);

		n = 0;
		nn = 0;
		for (CardInfo c : showDeckCards.getAllCards()) {
			if ((c.mana != null && c.mana.contains("G"))
					|| (c.colorIndicator != null && c.colorIndicator.contains("Green"))) {
				if (c.types.contains("Creature")) {
					n++;
				} else {
					nn++;
				}
			}
		}

		System.out.println("Green " + n + ", " + nn);

		n = CardAnalyzer.searchCard("return c and g", showDeckCards.getAllCards());

		System.out.println("CardAnalyzer.searchCard " + n);
	}

}
