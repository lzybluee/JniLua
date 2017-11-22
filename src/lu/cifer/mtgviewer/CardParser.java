package lu.cifer.mtgviewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

public class CardParser {

	public static String[][] SetList;

	public static int rulePage = 0;
	public static String oracleFolder = "Oracle";

	public static void initOracle() {
		File file = new File("Script/oracle.txt");
		if (file.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String s = reader.readLine();
				if (new File(s).exists()) {
					oracleFolder = s;
				}
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		file = new File("Script/list.txt");
		if (file.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String s = null;
				Vector<String[]> vector = new Vector<>();
				while ((s = reader.readLine()) != null) {
					s = s.trim();
					if (s.isEmpty() || s.startsWith("#")) {
						continue;
					}
					vector.add(s.split(","));
				}
				reader.close();
				SetList = new String[vector.size()][];
				for (int i = 0; i < vector.size(); i++) {
					SetList[i] = vector.get(i);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
