package com.admin.base.utils.Boyer;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        readWord();
    }

    public static void readWord() {
        File file = new File("C:\\Users\\ASUS\\Desktop\\盲水印\\20230308\\设计文档-李嘉宾.docx");
        try {
            FileInputStream fis = new FileInputStream(file);
            XWPFDocument docx = new XWPFDocument(fis);
            List<XWPFParagraph> paragraphs = docx.getParagraphs();
            for (int i = 0; i < paragraphs.size(); i++) {
                FilterResult sensitive_paragraphs = SensitiveWordFilter.find_sensitive_paragraphs(paragraphs.get(i).getText(), Arrays.asList("用户", "登录"),i);
                if (sensitive_paragraphs != null) {
                    System.out.println(sensitive_paragraphs.toString());
                }
            }
            docx.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
