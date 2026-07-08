//package com.admin.base.utils;
//
//import com.aspose.words.Document;
//import com.aspose.words.License;
//import com.aspose.words.SaveFormat;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
///**
// * @author ZXX
// * @version 1.0
// * @date 2023/4/22 11:33
// * @desc
// */
//public class WordToPdfAsposeUtil {
//    private static final Logger logger = LoggerFactory.getLogger(WordToPdfAsposeUtil.class);
//
//    /**
//     * 获取aspose证书
//     * @auther: YSL
//     * @date: 2022/8/2 12:10
//     * @return boolean
//     */
//    private static boolean getLicense() {
//        boolean result = false;
//        InputStream is = null;
//        try {
//            Resource resource = new ClassPathResource("license.xml");
//            is = resource.getInputStream();
//            License aposeLic = new License();
//            aposeLic.setLicense(is);
//            result = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }finally {
//            if (is != null) {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return result;
//    }
//
//    /**
//     * word转pdf静态方法
//     * @auther: YSL
//     * @date: 2022/8/2 12:08
//     * @param inPath word文件全路径含文件名
//     * @param outPath pdf输出全路径含文件名
//     * @return boolean
//     */
//    public static boolean docToPdf(String inPath, String outPath) {
//        // 验证License 若不验证则转化出的pdf文档会有水印产生
//        if (!getLicense()) {
//            return false;
//        }
//        FileOutputStream os = null;
//        try {
//            long old = System.currentTimeMillis();
//            // 新建一个空白pdf文档
//            File file = new File(outPath);
//            os = new FileOutputStream(file);
//            // inPath是将要被转化的word文档
//            Document doc = new Document(inPath);
//
//            // 全面支持DOC, DOCX, OOXML, RTF HTML, OpenDocument, PDF,EPUB, XPS, SWF 相互转换
//            doc.save(os, SaveFormat.PDF);
//            long now = System.currentTimeMillis();
//            // 转化用时
//            logger.info("word转换pdf成功，共耗时：" + ((now - old) / 1000.0) + "秒");
//        } catch (Exception e) {
//            logger.info("word转pdf失败",e);
//            return false;
//        }finally {
//            if (os != null) {
//                try {
//                    os.flush();
//                    os.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return true;
//    }
//
//    public static void main(String[] args) {
//        final boolean b = docToPdf("/Users/zhaoxin/Desktop/隐水印/word/附表2：任务书.doc", "/Users/zhaoxin/Desktop/隐水印/pdf/附表2：任务书.pdf");
//        System.out.println(b);
//    }
//}
