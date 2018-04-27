import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scryfall {

	static boolean check = true;
	static long smallest = Long.MAX_VALUE;
	static String smallestFile = "";

	static class DownloadThread implements Runnable {
		public boolean running = false;
		public String set = "";

		public DownloadThread() {

		}

		public void checkFile(File file, String urlStr) {
			int bufferSize = 1024 * 1024 * 4;
			byte buffer[] = new byte[bufferSize];
			try {
				InputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis, bufferSize);
				int readSize = 0;
				byte b1 = 0, b2 = 0, b3 = 0, b4 = 0;
				while ((readSize = bis.read(buffer)) != -1) {
					b4 = buffer[readSize - 4];
					b3 = buffer[readSize - 3];
					b2 = buffer[readSize - 2];
					b1 = buffer[readSize - 1];
				}
				if (file.getAbsolutePath().toLowerCase().endsWith(".jpg")) {
					if (!(b2 == -1 && b1 == -39)) {
						System.out.println(String.format("Check Binary Error %s %02x %02x", file.toString(), b2, b1));
					}
				} else if (file.getAbsolutePath().toLowerCase().endsWith(".png")) {
					if (!(b4 == -82 && b3 == 66 && b2 == 96 && b1 == -126)) {
						System.out.println(String.format("Check Binary Error %s %02x %02x %02x %02x", file.toString(),
								b4, b3, b2, b1));
					}
				}
				bis.close();
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			/*
			 * try { URL url = new URL(urlStr); HttpURLConnection conn =
			 * (HttpURLConnection) url.openConnection();
			 * conn.setConnectTimeout(3 * 1000);
			 * conn.setRequestProperty("User-Agent",
			 * "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)"); int
			 * length = conn.getContentLength(); if (length != file.length()) {
			 * System.out.println( String.format("Check Length Error %s %d %d",
			 * file.toString(), (int) file.length(), length)); } } catch
			 * (Exception e) { e.printStackTrace(); }
			 */
		}

		public void downLoadFromUrl(String urlStr, String fileName, String savePath) throws IOException {

			File saveDir = new File(savePath);
			if (!saveDir.exists()) {
				saveDir.mkdir();
			}
			if (!saveDir.exists()) {
				saveDir.mkdirs();
			}
			File file = new File(saveDir + File.separator + fileName);
			if (check && file.exists()) {
				checkFile(file, urlStr);
				if (file.length() < smallest) {
					smallest = file.length();
					smallestFile = file.getAbsolutePath();
				}
				return;
			}

			URL url = new URL(urlStr);
			byte[] getData = null;
			int retry = 0;

			while (retry <= 3) {
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3 * 1000);
				conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
				int length = conn.getContentLength();

				InputStream inputStream = conn.getInputStream();
				getData = readInputStream(inputStream);
				if (inputStream != null) {
					inputStream.close();
				}

				if (getData.length == length) {
					break;
				}

				System.out.println("Error: Length! " + url + " " + getData.length + " " + length);

				retry++;
				if (retry > 3) {
					break;
				}
			}

			FileOutputStream fos = new FileOutputStream(file);
			fos.write(getData);
			if (fos != null) {
				fos.close();
			}

			if (check) {
				System.out.println("info:" + url + " download success");
			}
		}

		public byte[] readInputStream(InputStream inputStream) throws IOException {
			byte[] buffer = new byte[1024];
			int len = 0;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			while ((len = inputStream.read(buffer)) != -1) {
				bos.write(buffer, 0, len);
			}
			bos.close();
			return bos.toByteArray();
		}

		String readUrl(String http) {
			String content = "";
			try {
				URL url = new URL(http);
				URLConnection URLconnection = url.openConnection();
				URLconnection.setRequestProperty("User-Agent",
						"Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
				URLconnection.setConnectTimeout(60000);
				URLconnection.setReadTimeout(60000);
				HttpURLConnection httpConnection = (HttpURLConnection) URLconnection;
				int responseCode = httpConnection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					InputStream in = httpConnection.getInputStream();
					InputStreamReader isr = new InputStreamReader(in);
					BufferedReader bufr = new BufferedReader(isr);
					String str;
					while ((str = bufr.readLine()) != null) {
						content += str + "\n";
					}
					bufr.close();
				} else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
					return "";
				} else if (responseCode == HttpURLConnection.HTTP_UNAVAILABLE) {
					return null;
				} else {
					System.out.println("Error " + responseCode + " : " + url);
				}
			} catch (Exception e) {
				return null;
			}
			return content;
		}

		String tryUrl(String http) {
			String content = null;
			int count = 0;
			while (content == null) {
				content = readUrl(http);
				if (count > 3) {
					System.out.println("!!! " + http);
					return "";
				}
				count++;
			}
			return content;
		}

		String getNewName(String s) {
			if (s.charAt(s.length() - 1) < '0' || s.charAt(s.length() - 1) > '9') {
				if (s.length() == 2) {
					return "00" + s;
				}
				if (s.length() == 3) {
					return "0" + s;
				}
			} else {
				if (s.length() == 1) {
					return "00" + s;
				}
				if (s.length() == 2) {
					return "0" + s;
				}
			}
			return s;
		}

		public void downloadSet(String set) {
			String url = "https://scryfall.com/sets/" + set;
			String content = tryUrl(url);

			Vector<String> v = new Vector<>();
			Pattern pattern = Pattern.compile("(https://img.scryfall.com/cards/normal/en/.*?/(.*?)\\.jpg)\\?\\d+");
			Matcher matcher = pattern.matcher(content);
			while (matcher.find()) {
				String s = matcher.group(1);
				if (!v.contains(s)) {
					v.add(s);
					s = s.replace("/normal/", "/png/").replace(".jpg", ".png");

					try {
						downLoadFromUrl(s, getNewName(matcher.group(2)) + ".png",
								"D:/Scryfall/" + (set.equals("con") ? "CFX" : set.toUpperCase()));
					} catch (IOException e) {
						System.out.println("Error : " + s);
						e.printStackTrace();
					}
				}
			}

			System.out.println("Finished " + set);
			running = false;
		}

		@Override
		public void run() {
			downloadSet(set);
		}
	}

	static String[] sets = new String[] { "dom"};

	static DownloadThread[] threads = new DownloadThread[16];

	public static void main(String[] args) {
		int index = 0;
		boolean finished = false;
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new DownloadThread();
		}
		while (!finished) {
			int finishedNum = 0;
			for (int i = 0; i < threads.length; i++) {
				if (!threads[i].running) {
					if (index < sets.length) {
						threads[i].set = sets[index];
						index++;
						System.out.println("Start " + threads[i].set + " " + index + "/" + sets.length);
						new Thread(threads[i]).start();
						threads[i].running = true;
					} else {
						finishedNum++;
					}
				}
			}
			if (finishedNum == threads.length) {
				finished = true;
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("\nSmallest: " + smallest + " " + smallestFile);
	}

}
