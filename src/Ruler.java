import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;

public class Ruler {

	static void writeLine(int n, String s, BufferedWriter writer) throws IOException {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < n; i++) {
			buf.append(" ");
		}
		writer.write(buf.toString() + s + "\n");
	}

	public static void main(String[] args) {
		try {
			FileInputStream fis = new FileInputStream("MagicCompRules 20220429.txt");
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader reader = new BufferedReader(isr);

			FileOutputStream fos = new FileOutputStream("MagicCompRules.txt");
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			BufferedWriter writer = new BufferedWriter(osw);

			String s;
			int space = 0;
			boolean isContent = false;
			int part = 0;
			int empty = 0;
			while ((s = reader.readLine()) != null) {
				s = s.trim().replaceAll("“", "\"").replaceAll("”", "\"").replaceAll("’", "'").replaceAll("‘", "'")
						.replaceAll("–", "-");
				if (s.startsWith("These rules are effective as of ")) {
					space = 3;
					writeLine(space, s, writer);
				} else if (s.startsWith("Published by Wizards of the Coast LLC")) {
					writer.write("\n" + s + "\n\n");
					break;
				} else if (s.startsWith("Example: ")) {
					writeLine(space + 9, s, writer);
				} else {
					if (s.equals("Contents")) {
						isContent = true;
					}
					if (isContent && s.equals("Credits")) {
						isContent = false;
					}
					if (s.equals("Glossary")) {
						part++;
					}
					if (Pattern.compile("^\\d+\\.\\d+\\. ").matcher(s).find()) {
						space = 3;
					} else if (Pattern.compile("^\\d+\\.\\d+[a-z] ").matcher(s).find()) {
						space = 6;
					} else if (isContent && Pattern.compile("^\\d\\d\\d\\. ").matcher(s).find()) {
						space = 3;
					} else if (!s.isEmpty() && part == 2 && Pattern.compile("^[^\\d]").matcher(s).find()) {
						writeLine(space + 3, s, writer);
						continue;
					} else {
						space = 0;
						if (!isContent && (Pattern.compile("^\\d\\. ").matcher(s).find() || s.equals("Glossary"))
								&& empty == 1) {
							writer.write("\n");
						}
					}
					if (s.isEmpty()) {
						empty++;
						if (part == 4 && empty > 1) {
							continue;
						}
					} else {
						empty = 0;
					}
					writeLine(space, s, writer);
					if (s.equals("Credits")) {
						part++;
					}
				}
			}
			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
