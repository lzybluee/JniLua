import lu.cifer.mtgviewer.CardAnalyzer;
import lu.cifer.mtgviewer.CardParser;
import lu.cifer.mtgviewer.LuaScript;
import lu.cifer.mtgviewer.ShowDeckCards;

public class Decker {

	public static void main(String[] args) {
		System.loadLibrary("Jni");

		LuaScript.initLua("Script/global.lua");

		CardParser.initOracle();
		String title = CardAnalyzer.initData();
		
		ShowDeckCards showDeckCards = new ShowDeckCards();
		showDeckCards.loadDeckFolder("decks/constructed/Standard");
	}

}
