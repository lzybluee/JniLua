package lu.cifer.mtgviewer;

import java.io.File;

import lu.cifer.mtgviewer.CardAnalyzer.CardInfo;
import lu.cifer.mtgviewer.CardAnalyzer.ReprintInfo;

public class PictureRepack {

	static String[][] forgeFolder = { { "5DN", "FD" }, { "8EB", "8E" }, { "9EB", "9E" }, { "AI", "AL" }, { "AL", "A" },
			{ "BE", "B" }, { "CMA", "CM1" }, { "CMD", "COM" }, { "DI", "DIS" }, { "DVD", "DDC" }, { "FVD", "DRB" },
			{ "GVL", "DDD" }, { "JVC", "DD2" }, { "LW", "LRW" }, { "MI", "MR" }, { "MPSAKH", "MPS_AKH" },
			{ "MPSKLD", "MPS_KLD" }, { "MR", "MI" }, { "MT", "MOR" }, { "P3K", "P3" }, { "PC", "PLC" },
			{ "PCH", "HOP" }, { "PO", "PT" }, { "PO2", "P2" }, { "PR", "PY" }, { "PVC", "DDE" }, { "RV", "R" },
			{ "TP", "TE" }, { "TR", "TO" }, { "TS", "TSP" }, { "TSTS", "TSB" }, { "UN", "U" }, };

	static String[][] mageFolder = { { "MPSAKH", "MPS-AKH" }, { "MPSKLD", "MPS-KLD" }, };

	PictureRepack() {
		for (String s : CardAnalyzer.allName) {
			CardInfo card = CardAnalyzer.get(s);
			if (card.isSplit && card.partIndex == 2) {
				continue;
			}
			for (ReprintInfo reprint : card.reprints) {
				generateCmd(reprint);
			}
		}
	}

	String getForgeName(CardInfo card) {
		if (card.isSplit) {
			return card.simpleName + CardAnalyzer.get(card.otherPart.get(0)).simpleName;
		} else {
			return card.simpleName.replaceAll(":", "").replaceAll("\"", "");
		}
	}

	String getMageName(CardInfo card) {
		if (card.isSplit) {
			return card.simpleName +" - " +  CardAnalyzer.get(card.otherPart.get(0)).simpleName;
		} else {
			return card.simpleName.replaceAll(":", "").replaceAll("\"", "");
		}
	}

	String getForgeFolder(String set) {
		for (String[] n : forgeFolder) {
			if (!n[1].isEmpty() && set.equals(n[0])) {
				return n[1];
			}
		}
		return set;
	}

	String getMageFolder(String set) {
		for (String[] n : mageFolder) {
			if (!n[1].isEmpty() && set.equals(n[0])) {
				return n[1];
			}
		}
		return set;
	}

	void generateCmd(ReprintInfo reprint) {
		String path = "D:\\Forge\\cardsDir\\" + getForgeFolder(reprint.altCode) + "\\" + getForgeName(reprint.card);
		if (reprint.sameIndex > 0 && !reprint.card.isMeld && !path.contains("Kaya, Ghost Assassin")) {
			path += reprint.sameIndex;
		}
		path += ".full.jpg";
		File f = new File(path);
		if (!f.exists()) {
			System.err.println(path);
		}

		String copyPath = ".\\" + getMageFolder(reprint.code) + "\\" + getMageName(reprint.card);
		if (reprint.sameIndex > 0 && !reprint.card.isMeld && !path.contains("Kaya, Ghost Assassin")) {
			copyPath += "." + reprint.number;
		}
		copyPath += ".full.jpg";
		System.out.println("copy \"" + path + "\" \"" + copyPath + "\"");
	}
}
