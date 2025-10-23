package org.mercadolibre.camilo.products.util;

import lombok.experimental.UtilityClass;

import java.text.Normalizer;
import java.util.*;

@UtilityClass
public class FuzzyUtils {

    public static double score(String needle, String haystack) {
        if (needle == null || haystack == null) return 0.0;

        String q = normalize(needle);
        String t = normalize(haystack);
        if (q.isEmpty() || t.isEmpty()) return 0.0;

        if (t.contains(q)) return 1.0;

        List<String> tokens = tokens(t);

        double jTitle = jaccardTrigram(q, t);
        double eTitle = editScore(q, t);

        double jToken = tokens.stream().mapToDouble(tok -> jaccardTrigram(q, tok)).max().orElse(0.0);
        double eToken = tokens.stream().mapToDouble(tok -> editScore(q, tok)).max().orElse(0.0);

        double base = Math.max(Math.max(jTitle, eTitle), Math.max(jToken, eToken));

        boolean prefixMatch = t.startsWith(q) || tokens.stream().anyMatch(tok -> tok.startsWith(q));
        if (prefixMatch) base = Math.min(1.0, base + 0.12);

        return base;
    }

    private static String normalize(String s) {
        String t = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return t;
    }

    private static List<String> tokens(String s) {
        if (s.isEmpty()) return List.of();
        return Arrays.asList(s.split(" "));
    }

    /**
     * Jaccard con trigramas; tolera cambios locales.
     */
    private static double jaccardTrigram(String a, String b) {
        Set<String> ta = trigrams(a);
        Set<String> tb = trigrams(b);
        if (ta.isEmpty() || tb.isEmpty()) return 0.0;
        int inter = 0;
        for (String t : ta) if (tb.contains(t)) inter++;
        int union = ta.size() + tb.size() - inter;
        return union == 0 ? 0.0 : (double) inter / union;
    }

    private static Set<String> trigrams(String s) {
        Set<String> res = new HashSet<>();
        String pad = "  " + s + "  ";
        for (int i = 0; i + 3 <= pad.length(); i++) {
            res.add(pad.substring(i, i + 3));
        }
        return res;
    }

    private static double editScore(String a, String b) {
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 1.0;
        int dist = damerauLevenshtein(a, b);
        return Math.max(0.0, 1.0 - ((double) dist / (double) maxLen));
    }

    private static int damerauLevenshtein(String s, String t) {
        int n = s.length(), m = t.length();
        if (n == 0) return m;
        if (m == 0) return n;

        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;

        for (int i = 1; i <= n; i++) {
            char cs = s.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                char ct = t.charAt(j - 1);
                int cost = (cs == ct) ? 0 : 1;

                int del = dp[i - 1][j] + 1;
                int ins = dp[i][j - 1] + 1;
                int sub = dp[i - 1][j - 1] + cost;

                int val = Math.min(Math.min(del, ins), sub);

                // transposición
                if (i > 1 && j > 1 && cs == t.charAt(j - 2) && s.charAt(i - 2) == ct) {
                    val = Math.min(val, dp[i - 2][j - 2] + 1);
                }
                dp[i][j] = val;
            }
        }
        return dp[n][m];
    }
}
