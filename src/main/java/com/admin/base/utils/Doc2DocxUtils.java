//package com.admin.base.utils;
//
//import com.aspose.words.License;
//import com.aspose.words.SaveFormat;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//import org.springframework.util.FileCopyUtils;
//
//import java.io.*;
//
///**
// * @author ZXX
// * @version 1.0
// * @date 2023/4/22 23:10
// */
//public class Doc2DocxUtils {
//
//
//    public static void main(String[] args) throws Exception {
////        getLicense();
//        String srcfile = "/Users/zhaoxin/Desktop/赵鑫鑫个人资料/毕业设计/附表4：开题报告.doc";
//        byteArrayToFile(convertDocIs2DocxIs(new FileInputStream(new File(srcfile))), "/Users/zhaoxin/Desktop/赵鑫鑫个人资料/毕业设计/附表4：开题报告2.docx");
//    }
//
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
//        } finally {
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
//    // 字节数组到文件的过程
//    public static void byteArrayToFile(byte[] data, String newFileNmae) {
//        getLicense();
//        File file = new File(newFileNmae);
//        //选择流
//        FileOutputStream fos = null;
//        ByteArrayInputStream bais;
//        try {
//            bais = new ByteArrayInputStream(data);
//            fos = new FileOutputStream(file);
//            int temp;
//            byte[] bt = new byte[1024 * 10];
//            while ((temp = bais.read(bt)) != -1) {
//                fos.write(bt, 0, temp);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            //关流
//            try {
//                if (null != fos) {
//                    fos.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * 将doc输入流转换为docx输入流
//     *
//     * @param docInputStream
//     * @return
//     * @throws IOException
//     */
//    public static byte[] convertDocIs2DocxIs(InputStream docInputStream) throws IOException {
//        byte[] docBytes = FileCopyUtils.copyToByteArray(docInputStream);
//        return convertDocStream2docxStream(docBytes);
//    }
//
//    /**
//     * 将doc字节数组转换为docx字节数组
//     *
//     * @param arrays
//     * @return
//     */
//    private static byte[] convertDocStream2docxStream(byte[] arrays) {
//        byte[] docxBytes = new byte[1];
//        if (arrays != null && arrays.length > 0) {
//            try (
//                    ByteArrayOutputStream os = new ByteArrayOutputStream();
//                    InputStream sbs = new ByteArrayInputStream(arrays)
//            ) {
//                com.aspose.words.Document doc = new com.aspose.words.Document(sbs);
//                doc.save(os, SaveFormat.DOCX);
//                docxBytes = os.toByteArray();
//            } catch (Exception e) {
//                System.out.println("出错啦");
//            }
//        }
//        return docxBytes;
//    }
//}
