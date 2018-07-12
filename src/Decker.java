import lu.cifer.mtgviewer.CardAnalyzer;
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

		n = CardAnalyzer.searchCard("return c and not legend", showDeckCards.getAllCards());

		System.out.println("c and not legend " + n);

		n = CardAnalyzer.searchCard("return c and not b", showDeckCards.getAllCards());

		System.out.println("c and not b " + n);

		n = CardAnalyzer.searchCard("return mono and w", showDeckCards.getAllCards());

		System.out.println("mono and w " + n);

		n = CardAnalyzer.searchCard("return mono and u", showDeckCards.getAllCards());

		System.out.println("mono and u " + n);

		n = CardAnalyzer.searchCard("return mono and b", showDeckCards.getAllCards());

		System.out.println("mono and b " + n);

		n = CardAnalyzer.searchCard("return mono and r", showDeckCards.getAllCards());

		System.out.println("mono and r " + n);

		n = CardAnalyzer.searchCard("return mono and g", showDeckCards.getAllCards());

		System.out.println("mono and g " + n);

		n = CardAnalyzer.searchCard("return m", showDeckCards.getAllCards());

		System.out.println("m " + n);
		
		n = CardAnalyzer.searchCard("return cl", showDeckCards.getAllCards());

		System.out.println("cl " + n);
	}

}
