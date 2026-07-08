package com.admin.base.utils.Boyer;

import java.util.*;

public class BoyerMoore {

    public static List<Integer> boyerMoore(String text, String pattern) {
        List<Integer> res = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m == 0) {
            res.add(0);
            return res;
        }
        Map<Character, Integer> last = new HashMap<>();
        for (int i = 0; i < m; i++) {
            last.put(pattern.charAt(i), i);
        }
        int i = m - 1;
        int k = m - 1;
        while (i < n) {
            if (text.charAt(i) == pattern.charAt(k)) {
                if (k == 0) {
                    res.add(i);
                    i += m;
                    k = m - 1;
                } else {
                    i--;
                    k--;
                }
            } else {
                int j = last.getOrDefault(text.charAt(i), -1);
                i += m - Math.min(k, j + 1);
                k = m - 1;
            }
        }
        return res;
    }
}
