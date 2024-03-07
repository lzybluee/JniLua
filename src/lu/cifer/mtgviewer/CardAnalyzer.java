package lu.cifer.mtgviewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardAnalyzer {

    public static boolean CHECK = true;

    public static String[] LegalList = { "Block", "Modern", "Legacy", "Vintage", "Commander" };
    public static String[] TypeList = { "Artifact", "Creature", "Enchantment", "Instant", "Land", "Planeswalker",
            "Sorcery", "Tribal" };
    public static String[] SpecialTypeList = { "Conspiracy", "Phenomenon", "Plane", "Scheme", "Vanguard" };

    public static String[] results;
    public static int exclude;

    static Hashtable<String, CardInfo> cardDatabase = new Hashtable<>();
    static int reprintCards = 0;
    static String[] allName;
    static int[] landIndex = new int[5];
    static HashMap<String, Integer> cardNameInSet = new HashMap<>();
    static String wrongCard;
    static String filterString = "";
    static Vector<String> lastFilter;
    static Vector<String> filter = new Vector<>();
    static Vector<String> setOrder = new Vector<>();
    static int progress;
    static int foundCards;
    static boolean reverse;
    static int sortType = 1;
    static String[] sortName = new String[] { "Edition", "Name", "Cmc", "Color", "Rating", "Random" };
    static boolean stop;
    static Vector<ReprintInfo> resultCards;
    static String lastCode;
    static boolean single = true;
    static int subSet = 0;

    public static CardInfo get(String name) {
        return cardDatabase.get(name);
    }

    static Comparator<ReprintInfo> editionComparator = new Comparator<ReprintInfo>() {
        @Override
        public int compare(ReprintInfo left, ReprintInfo right) {
            int ret;
            if (left.order == right.order) {
                ret = left.formatedNumber.compareTo(right.formatedNumber);
            } else {
                ret = left.order - right.order;
            }
            return reverse ? -ret : ret;
        }
    };

    static Comparator<ReprintInfo> nameComparator = new Comparator<ReprintInfo>() {
        @Override
        public int compare(ReprintInfo left, ReprintInfo right) {
            int ret;
            if (left.card.name.equals(right.card.name)) {
                if (left.order == right.order) {
                    ret = left.formatedNumber.compareTo(right.formatedNumber);
                } else {
                    ret = left.order - right.order;
                }
            } else {
                ret = left.card.name.compareTo(right.card.name);
            }
            return reverse ? -ret : ret;
        }
    };

    static Comparator<ReprintInfo> cmcComparator = new Comparator<ReprintInfo>() {
        @Override
        public int compare(ReprintInfo left, ReprintInfo right) {
            int ret;
            if (left.card.value == right.card.value) {
                if (left.order == right.order) {
                    ret = left.formatedNumber.compareTo(right.formatedNumber);
                } else {
                    ret = left.order - right.order;
                }
            } else {
                ret = left.card.value - right.card.value;
            }
            return reverse ? -ret : ret;
        }
    };

    static Comparator<ReprintInfo> ratingComparator = new Comparator<ReprintInfo>() {
        @Override
        public int compare(ReprintInfo left, ReprintInfo right) {
            float ret;
            if (left.rating == right.rating) {
                if (left.order == right.order) {
                    ret = left.formatedNumber.compareTo(right.formatedNumber);
                } else {
                    ret = left.order - right.order;
                }
            } else {
                ret = left.rating - right.rating;
            }
            if (reverse) {
                return ret > 0.0f ? 1 : -1;
            } else {
                return ret > 0.0f ? -1 : 1;
            }
        }
    };

    static Comparator<ReprintInfo> colorComparator = new Comparator<ReprintInfo>() {
        @Override
        public int compare(ReprintInfo left, ReprintInfo right) {
            int ret;
            int leftColorMask = getColorMask(left.card);
            int rightColorMask = getColorMask(right.card);
            if (leftColorMask == rightColorMask) {
                if (left.card.value == right.card.value) {
                    if (left.order == right.order) {
                        ret = left.formatedNumber.compareTo(right.formatedNumber);
                    } else {
                        ret = left.order - right.order;
                    }
                } else {
                    ret = left.card.value - right.card.value;
                }
            } else {
                ret = leftColorMask - rightColorMask;
            }
            return reverse ? -ret : ret;
        }
    };

    static boolean hasColor(CardInfo card, String longColor, String shortColor) {
        return (card.colorIndicator != null && card.colorIndicator.contains(longColor))
                || (card.mana != null && card.mana.contains(shortColor));
    }

    static boolean hasLandColor(CardInfo card, String manaColor, String land) {
        return (card.types != null && card.types.contains(land))
                || (card.text != null && (card.text.contains("{" + manaColor) || card.text.contains(manaColor + "}")));
    }

    static int getColorMask(CardInfo card) {
        int mask = 0;
        int colors = 0;
        if (hasColor(card, "White", "W")) {
            mask |= 0x1;
            colors++;
        }
        if (hasColor(card, "Blue", "U")) {
            mask |= 0x10;
            colors++;
        }
        if (hasColor(card, "Black", "B")) {
            mask |= 0x100;
            colors++;
        }
        if (hasColor(card, "Red", "R")) {
            mask |= 0x1000;
            colors++;
        }
        if (hasColor(card, "Green", "G")) {
            mask |= 0x10000;
            colors++;
        }
        mask += 0x100000 * colors;
        if (mask == 0) {
            if (card.types.contains("Land")) {
                mask = 0x10000000;
                if (hasLandColor(card, "W", "Plains")) {
                    mask |= 0x1;
                    colors++;
                }
                if (hasLandColor(card, "U", "Island")) {
                    mask |= 0x10;
                    colors++;
                }
                if (hasLandColor(card, "B", "Swamp")) {
                    mask |= 0x100;
                    colors++;
                }
                if (hasLandColor(card, "R", "Mountain")) {
                    mask |= 0x1000;
                    colors++;
                }
                if (hasLandColor(card, "G", "Forest")) {
                    mask |= 0x10000;
                    colors++;
                }
                mask += 0x100000 * colors;
                if (card.superTypes.contains("Basic")) {
                    mask += 0x10000000;
                    if (card.name.equals("Plains") || card.name.equals("Island") || card.name.equals("Swamp")
                            || card.name.equals("Mountain") || card.name.equals("Forest")) {
                        mask += 0x10000000;
                    }
                }
                if (card.types.contains("Artifact")) {
                    mask = 0x2000000 + mask - 0x10000000;
                }
            } else if (card.types.contains("Artifact")) {
                mask = 0x1000000;
            }
        }
        return mask;
    }

    public static String switchSortType(boolean increment) {
        if (increment) {
            sortType++;
            if (sortType >= sortName.length) {
                sortType = 0;
            }
        } else {
            sortType--;
            if (sortType < 0) {
                sortType = sortName.length - 1;
            }
        }
        return sortName[sortType];
    }

    public static String getSortType() {
        return sortName[sortType];
    }

    public static boolean getSingleMode() {
        return single;
    }

    public static boolean switchSingleMode() {
        single = !single;
        return single;
    }

    public static boolean isReverse() {
        return reverse;
    }

    public static void setReverse(boolean r) {
        reverse = r;
    }

    public static String getEntry(String str, String tag) {
        Pattern pattern = Pattern.compile("<" + tag + ">(.+?)</" + tag + ">", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static boolean nearlyEquals(String str1, String str2) {
        String s1 = str1.toLowerCase().replaceAll("[^a-z0-9]", "");
        String s2 = str2.toLowerCase().replaceAll("[^a-z0-9]", "");

        if (s1.equals(s2)) {
            return true;
        }

        if (s2.length() < s1.length()) {
            String s = s1;
            s1 = s2;
            s2 = s;
        }

        int[] array = new int[s1.length()];
        int count = 0, sum = 0;
        for (int i = 0; i < s1.length(); i++) {
            int n;
            array[i] = -1;
            if ((n = s2.indexOf(s1.charAt(i))) >= 0) {
                s2 = s2.substring(0, n) + s2.substring(n + 1);
                array[i] = n;
                sum += n;
                count++;
            }
        }

        float avg = (float) sum / (float) count;
        float power = 0.0f;
        for (int i = 0; i < s1.length(); i++) {
            if (array[i] >= 0) {
                power += (array[i] - avg) * (array[i] - avg);
            }
        }

        return s2.length() <= Math.max(8, str2.length() / 8) && Math.sqrt(power / count) <= 8;
    }

    private static String getFormatedNumber(String name) {
        Pattern pattern = Pattern.compile("([^\\d]*)(\\d+)([^\\d]*)");
        Matcher matcher = pattern.matcher(name);

        if (matcher.find()) {
            int num = Integer.parseInt(matcher.group(2));
            return matcher.group(1) + String.format("%03d", num) + matcher.group(3);
        } else {
            return null;
        }
    }

    private static boolean compareFilter() {
        if (lastFilter.size() != filter.size()) {
            return false;
        } else {
            for (String set : filter) {
                if (!lastFilter.contains(set)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void copyFilter() {
        lastFilter = new Vector<>();
        lastFilter.addAll(filter);
    }

    public static int getInitProgressMax() {
        if (filter.isEmpty()) {
            return CardParser.SetList.length;
        }
        return filter.size();
    }

    public static int getSearchProgressMax(boolean inResult) {
        if (inResult && resultCards != null) {
            return resultCards.size();
        } else {
            return reprintCards;
        }
    }

    public static int getProgress() {
        return progress;
    }

    public static int getFoundCards() {
        return foundCards;
    }

    public static void initProgress() {
        progress = 0;
        foundCards = 0;
        stop = false;
    }

    private static void sortReprint(CardInfo card) {
        card.reprintTimes = card.reprints.size();

        if (card.reprints.size() == 1) {
            card.reprints.get(0).reprintIndex = 1;
            card.reprints.get(0).latest = true;
            return;
        }
        Collections.sort(card.reprints, new Comparator<ReprintInfo>() {
            @Override
            public int compare(ReprintInfo left, ReprintInfo right) {
                if (left.multiverseid == right.multiverseid) {
                    return left.formatedNumber.compareTo(right.formatedNumber);
                }
                if (left.multiverseid == 0) {
                    return 1;
                }
                if (right.multiverseid == 0) {
                    return -1;
                }
                return left.multiverseid - right.multiverseid;
            }
        });
        for (int i = 0; i < card.reprintTimes; i++) {
            card.reprints.get(i).reprintIndex = i + 1;
            if (i == card.reprintTimes - 1) {
                card.reprints.get(i).latest = true;
            }
        }
    }

    public static void javaSearch() {
        for (String name : allName) {
            CardInfo card = cardDatabase.get(name);
            String text = "";
            if (card.mana != null) {
                text += card.mana + " ";
            }
            if (card.text != null) {
                text += card.text + " ";
            }
            if (card.power != null) {
                text += card.power + " ";
            }
            if (card.toughness != null) {
                text += card.toughness + " ";
            }
            Vector<Integer> v = new Vector<>();
            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(text);
            while (m.find()) {
                int i = Integer.parseInt(m.group(0));
                if (!v.contains(i)) {
                    v.add(i);
                }
            }
            if (v.size() > 5) {
                System.out.print(text + " -> [" + v.size() + "] ");
                Collections.sort(v);
                for (int i : v) {
                    System.out.print(i + " ");
                }
                System.out.println("\n=======================");
            }
        }
    }

    public static String initData() {
        if (lastFilter != null && compareFilter()) {
            return (setOrder.size() - subSet) + " Sets and " + reprintCards + " Cards" + " (" + allName.length
                    + " Unique)";
        }

        lastCode = null;

        stop = false;
        progress = 0;
        foundCards = 0;

        setOrder.clear();
        cardDatabase.clear();
        reprintCards = 0;
        subSet = 0;

        for (String[] s : CardParser.SetList) {
            landIndex = new int[5];
            cardNameInSet = new HashMap<>();
            if (filter.isEmpty() || filter.contains(s[0])) {
                if (stop) {
                    break;
                }
                progress++;
                setOrder.add(s[2]);
                processSet(new File(CardParser.oracleFolder + "/MtgOracle_" + s[2] + ".txt"));
            }
        }

        if (!stop) {
            copyFilter();
        }

        allName = new String[cardDatabase.size()];

        Enumeration<String> keys = cardDatabase.keys();
        int count = 0;
        while (keys.hasMoreElements()) {
            CardInfo info = cardDatabase.get(keys.nextElement());
            sortReprint(info);
            allName[count] = info.name;
            count++;
        }

        Arrays.sort(allName);

        return (setOrder.size() - subSet) + " Sets and " + reprintCards + " Cards" + " (" + allName.length + " Unique)";
    }

    public static CardInfo getNewCard(String str) {
        String entry;

        CardInfo card = new CardInfo();
        CardInfo otherCard;

        card.name = getEntry(str, "Name");

        card.simpleName = card.name.replaceAll(" \\(.+/.+\\)", "").replaceAll("®", "").replaceAll("[àáâ]", "a")
                .replaceAll("é", "e").replaceAll("í", "i").replaceAll("ö", "o").replaceAll("[úû]", "u")
                .replaceAll("Æ", "AE");

        card.otherPart = new Vector<>();

        entry = getEntry(str, "OtherPart");
        if (entry != null) {
            card.otherPart.add(entry);
            String num = getEntry(str, "No");
            if (num.charAt(num.length() - 1) >= 'a') {
                card.partIndex = num.charAt(num.length() - 1) - 'a' + 1;
            }
            if (card.name.contains("(") && card.name.contains("/")) {
                card.isSplit = true;
            } else if (card.partIndex == 2 && getEntry(str, "ManaCost") == null) {
                card.isDoubleFaced = true;
                for (String other : entry.split(" ; ")) {
                    otherCard = cardDatabase.get(other);
                    if (otherCard != null) {
                        otherCard.isDoubleFaced = true;
                    }
                }
            } else if (card.partIndex == 2) {
                card.isFlip = true;
                otherCard = cardDatabase.get(entry);
                otherCard.isFlip = true;
            }
        }

        entry = getEntry(str, "Type");

        Pattern pattern = Pattern.compile(" ([^ ]+)/([^ ]+)");
        Matcher matcher = pattern.matcher(entry);

        if (matcher.find()) {
            card.power = matcher.group(1);
            card.toughness = matcher.group(2);
            entry = entry.substring(0, entry.indexOf(card.power));
        }

        pattern = Pattern.compile("\\(Loyalty: ([^)]+)\\)");
        matcher = pattern.matcher(entry);

        if (matcher.find()) {
            card.loyalty = matcher.group(1);
            entry = entry.substring(0, entry.indexOf(matcher.group(0)));
        }

        pattern = Pattern.compile("([^—]+)(—(.+))?");
        matcher = pattern.matcher(entry);

        String types = null;
        String subtypes = null;

        if (matcher.find()) {
            types = matcher.group(1);
            subtypes = matcher.group(3);
        }

        card.types = new Vector<>();
        card.subTypes = new Vector<>();
        card.superTypes = new Vector<>();

        if (types != null) {
            for (String s : types.trim().split(" ")) {
                boolean flag = false;
                for (String t : TypeList) {
                    if (s.equals(t)) {
                        card.types.add(s);
                        flag = true;
                        break;
                    }
                }
                for (String t : SpecialTypeList) {
                    if (s.equals(t)) {
                        card.types.add(s);
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    continue;
                }
                if (s.equals("Legendary")) {
                    card.isLegendary = true;
                }
                card.superTypes.add(s);
            }
        }

        if (subtypes != null) {
            card.subTypes.addAll(Arrays.asList(subtypes.trim().split(" ")));
        }

        entry = getEntry(str, "ManaCost");
        if (card.isFlip && card.partIndex == 2) {
            if (entry != null) {
                if (entry.contains("W")) {
                    card.colorIndicator = "White";
                } else if (entry.contains("U")) {
                    card.colorIndicator = "Blue";
                } else if (entry.contains("B")) {
                    card.colorIndicator = "Black";
                } else if (entry.contains("R")) {
                    card.colorIndicator = "Red";
                } else if (entry.contains("G")) {
                    card.colorIndicator = "Green";
                }
            }
        } else {
            if (entry != null) {
                card.mana = entry;
                card.value = 0;

                for (String mana : entry.substring(1).split("[\\{\\}]+")) {
                    if (mana.matches("\\d+")) {
                        card.value += Integer.parseInt(mana);
                    } else if (mana.matches("[WUBRGCS]")) {
                        card.value += 1;
                    } else if (mana.matches("[WUBRG]/[WUBRGP]")) {
                        card.value += 1;
                    } else if (mana.matches("[XYZ]")) {
                        card.value += 0;
                    } else if (mana.matches("2/[WUBRGP]")) {
                        card.value += 2;
                    }
                }
            }
            card.colorIndicator = getEntry(str, "ColorIndicator");
        }

        card.text = getEntry(str, "CardText");

        card.rules = getEntry(str, "Rulings");

        card.legal = new Vector<>();
        card.restricted = new Vector<>();
        card.banned = new Vector<>();

        for (String s : LegalList) {
            entry = getEntry(str, s);
            if (entry != null) {
                switch (entry) {
                case "Legal":
                    card.legal.add(s);
                    break;
                case "Restricted":
                    card.restricted.add(s);
                    break;
                case "Banned":
                    card.banned.add(s);
                    break;
                }
            }
        }

        card.reserved = (getEntry(str, "Reserved") != null);

        String hand = getEntry(str, "HandSize");
        if (hand != null) {
            card.handModifier = Integer.parseInt(hand.substring(hand.indexOf(":") + 2));
        }

        String life = getEntry(str, "StartingLife");
        if (life != null) {
            card.lifeModifier = Integer.parseInt(life.substring(life.indexOf(":") + 2));
        }

        return card;
    }

    public static ReprintInfo addReprintCard(String str, CardInfo card) {
        reprintCards++;

        ReprintInfo reprint = new ReprintInfo();
        reprint.set = getEntry(str, "Set");
        reprint.number = getEntry(str, "No");
        reprint.flavor = getEntry(str, "Flavor");
        reprint.artist = getEntry(str, "Artist");
        reprint.rarity = getEntry(str, "Rarity");
        reprint.watermark = getEntry(str, "Watermark");

        String idEntry = getEntry(str, "Multiverseid");
        if (idEntry != null) {
            reprint.multiverseid = Integer.parseInt(idEntry);
        }

        if (reprint.rarity == null) {
            reprint.rarity = "Special";
        }

        String otherPart = getEntry(str, "OtherPart");
        if (otherPart != null && !card.otherPart.contains(otherPart)) {
            card.otherPart.add(otherPart);
            if (card.otherPart.size() == 2) {
                card.isDoubleFaced = false;
                card.isMeld = true;
                for (String name : card.otherPart) {
                    CardInfo otherCard = cardDatabase.get(name);
                    otherCard.isDoubleFaced = false;
                    otherCard.isMeld = true;
                }
                String text = cardDatabase.get(card.otherPart.get(0)).text;
                if (!text.contains("(Melds with ")) {
                    Collections.reverse(card.otherPart);
                }
            }
        }

        String rating = getEntry(str, "Rating");
        if (rating != null) {
            reprint.rating = Float.parseFloat(getEntry(str, "Rating"));
        }

        String votes = getEntry(str, "Votes");
        if (votes != null) {
            reprint.votes = Integer.parseInt(getEntry(str, "Votes"));
        }

        if (card.types.size() > 0) {
            for (String type : card.types) {
                for (String special : SpecialTypeList) {
                    if (type.equals(special)) {
                        reprint.specialType = special;
                        break;
                    }
                }
                if (reprint.specialType != null) {
                    break;
                }
            }
        }

        if (!card.isInCore) {
            for (String[] s : CardParser.SetList) {
                if (reprint.set.equals(s[0])) {
                    card.isInCore = true;
                    break;
                }
                if (s[0].equals("Limited Edition Alpha")) {
                    break;
                }
            }
        }
        if (!card.rarityChanged) {
            Vector<ReprintInfo> vector = card.reprints;
            for (ReprintInfo info : vector) {
                if (!info.rarity.equals(reprint.rarity)) {
                    card.rarityChanged = true;
                    break;
                }
            }
        }

        for (String[] strs : CardParser.SetList) {
            if (reprint.set.equals(strs[0])) {
                reprint.code = strs[2];
                reprint.folder = strs[1];
                break;
            }
        }
        reprint.formatedNumber = getFormatedNumber(reprint.number);
        if (reprint.folder == null || reprint.formatedNumber == null) {
            System.out.println("No picture " + reprint.set + " " + reprint.number);
        }

        reprint.picture = reprint.folder + "/" + reprint.formatedNumber + ".jpg";

        if (reprint.picture.contains("Funny/")) {
            if (!card.name.equals("Plains") && !card.name.equals("Island") && !card.name.equals("Swamp")
                    && !card.name.equals("Mountain") && !card.name.equals("Forest")) {
                card.isFun = true;
            }
        }

        reprint.order = setOrder.indexOf(reprint.code);

        reprint.card = card;
        card.reprints.add(reprint);

        return reprint;
    }

    public static ReprintInfo processCard(String str, HashMap<String, Object> map, File file) {
        ReprintInfo reprint = null;

        String name = getEntry(str, "Name");

        if (CHECK) {
            if (cardDatabase.containsKey(name)) {
                CardInfo c1 = getNewCard(getEntry(str, "Card"));
                CardInfo c2 = cardDatabase.get(name);
                if (!isSameFunction(c1, c2)) {
                    System.out.println(file.getName());
                    System.out.println(c1);
                    System.out.println("<<<<<<<<<<<<<<>>>>>>>>>>>>>>");
                    System.out.println(c2);
                    System.out.println();
                }
            }
        }

        if (cardDatabase.containsKey(name)) {
            reprint = addReprintCard(str, cardDatabase.get(name));
        } else {
            CardInfo card = getNewCard(getEntry(str, "Card"));
            cardDatabase.put(name, card);
            card.reprints = new Vector<>();
            reprint = addReprintCard(str, card);
        }

        if (map.containsKey(name)) {
            Object obj = map.get(name);
            if (obj instanceof ReprintInfo) {
                ReprintInfo info = (ReprintInfo) obj;
                info.sameIndex = 1;
                reprint.sameIndex = 2;
                map.put(name, 2);
            } else {
                int n = (Integer) map.get(name);
                n++;
                reprint.sameIndex = n;
                map.put(name, n);
            }
        } else {
            map.put(name, reprint);
        }

        return reprint;
    }

    public static void processSet(File file) {
        // System.out.println("Process Oracle: " + file);
        BufferedReader reader;
        HashMap<String, Object> map = new HashMap<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String str;
            String card = "";
            while ((str = reader.readLine()) != null) {
                if (str.isEmpty()) {
                    processCard(card, map, file);
                    card = "";
                } else {
                    card += str + "\n";
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getFilterString() {
        return filterString;
    }

    public static String getWrongCard() {
        String card = wrongCard;
        wrongCard = null;
        return card;
    }

    public static void setFilter(String sets) {
        if (sets == null || sets.equals("")) {
            filterString = "All";
            filter.clear();
        } else if (sets.equals("Back")) {
            if (filterString.isEmpty()) {
                filterString = "Back";
            }
        } else {
            Vector<String> v = new Vector<>();
            String[] paths = sets.split("\\|");
            for (String[] s : CardParser.SetList) {
                for (String path : paths) {
                    if ((s[1] + "/").contains(path + "/")) {
                        v.add(s[0]);
                        break;
                    }
                }
            }
            if (!v.isEmpty()) {
                filterString = sets;
                filter = v;
            }
        }
    }

    public static void setStop() {
        stop = true;
    }

    public static void clearResults() {
        resultCards = null;
    }

    public static boolean containsUpperCase(String text) {
        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                return true;
            }
        }
        return false;
    }

    public static boolean containsText(String text, String search) {
        if (search.startsWith("\"") && search.endsWith("\"")) {
            String s = search.substring(1, search.length() - 1);
            if (s.isEmpty()) {
                return false;
            }
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(s) + "\\b");
            Matcher matcher = pattern.matcher(text);
            return matcher.find();
        } else {
            return text.contains(search);
        }
    }

    public static boolean checkStringGroup(String text, String search, boolean anyWord) {
        if (search.isEmpty()) {
            return true;
        }

        if (!containsUpperCase(search)) {
            text = text.toLowerCase();
            search = search.toLowerCase();
        }

        boolean ret = false;
        String[] strs = search.contains("|") ? search.split("\\|") : search.split(" ");

        if (anyWord) {
            for (String s : strs) {
                if (!s.isEmpty() && containsText(text, s)) {
                    return true;
                }
            }
        } else {
            for (String s : strs) {
                if (!s.isEmpty()) {
                    if (containsText(text, s)) {
                        ret = true;
                    } else {
                        ret = false;
                        break;
                    }
                }
            }
        }
        return ret;
    }

    private static boolean checkCard(ReprintInfo reprint, String script, Vector<ReprintInfo> cards) {
        progress++;
        int result;

        if (script.startsWith("@")) {
            result = checkStringGroup(reprint.card.simpleName, script.substring(1).trim(), false) ? 1 : 0;
        } else {
            result = LuaScript.checkCard(reprint, script);
        }

        if (result == 1) {
            foundCards++;
            cards.add(reprint);
        } else if (result == 2) {
            wrongCard = reprint.picture;
            results = new String[] { wrongCard };
            return false;
        }
        return true;
    }

    public static int searchCard(String script) {
        return searchCard(script, false, null);
    }

    public static int searchCard(String script, Vector<CardInfo> external) {
        return searchCard(script, false, external);
    }

    public static int searchCard(String script, boolean searchResult, Vector<CardInfo> external) {
        Vector<ReprintInfo> cards = new Vector<>();
        boolean skipSearch = false;
        boolean noResult = false;

        wrongCard = null;
        stop = false;
        progress = 0;
        foundCards = 0;
        exclude = 0;

        if (lastCode != null && !script.isEmpty() && script.equals(lastCode)) {
            skipSearch = true;
        }

        if (resultCards == null) {
            noResult = true;
            searchResult = false;
            skipSearch = false;
            resultCards = new Vector<>();
        }

        if (skipSearch) {
            cards = resultCards;
        } else {
            if (searchResult) {
                if (resultCards.isEmpty()) {
                    return -2;
                }
                for (ReprintInfo reprint : resultCards) {
                    if (stop) {
                        break;
                    }
                    if (!checkCard(reprint, script, cards)) {
                        return -1;
                    }
                }
            } else if (external == null) {
                for (String name : allName) {
                    if (stop) {
                        break;
                    }
                    CardInfo card = cardDatabase.get(name);
                    for (ReprintInfo reprint : card.reprints) {
                        if (!checkCard(reprint, script, cards)) {
                            return -1;
                        }
                    }
                }
            } else {
                for (CardInfo card : external) {
                    if (stop) {
                        break;
                    }
                    for (ReprintInfo reprint : card.reprints) {
                        if (!checkCard(reprint, script, cards)) {
                            return -1;
                        }
                    }
                }
            }

            if (!stop) {
                resultCards = cards;
            } else if (noResult) {
                resultCards = null;
            }
        }

        if (!stop) {
            lastCode = script;
        }

        switch (sortName[sortType]) {
        case "Edition":
            Collections.sort(cards, editionComparator);
            break;
        case "Name":
            Collections.sort(cards, nameComparator);
            break;
        case "Cmc":
            Collections.sort(cards, cmcComparator);
            break;
        case "Color":
            Collections.sort(cards, colorComparator);
            break;
        case "Rating":
            Collections.sort(cards, ratingComparator);
            break;
        case "Random":
            if (!single) {
                Collections.shuffle(cards);
            }
            break;
        }

        if (single) {
            Vector<String> names = new Vector<>();
            Vector<ReprintInfo> singleCards = new Vector<>();

            if (sortName[sortType].equals("Random")) {
                Collections.shuffle(cards);
            }

            for (ReprintInfo info : cards) {
                if (names.contains(info.card.name)) {
                    continue;
                }
                singleCards.add(info);
                names.add(info.card.name);
            }

            if (sortName[sortType].equals("Random")) {
                Collections.shuffle(singleCards);
            }

            resultCards = singleCards;

            results = new String[singleCards.size()];

            for (int i = 0; i < results.length; i++) {
                results[i] = singleCards.get(i).picture;
            }
        } else {
            results = new String[cards.size()];

            for (int i = 0; i < results.length; i++) {
                results[i] = cards.get(i).picture;
            }
        }

        exclude = cards.size() - results.length;

        return results.length;
    }

    public static boolean compareStringVector(Vector<String> v1, Vector<String> v2) {
        if (v1 == null) {
            return v2 == null;
        }
        if (v2 == null) {
            return v1 == null;
        }
        if (v1.size() != v2.size()) {
            return false;
        }
        for (String s : v1) {
            if (!v2.contains(s)) {
                return false;
            }
        }
        return true;
    }

    public static boolean compareStringNoReminder(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        }
        if (s2 == null) {
            return s1 == null;
        }
        return s1.replaceAll("\\(.*?\\)", "").equals(s2.replaceAll("\\(.*?\\)", ""));
    }

    public static boolean compareString(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        }
        if (s2 == null) {
            return s1 == null;
        }
        return s1.equals(s2);
    }

    public static boolean isSameFunction(CardInfo c1, CardInfo c2) {
        return compareString(c1.mana, c2.mana) && compareString(c1.colorIndicator, c2.colorIndicator)
                && compareStringVector(c1.types, c2.types) && compareStringVector(c1.subTypes, c2.subTypes)
                && compareStringVector(c1.superTypes, c2.superTypes) && compareString(c1.power, c2.power)
                && compareString(c1.toughness, c2.toughness) && compareString(c1.loyalty, c2.loyalty)
                && compareString(c1.text, c2.text) && compareString(c1.rules, c2.rules)
                && compareStringVector(c1.legal, c2.legal);
    }

    public static class ReprintInfo {

        public CardInfo card;

        public int multiverseid;
        public float rating;
        public int votes;
        public String set;
        public String code;
        public String folder;
        public String number;
        public String flavor;
        public String artist;
        public String rarity;
        public String watermark;
        public String specialType;

        public String picture;
        public int sameIndex;
        public String formatedNumber;
        public int order;
        public int reprintIndex;
        public boolean latest;

        public String toString() {
            return (multiverseid == 0 ? "" : multiverseid + " ") + set
                    + (specialType == null ? "" : " [" + specialType + "]") + " : " + number + " (" + rarity + ")"
                    + (artist != null ? " " + artist : "")
                    + (rating > 0 || votes > 0 ? " (" + rating + "|" + votes + ")" : "");
        }
    }

    public static class CardInfo {

        public String name;
        public String simpleName;
        public Vector<String> otherPart;
        public int partIndex;
        public boolean isSplit;
        public boolean isDoubleFaced;
        public boolean isFlip;
        public boolean isMeld;
        public boolean isLegendary;
        public boolean isFun;
        public boolean isInCore;

        public Vector<String> types;
        public Vector<String> subTypes;
        public Vector<String> superTypes;

        public String mana;
        public int value;
        public String colorIndicator;

        public String power;
        public String toughness;
        public String loyalty;

        public String text;

        public String rules;
        public Vector<String> legal;
        public Vector<String> restricted;
        public Vector<String> banned;
        public boolean reserved;

        public int handModifier;
        public int lifeModifier;

        public Vector<ReprintInfo> reprints;
        public boolean rarityChanged;

        public int reprintTimes;

        public String toSimpleString() {
            StringBuilder str = new StringBuilder();
            if (superTypes.size() > 0) {
                for (int i = 0; i < superTypes.size(); i++) {
                    str.append(superTypes.get(i));
                    if (i < superTypes.size() - 1) {
                        str.append(" ");
                    }
                }
            }
            if (types.size() > 0) {
                if (superTypes.size() > 0) {
                    str.append(" ");
                }
                for (int i = 0; i < types.size(); i++) {
                    str.append(types.get(i));
                    if (i < types.size() - 1) {
                        str.append(" ");
                    }
                }
            }
            if (subTypes.size() > 0) {
                str.append(" — ");
                for (int i = 0; i < subTypes.size(); i++) {
                    str.append(subTypes.get(i));
                    if (i < subTypes.size() - 1) {
                        str.append(" ");
                    }
                }
            }
            if (power != null) {
                str.append(" " + power);
            }
            if (toughness != null) {
                str.append("/" + toughness);
            }
            if (loyalty != null) {
                str.append(" " + "(Loyalty: " + loyalty + ")");
            }
            str.append("\n");
            if (mana != null) {
                str.append(mana + " — " + value + "\n");
            }
            if (colorIndicator != null) {
                str.append("(Color Indicator: " + colorIndicator + ")\n");
            }
            if (text != null) {
                str.append(text + "\n");
            }

            return str.toString();
        }

        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append(name + "\n");
            if (partIndex > 0) {
                String suffix = "th";
                if (Integer.toString(partIndex).endsWith("1")) {
                    suffix = "st";
                } else if (Integer.toString(partIndex).endsWith("2")) {
                    suffix = "nd";
                } else if (Integer.toString(partIndex).endsWith("3")) {
                    suffix = "rd";
                }
                if (otherPart.size() >= 2) {
                    str.append(partIndex + suffix + " part of the card, other parts are ");
                    for (int i = 0; i < otherPart.size(); i++) {
                        str.append("<" + otherPart.get(i) + ">");
                        if (i == otherPart.size() - 1) {
                            str.append("\n");
                        } else if (i == otherPart.size() - 2) {
                            str.append(" and ");
                        } else {
                            str.append(" , ");
                        }
                    }
                } else if (otherPart.size() == 1) {
                    str.append(partIndex + suffix + " part of the card, other part is <" + otherPart.get(0) + ">\n");
                }
            }
            if (superTypes.size() > 0) {
                for (int i = 0; i < superTypes.size(); i++) {
                    str.append(superTypes.get(i));
                    if (i < superTypes.size() - 1) {
                        str.append(" ");
                    }
                }
            }
            boolean vanguard = false;
            if (types.size() > 0) {
                if (superTypes.size() > 0) {
                    str.append(" ");
                }
                for (int i = 0; i < types.size(); i++) {
                    if (types.get(i).equals("Vanguard")) {
                        vanguard = true;
                    }
                    str.append(types.get(i));
                    if (i < types.size() - 1) {
                        str.append(" ");
                    }
                }
            }
            if (subTypes.size() > 0) {
                str.append(" — ");
                for (int i = 0; i < subTypes.size(); i++) {
                    str.append(subTypes.get(i));
                    if (i < subTypes.size() - 1) {
                        str.append(" ");
                    }
                }
            }
            if (power != null) {
                str.append(" " + power);
            }
            if (toughness != null) {
                str.append("/" + toughness);
            }
            if (loyalty != null) {
                str.append(" " + "(Loyalty: " + loyalty + ")");
            }
            str.append("\n");
            if (mana != null) {
                str.append(mana + " — " + value + "\n");
            }
            if (colorIndicator != null) {
                str.append("(Color Indicator: " + colorIndicator + ")\n");
            }
            if (text != null) {
                str.append(text + "\n");
            }
            if (vanguard) {
                str.append("(Hand Modifier: " + (handModifier >= 0 ? "+" : "") + handModifier + " , "
                        + "Life Modifier: " + (lifeModifier >= 0 ? "+" : "") + lifeModifier + ")\n");
            }
            if (rules != null) {
                str.append(rules + "\n");
            }
            if (legal.size() > 0) {
                str.append("Legal in ");
                for (int i = 0; i < legal.size(); i++) {
                    str.append(legal.get(i));
                    if (i < legal.size() - 1) {
                        str.append("/");
                    }
                }
                str.append("\n");
            }
            if (restricted.size() > 0) {
                str.append("Restricted in ");
                for (int i = 0; i < restricted.size(); i++) {
                    str.append(restricted.get(i));
                    if (i < restricted.size() - 1) {
                        str.append("/");
                    }
                }
                str.append("\n");
            }
            if (banned.size() > 0) {
                str.append("Banned in ");
                for (int i = 0; i < banned.size(); i++) {
                    str.append(banned.get(i));
                    if (i < banned.size() - 1) {
                        str.append("/");
                    }
                }
                str.append("\n");
            }
            if (reserved) {
                str.append("In RESERVED list!\n");
            }
            if (reprints != null) {
                Vector<String> watermarks = new Vector<>();
                for (ReprintInfo info : reprints) {
                    if (info.watermark != null && !watermarks.contains(info.watermark)) {
                        watermarks.add(info.watermark);
                    }
                }
                if (!watermarks.isEmpty()) {
                    str.append("<Watermark: ");
                    for (int i = 0; i < watermarks.size(); i++) {
                        str.append(watermarks.get(i));
                        if (i + 1 < watermarks.size()) {
                            str.append(", ");
                        }
                    }
                    str.append(">\n");
                }
                for (ReprintInfo info : reprints) {
                    str.append(info + "\n");
                }
                Vector<String> flavors = new Vector<>();
                for (ReprintInfo info : reprints) {
                    if (info.flavor != null) {
                        boolean flag = false;
                        for (String s : flavors) {
                            if (nearlyEquals(s, info.flavor)) {
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            flavors.add(info.flavor);
                        }
                    }
                }
                for (String s : flavors) {
                    str.append(s + "\n");
                }
            }
            return str.toString();
        }
    }
}
