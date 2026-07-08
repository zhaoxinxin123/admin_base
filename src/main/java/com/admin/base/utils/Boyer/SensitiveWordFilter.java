package com.admin.base.utils.Boyer;


import java.util.ArrayList;
import java.util.List;

public class SensitiveWordFilter {
    public static FilterResult find_sensitive_paragraphs(String str, List<String> sensitives, Integer number) {
        FilterResult filterResult = new FilterResult();
        filterResult.setParagraphNumber(number);
        filterResult.setParagraphContent(str);
        List<String> result_sensitives = new ArrayList<>();
        String[] paragraphs = str.split("\n\n");
        for (String paragraph : paragraphs) {
            for (String sensitive : sensitives) {
                List<Integer> integers = BoyerMoore.boyerMoore(paragraph, sensitive);
                if (integers.size() > 0) {
                    result_sensitives.add(sensitive);
                }
            }
        }
        if (result_sensitives.size() > 0) {
            filterResult.setSensitives(result_sensitives);
            return filterResult;
        } else {
            return null;
        }

    }
}
