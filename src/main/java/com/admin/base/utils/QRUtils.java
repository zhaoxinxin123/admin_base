package com.admin.base.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;

import java.util.Objects;

public class QRUtils {
    public static void extraMsg(String path){
        String decode = QrCodeUtil.decode(FileUtil.file(path));
        System.out.println(decode);

    }

    public static void generate(String path){

        QrCodeUtil.generate(path, 60, 60, FileUtil.file(path));

    }

    public static void main(String[] args) {
        String s=String.format("%s:%d", "zxx", 20);
        String s2=String.format("%s:%d", "zxx", 20);
        System.out.println(s==s2);
    }
}
