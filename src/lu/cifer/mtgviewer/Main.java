package lu.cifer.mtgviewer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lu.cifer.mtgviewer.CardAnalyzer.CardInfo;
import lu.cifer.mtgviewer.CardAnalyzer.ReprintInfo;

public class Main {

    static HashMap<String, String> rules = new HashMap<>();

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

        writeOracleFile(title);

        System.exit(0);

        for (String name : CardAnalyzer.allName) {
            CardInfo c = CardAnalyzer.get(name);
            if (c.text == null) {
                continue;
            }
            String[] lines = c.text.replaceAll("\\(.*?\\)", "").split("\n");
            Pattern p = Pattern.compile("choose target");
            boolean found = false;
            for (String line : lines) {
                if (line.startsWith("When") || line.startsWith("At")) {
                    Matcher m = p.matcher(line);
                    if (m.find() && !line.contains("If you do, ") && line.contains(" target ")) {
                        System.out.println(c.name);
                        System.out.println(c.text);
                        System.out.println();
                        break;
                    }
                }
            }
            if (found) {
                System.out.println(c.name);
                System.out.println(c.text);
                System.out.println();
            }
        }

        System.exit(0);

        String[][] convert = new String[][] { { "Schemes", "ARC" }, { "SchemeNicolBolas", "E01" }, { "Plane", "HOP" },
                { "Plane2012", "PC2" }, { "TMP", "TE" }, { "ODY", "OD" }, { "HML", "HL" }, { "5ED", "5E" },
                { "WTH", "WL" }, { "6ED", "6E" }, { "PCY", "PY" }, { "LEG", "LG" }, { "4ED", "4E" }, { "ULG", "UL" },
                { "USG", "US" }, { "GPT", "GP" }, { "ARN", "AN" }, { "CHR", "CH" }, { "INV", "IN" }, { "5DN", "FD" },
                { "7ED", "7E" }, { "S99", "ST" }, { "MIR", "MI" }, { "PO2", "P2" }, { "ICE", "IA" }, { "8ED", "8E" },
                { "UDS", "UD" }, { "TOR", "TO" }, { "SCG", "SC" }, { "NMS", "NE" }, { "ONS", "ON" }, { "CMD", "COM" },
                { "STH", "SH" }, { "H09", "PDS" }, { "CSP", "CS" }, { "9ED", "9E" }, { "FEM", "FE" }, { "ALL", "AL" },
                { "APC", "AP" }, { "DST", "DS" }, { "MRD", "MR" }, { "EXO", "EX" }, { "MMQ", "MM" },
                { "MPSKLD", "MPS_KLD" }, { "MPSAKH", "MPS_AKH" }, { "LEA", "A" }, { "LEB", "B" }, { "2ED", "U" },
                { "3ED", "R" }, { "LGN", "LE" }, { "VIS", "VI" }, { "POR", "PT" }, { "PTK", "P3" }, { "PLS", "PS" },
                { "DRK", "DK" }, { "ATQ", "AQ" }, { "JUD", "JU" }, { "V10", "FVR" }, { "V09", "FVE" }, { "V11", "FVL" },
                { "8EB", "8E" }, { "9EB", "9E" }, };

        for (String name : CardAnalyzer.allName) {
            CardInfo c = CardAnalyzer.get(name);

            for (ReprintInfo r : c.reprints) {
                String full = ".full";
                String set = r.code;
                for (int i = 0; i < convert.length; i++) {
                    if (convert[i][0].equals(set)) {
                        if (set.contains("Plane")) {
                            full = "";
                        }
                        set = convert[i][1];
                        break;
                    }
                }
                if (set == null) {
                    System.out.println();
                }
                String index = "";
                if (r.sameIndex > 0 && !c.isMeld) {
                    index = r.sameIndex + "";
                }
                String n = c.name.replaceAll("[:?\"]", "").replace("é", "e").replace("ö", "o").replace("í", "i")
                        .replaceAll("[úû]", "u").replaceAll("[àáâ]", "a");
                if (c.isSplit) {
                    if (c.partIndex == 1) {
                        n = c.simpleName + CardAnalyzer.get(c.otherPart.get(0)).simpleName;
                    } else {
                        continue;
                    }
                }
                String path = "E:\\Forge\\cardsDir\\" + set + "\\" + n + index + full + ".jpg";
                if (!new File(path).exists()) {
                    System.out.println(path + " <== " + r.code);
                }
            }
        }

