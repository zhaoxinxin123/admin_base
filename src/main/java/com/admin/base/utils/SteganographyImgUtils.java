package com.admin.base.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import cn.hutool.extra.qrcode.BufferedImageLuminanceSource;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class SteganographyImgUtils {

    // 默认是黑色
    private static final int QRCOLOR = 0xFF000000;
    // 背景颜色
    private static final int BGWHITE = 0xFFFFFFFF;
    // 是绿色
    public static final int QRCOLOR_GREEN = 0x05A01D;

    // 二维码宽
    private static final int WIDTH = 400;
    // 二维码高
    private static final int HEIGHT = 400;

    /**
     * 用于设置QR二维码参数
     */
    private static final Map<EncodeHintType, Object> hints = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = 1L;

        {
            // 设置QR二维码的纠错级别（H为最高级别）具体级别信息
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            // 设置编码方式
            put(EncodeHintType.CHARACTER_SET, "utf-8");
            put(EncodeHintType.MARGIN, 0);
        }
    };

    /**
     * 根据传入的字符串生成一个简单的二维码
     */
    public static void drawSimpleQRCode(String qrContent, File outputFile) throws WriterException, IOException{
        BitMatrix bitMatrix = new MultiFormatWriter().encode(qrContent,BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
        BufferedImage image = toBufferedImage(bitMatrix);
        ImageIO.write(image, "jpg", outputFile);
    }

    private static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }


    /**
     * 生成带logo的二维码图片
     * @param imageFile 二维码中心小图标
     * @param outputFile 生成的二维码文件
     * @param content 二维码内容
     * @param note  二维码底部文字描述
     * @param QRCOLOR 二维码背景颜色
     */
    public static void drawImageQRCode(File imageFile, File outputFile, String content, String note,int QRCOLOR) throws Exception{
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        // 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
        BitMatrix bm = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);

        // 开始利用二维码数据创建Bitmap图片，分别设为黑（0xFFFFFFFF）白（0xFF000000）两色
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                image.setRGB(x, y, bm.get(x, y) ? QRCOLOR : BGWHITE);
            }
        }

        int width = image.getWidth();
        int height = image.getHeight();
        if (imageFile.exists()) {
            // 构建绘图对象
            Graphics2D g = image.createGraphics();
            // 读取Logo图片
            BufferedImage logo = ImageIO.read(imageFile);
            // 开始绘制logo图片
            g.drawImage(logo, width * 2 / 5, height * 2 / 5, width * 2 / 10, height * 2 / 10, null);
            g.dispose();
            logo.flush();
        }

        // 自定义文本描述
        if (note != null && !note.isEmpty()) {
            // 新的图片，把带logo的二维码下面加上文字
            BufferedImage outImage = new BufferedImage(400, 445, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D outg = outImage.createGraphics();
            // 画二维码到新的面板
            outg.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
            // 画文字到新的面板
            outg.setColor(Color.BLACK);
            outg.setFont(new Font("楷体", Font.BOLD, 30)); // 字体、字型、字号
            int strWidth = outg.getFontMetrics().stringWidth(note);
            if (strWidth > 399) {
                // //长度过长就截取前面部分
                // 长度过长就换行
                String note1 = note.substring(0, note.length() / 2);
                String note2 = note.substring(note.length() / 2);
                int strWidth1 = outg.getFontMetrics().stringWidth(note1);
                int strWidth2 = outg.getFontMetrics().stringWidth(note2);
                outg.drawString(note1, 200 - strWidth1 / 2, height + (outImage.getHeight() - height) / 2 + 12);
                BufferedImage outImage2 = new BufferedImage(400, 485, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D outg2 = outImage2.createGraphics();
                outg2.drawImage(outImage, 0, 0, outImage.getWidth(), outImage.getHeight(), null);
                outg2.setColor(Color.BLACK);
                outg2.setFont(new Font("宋体", Font.BOLD, 30)); // 字体、字型、字号
                outg2.drawString(note2, 200 - strWidth2 / 2, outImage.getHeight() + (outImage2.getHeight() - outImage.getHeight()) / 2 + 5);
                outg2.dispose();
                outImage2.flush();
                outImage = outImage2;
            } else {
                outg.drawString(note, 200 - strWidth / 2, height + (outImage.getHeight() - height) / 2 + 12); // 画文字
            }
            outg.dispose();
            outImage.flush();
            image = outImage;
        }

        image.flush();

        ImageIO.write(image, "png", outputFile);
    }

    /**
     * 识别二维码
     */
    public static void QRReader(File file) throws IOException, NotFoundException {
        MultiFormatReader formatReader = new MultiFormatReader();
        //读取指定的二维码文件
        BufferedImage bufferedImage =ImageIO.read(file);
        BinaryBitmap binaryBitmap= new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));
        //定义二维码参数
        Map<EncodeHintType, Object> hints= new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        Map<DecodeHintType, Object> decodeHints = new HashMap<>();
        for (Map.Entry<EncodeHintType, Object> entry : hints.entrySet()) {
            DecodeHintType decodeKey = DecodeHintType.valueOf(entry.getKey().name());
            decodeHints.put(decodeKey, entry.getValue());
        }
        Result result = formatReader.decode(binaryBitmap, decodeHints);
        //输出相关的二维码信息
        System.out.println("解析结果："+result.toString());
        System.out.println("二维码格式类型："+result.getBarcodeFormat());
        System.out.println("二维码文本内容："+result.getText());
        bufferedImage.flush();
    }

    public static void main(String[] args)throws Exception {
        File outputFile = new File("E:\\pyWork\\images-convert\\static\\extra\\javamark444.jpg");
        QRReader(outputFile);
    }
}
