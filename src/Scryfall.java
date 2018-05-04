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
    static boolean checkLocal = false;
    static boolean checkRemote = true;

    static class DownloadThread implements Runnable {
        public boolean running = false;
        public String set = "";

        public DownloadThread() {

        }

        public boolean checkFile(File file, String urlStr) {
            boolean ret = true;

            if (checkLocal) {
                try {
                    byte buffer[] = new byte[1024 * 1024 * 4];
                    InputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis, buffer.length);
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
                            System.err.println(String.format("Check Binary Error: %s %02X %02X [FF D9]",
                                    file.getAbsolutePath(), b2, b1));
                            ret = false;
                        }
                    } else if (file.getAbsolutePath().toLowerCase().endsWith(".png")) {
                        if (!(b4 == -82 && b3 == 66 && b2 == 96 && b1 == -126)) {
                            System.err.println(String.format("Check Binary Error: %s %02X %02X %02X %02X [AE 42 60 82]",
                                    file.getAbsolutePath(), b4, b3, b2, b1));
                            ret = false;
                        }
                    }
                    bis.close();
                    fis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (checkRemote) {
                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(3 * 1000);
                    conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                    int length = conn.getContentLength();
                    if (length > 0 && length != file.length()) {
                        System.err.println(String.format("Check Length Error: %s local:%d remote:%d",
                                file.getAbsolutePath(), (int) file.length(), length));
                        ret = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return ret;
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
                boolean pass = checkFile(file, urlStr);
                if (pass) {
                    if (file.length() < smallest) {
                        smallest = file.length();
                        smallestFile = file.getAbsolutePath();
                    }
                    return;
                } else {
                    file.delete();
                }
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

                System.err.println("Error: Length! " + url + " " + getData.length + " " + length);

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
                System.out.println("Info: " + url + " download success");
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
                    System.err.println("Error " + responseCode + " : " + url);
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
                    System.err.println("Error Retry: " + http);
                    return "";
                }
                count++;
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return content;
        }

        String getNewName(String s) {
            Pattern p = Pattern.compile("^(\\d+)[^\\d].*");
            Matcher m = p.matcher(s);
            if (m.find()) {
                String num = m.group(1);
                if (num.length() == 2) {
                    return "0" + s;
                } else if (num.length() == 1) {
                    return "00" + s;
                }
            }
            return s;
        }

        public void downloadSet(String set) {
            String url = "https://scryfall.com/sets/" + set;
            String content = tryUrl(url);

            Vector<String> v = new Vector<>();
            Pattern pattern = Pattern.compile("(https://img.scryfall.com/cards/normal/[^/]+/.*?/(.*?)\\.jpg)\\?\\d+");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String s = matcher.group(1);
                if (!v.contains(s)) {
                    v.add(s);
                    s = s.replace("/normal/", "/png/").replace(".jpg", ".png");

                    try {
                        downLoadFromUrl(s, getNewName(matcher.group(2) + ".png"),
                                "E:/Scryfall/" + (set.equals("con") ? "CFX" : set.toUpperCase()));
                    } catch (IOException e) {
                        System.err.println("Error : " + s);
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

    static String[] sets = new String[] { "dom", "pdom", "tdom"/*"ss1", "dom", "pdom", "tdom", "ddu", "tddu", "a25", "ta25", "plny", "pnat",
            "rix", "prix", "trix", "j18", "f18", "ust", "pust", "tust", "e02", "v17", "ima", "tima", "ddt", "tddt",
            "pgp17", "xln", "pxln", "txln", "pss2", "pxtc", "h17", "htr", "c17", "oc17", "tc17", "ps17", "hou", "phou",
            "thou", "e01", "te01", "cma", "tcma", "akh", "mp2", "pakh", "takh", "w17", "dds", "tdds", "mm3", "tmm3",
            "aer", "paer", "taer", "f17", "j17", "pca", "tpca", "pz2", "c16", "oc16", "tc16", "ps16", "kld", "mps",
            "pkld", "tkld", "ddr", "cn2", "tcn2", "v16", "emn", "pemn", "temn", "ema", "tema", "soi", "psoi", "tsoi",
            "w16", "ddq", "ogw", "pogw", "togw", "f16", "j16", "pz1", "c15", "oc15", "tc15", "bfz", "pbfz", "tbfz",
            "pss1", "exp", "ddp", "v15", "ori", "cp3", "pori", "tori", "ps15", "mm2", "tmm2", "tpr", "dtk", "pdtk",
            "tdtk", "ptkdf", "ddo", "frf", "cp2", "pfrf", "tfrf", "ugin", "f15", "j15", "dvd", "evg", "gvl", "jvc",
            "tgvl", "tevg", "tjvc", "tdvd", "c14", "oc14", "tc14", "ktk", "pktk", "tktk", "ddn", "tddn", "v14", "m15",
            "ppc1", "cp1", "pm15", "tm15", "ps14", "vma", "cns", "tcns", "md1", "tmd1", "jou", "tdag", "thp3", "pjou",
            "tjou", "ddm", "tddm", "bng", "tbth", "thp2", "pbng", "tbng", "pdp14", "pi14", "f14", "j14", "c13", "oc13",
            "ths", "tfth", "thp1", "pths", "tths", "ddl", "tddl", "v13", "m14", "pm14", "tm14", "psdc", "mma", "tmma",
            "dgm", "pdgm", "tdgm", "pwcq", "ddk", "tddk", "gtc", "pgtc", "tgtc", "pdp13", "pi13", "f13", "j13", "cm1",
            "ocm1", "rtr", "prtr", "trtr", "ddj", "tddj", "v12", "m13", "pm13", "tm13", "pc2", "avr", "pavr", "tavr",
            "phel", "ddi", "tddi", "dka", "pdka", "tdka", "pidw", "pwp12", "pdp12", "ltk", "l12", "l13", "l14", "l15",
            "l16", "l17", "f12", "j12", "pd3", "isd", "pisd", "tisd", "ddh", "tddh", "v11", "m12", "pm12", "tm12",
            "cmd", "pcmd", "ocmd", "td2", "nph", "pnph", "tnph", "ddg", "tddg", "mbs", "pmbs", "tmbs", "me4", "pmps11",
            "pdp11", "pwp11", "f11", "g11", "olgc", "p11", "pd2", "td0", "som", "psom", "tsom", "ddf", "tddf", "v10",
            "m11", "pm11", "tm11", "arc", "parc", "dpa", "roe", "proe", "troe", "dde", "tdde", "wwk", "pwwk", "twwk",
            "pdp10", "pwp10", "pmps10", "f10", "g10", "p10", "h09", "ddd", "tddd", "zen", "pzen", "tzen", "me3", "hop",
            "phop", "v09", "m10", "pm10", "tm10", "arb", "tarb", "ddc", "tddc", "purl", "con", "tcon", "pbok", "pwp09",
            "pdtp", "pmps09", "f09", "g09", "p09", "dd2", "pdd2", "tdd2", "ala", "tala", "pwpn", "me2", "drb", "eve",
            "teve", "shm", "tshm", "p15a", "plpa", "mor", "tmor", "pmps08", "pg08", "f08", "g08", "p08", "dd1", "lrw",
            "tlrw", "med", "psum", "10e", "p10e", "t10e", "fut", "pgpx", "ppro", "plc", "pres", "pg07", "pmps07", "f07",
            "g07", "p07", "hho", "tsp", "tsb", "csp", "cst", "tcsp", "dis", "pcmp", "gpt", "pal06", "pmps06", "pjas",
            "f06", "pgtw", "phuk", "g06", "p06", "p2hg", "rav", "9ed", "sok", "bok", "pal05", "pmps", "pjse", "f05",
            "g05", "p05", "unh", "chk", "wc04", "5dn", "dst", "pal04", "f04", "g04", "p04", "mrd", "wc03", "8ed",
            "prel", "scg", "lgn", "pal03", "f03", "g03", "p03", "ovnt", "ons", "wc02", "prm", "jud", "tor", "pal02",
            "f02", "g02", "pr2", "dkm", "ody", "wc01", "apc", "7ed", "pls", "pal01", "f01", "g01", "mpr", "inv", "btd",
            "wc00", "pcy", "s00", "nem", "pelp", "pal00", "fnm", "g00", "psus", "brb", "mmq", "pwos", "wc99", "pwor",
            "pgru", "s99", "uds", "ptk", "6ed", "ulg", "pal99", "g99", "ath", "usg", "palp", "wc98", "ugl", "tugl",
            "exo", "p02", "sth", "jgp", "tmp", "ppre", "wcd", "wth", "por", "ppod", "pvan", "5ed", "vis", "itp", "mgb",
            "mir", "pcel", "parl", "rqs", "all", "ptc", "hml", "chr", "ice", "4ed", "plgm", "pmei", "fem", "drk", "sum",
            "leg", "3ed", "atq", "pdrc", "phpr", "cei", "arn", "ced", "2ed", "leb", "lea",*/ };

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

        if (!smallestFile.isEmpty()) {
            System.out.println("\nSmallest: " + smallest + " " + smallestFile);
        }
    }

}