        System.exit(0);

        CardAnalyzer.searchCard("return watermark == 'Planeswalker'");

        for (ReprintInfo info : CardAnalyzer.resultCards) {
            System.out.println(info.card.name);
        }

        System.exit(0);

        String guild = "Golgari";

        System.out.println("[metadata]");
        System.out.println("Name=Guilds " + guild);
        System.out.println("[Main]");

        for (String s : CardAnalyzer.allName) {
            CardInfo c = CardAnalyzer.get(s);
            for (ReprintInfo r : c.reprints) {
                if (guild.equals(r.watermark)) {
                    String name = c.name;
                    if (r.card.isSplit) {
                        if (r.number.endsWith("a")) {
                            name = c.simpleName + " // " + CardAnalyzer.get(c.otherPart.get(0)).simpleName;
                        } else {
                            name = CardAnalyzer.get(c.otherPart.get(0)).simpleName + " // " + c.simpleName;
                        }
                    }
                    System.out.println("1 " + name + "|" + r.code + (r.sameIndex > 0 ? "|" + r.sameIndex : ""));
                }
            }
        }

        System.exit(0);

        Vector<String> mageCards = new Vector<>();
        File[] cardFolders = new File("D:\\z7z8\\xMage\\MageX\\Mage.Sets\\src\\mage\\cards").listFiles();
        for (File folder : cardFolders) {
            for (File f : folder.listFiles()) {
                System.out.println();
            }
        }

        /*
         * HashMap<String, Integer> creatures = new HashMap<>(); for (String s :
         * CardAnalyzer.allName) { CardInfo c = CardAnalyzer.get(s); if
         * ((c.legal.contains("Modern") || c.banned.contains("Modern")) &&
         * c.types.contains("Creature")) { for (String t : c.subTypes) { if
         * (creatures.containsKey(t)) { creatures.put(t, creatures.get(t) + 1); } else {
         * creatures.put(t, 1); } } } } for (Map.Entry<String, Integer> e :
         * creatures.entrySet()) { System.out.println(e.getKey() + "\t" + e.getValue());
         * }
         * 
         * System.exit(0);
         */

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
         * for (int i = 0; i < CardAnalyzer.allName.length; i++) { for (int j = i + 1; j
         * < CardAnalyzer.allName.length; j++) { CardInfo c1 =
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
                "return (c and legend and partIndex <= 1 and (text or cmc == 1)) or (text and string.find(text, 'can be your commander'))");

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
         * String[] ss = { "ccolors == 0", "ccolors == 1 and cw", "ccolors == 1 and cu",
         * "ccolors == 1 and cb", "ccolors == 1 and cr", "ccolors == 1 and cg",
         * "ccolors == 2 and cw and cu", "ccolors == 2 and cu and cb",
         * "ccolors == 2 and cb and cr", "ccolors == 2 and cr and cg",
         * "ccolors == 2 and cg and cw", "ccolors == 2 and cw and cb",
         * "ccolors == 2 and cu and cr", "ccolors == 2 and cb and cg",
         * "ccolors == 2 and cr and cw", "ccolors == 2 and cg and cu",
         * "ccolors == 3 and cw and cu and cb", "ccolors == 3 and cu and cb and cr",
         * "ccolors == 3 and cb and cr and cg", "ccolors == 3 and cr and cg and cw",
         * "ccolors == 3 and cg and cw and cu", "ccolors == 3 and cw and cb and cr",
         * "ccolors == 3 and cu and cr and cg", "ccolors == 3 and cb and cg and cw",
         * "ccolors == 3 and cr and cw and cu", "ccolors == 3 and cg and cu and cb",
         * "ccolors == 4 and cu and cb and cr and cg",
         * "ccolors == 4 and cw and cb and cr and cg",
         * "ccolors == 4 and cw and cu and cr and cg",
         * "ccolors == 4 and cw and cu and cb and cg",
         * "ccolors == 4 and cw and cu and cb and cr", "ccolors == 5", };
         * 
         * for(String s : ss) { int n = CardAnalyzer.searchCard("return " + s, false);
         * System.out.println("\n=============== " + s + " ==============");
         * System.out.println("Found " + n + "\n"); }
         */

    }
}
