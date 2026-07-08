package com.admin.base.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SteganographyStringUtils {

    // 隐藏信息到图像中
    public static void hideMessage(BufferedImage image, String message) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int messageLength = message.length();
        int messageIndex = 0;
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                if (messageIndex < messageLength) {
                    int rgb = image.getRGB(x, y);
                    int alpha = (rgb >> 24) & 0xFF;
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    char character = message.charAt(messageIndex);
                    int ascii = (int) character;
                    red = (red & 0xFE) | ((ascii >> 7) & 0x1);
                    green = (green & 0xFE) | ((ascii >> 6) & 0x1);
                    blue = (blue & 0xFE) | ((ascii >> 5) & 0x1);
                    alpha = (alpha & 0xFE) | ((ascii >> 4) & 0x1);
                    image.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
                    messageIndex++;
                } else {
                    return;
                }
            }
        }
    }

    // 从图像中提取信息
    public static String extractMessage(BufferedImage image) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        StringBuilder message = new StringBuilder();
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                int alpha = (rgb >> 24) & 0xFF;
                int ascii = (alpha << 4) | (red >> 7 << 3) | (green >> 6 << 2) | (blue >> 5 << 1);
                if (ascii != 0) {
                    message.append((char) ascii);
                } else {
                    return message.toString();
                }
            }
        }
        return message.toString();
    }

    public static void main(String[] args) throws IOException {
        String message = "Hello World!";
        BufferedImage image = ImageIO.read(new File("image.jpg"));
        hideMessage(image, message);
        ImageIO.write(image, "png", new File("stego-image.png"));
        BufferedImage stegoImage = ImageIO.read(new File("stego-image.png"));
        String extractedMessage = extractMessage(stegoImage);
        System.out.println("Extracted message: " + extractedMessage);
    }
}
