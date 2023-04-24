package com.shinbash.tpk.utils;

import java.io.UnsupportedEncodingException;

public class DataConvert {

    static final byte[] HEX_CHAR_TABLE = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};

    public DataConvert() {
    }

    public static byte[] hexStringToByteArray(String hexString) {
        int length = hexString.length();
        byte[] buffer = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            buffer[i / 2] = (byte) ((toByte2(hexString.charAt(i)) << 4) | toByte2(hexString.charAt(i + 1)));
        }

        return buffer;
    }

    private static int toByte2(char c) {
        if (c >= '0' && c <= '9') return (c - '0');
        if (c >= 'A' && c <= 'F') return (c - 'A' + 10);
        System.out.println("toByte2="+(c - 'a' + 10));
        if (c >= 'a' && c <= 'f') return (c - 'a' + 10);


        throw new RuntimeException("Invalid hex char '" + c + "'");
    }

    public static String bcdToStr(byte[] var0) {
        StringBuffer var1 = new StringBuffer(var0.length * 2);

        for (int var2 = 0; var2 < var0.length; ++var2) {
            var1.append((byte) ((var0[var2] & 240) >>> 4));
            var1.append((byte) (var0[var2] & 15));
        }

        String var3;
        if (var1.toString().substring(0, 1).equalsIgnoreCase("0")) {
            var3 = var1.toString().substring(1);
        } else {
            var3 = var1.toString();
        }

        return var3;
    }

    public static Integer bytesToInt(byte[] src, int offset) {
        Integer value;

        value = (Integer) ((src[offset] & 0xff) << 24 | (src[offset + 1] & 0xff) << 16 | (src[offset + 2] & 0xff) << 8 | (src[offset + 3] & 0xff));
        System.out.println("value======" + value);//

        byte[] result = toByteArray(value);
        System.out.println("result======" + bytesToHexString(result));//

        return value;
    }

    public static byte[] toByteArray(int num) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((num >> 24) & 0xff);
        bytes[1] = (byte) ((num >> 16) & 0xff);
        bytes[2] = (byte) ((num >> 8) & 0xff);
        bytes[3] = (byte) (num & 0xff);
        return bytes;
    }

    private static String byteToString(byte var0) {
        byte var1 = (byte) ((var0 & -16) >> 4);
        byte var2 = (byte) (var0 & 15);
        StringBuffer var3 = new StringBuffer();
        var3.append(findHex(var1));
        var3.append(findHex(var2));
        return var3.toString();
    }



    public static String bytesToHexString(byte[] var0) {
        StringBuilder var1 = new StringBuilder("");
        String var4;
        if (var0 != null && var0.length > 0) {
            for (int var2 = 0; var2 < var0.length; ++var2) {
                String var3 = Integer.toHexString(var0[var2] & 255);
                if (var3.length() < 2) {
                    var1.append(0);
                }

                var1.append(var3);
            }

            var4 = var1.toString();
        } else {
            var4 = null;
        }

        return var4;
    }

    private static byte charToByte(char var0) {
        return (byte) "0123456789ABCDEF".indexOf(var0);
    }

    public static byte[] convertBytePointer(byte[] var0) {
        byte[] var1 = new byte[var0.length];
        int var2 = 0;

        for (int var3 = var0.length - 1; var2 < var0.length; --var3) {
            var1[var2] = (byte) var0[var3];
            ++var2;
        }

        return var1;
    }

    public static String convertByteToString(byte[] var0) {
        StringBuffer var1 = new StringBuffer();

        for (int var2 = 0; var2 < var0.length; ++var2) {
            var1.append(byteToString(var0[var2]));
        }

        return var1.toString();
    }

    private static char findHex(byte var0) {
        int var1 = (new Byte(var0)).intValue();
        int var2 = var1;
        if (var1 < 0) {
            var2 = var1 + 16;
        }

        char var3;
        char var4;
        if (var2 >= 0 && var2 <= 9) {
            var4 = (char) (var2 + 48);
            var3 = var4;
        } else {
            var4 = (char) (var2 - 10 + 65);
            var3 = var4;
        }

        return var3;
    }

    public static String getHexString(byte[] var0) {
        byte var1 = 0;
        byte[] var2 = new byte[8];
        System.arraycopy(var0, 0, var2, 0, var0.length);

        int var3;
        for (var3 = 0; var3 < 8; ++var3) {
            var0[var3] = (byte) var2[7 - var3];
        }

        var2 = new byte[var0.length * 2];
        int var4 = var0.length;
        byte var5 = 0;
        var3 = var1;

        for (int var9 = var5; var3 < var4; ++var3) {
            int var10 = var0[var3] & 255;
            int var6 = var9 + 1;
            var2[var9] = (byte) HEX_CHAR_TABLE[var10 >>> 4];
            var9 = var6 + 1;
            var2[var6] = (byte) HEX_CHAR_TABLE[var10 & 15];
        }

        String var8;
        try {
            var8 = new String(var2, "ASCII");
        } catch (UnsupportedEncodingException var7) {
            var7.printStackTrace();
            var8 = null;
        }

        return var8;
    }

    public static String bytesToString(byte[] data) {
        StringBuffer s = new StringBuffer();
        ;
        for (int i = 1; i < data.length; i++) {
            String hex = Integer.toHexString(data[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            s.append(hex);
        }
        return s.toString();
    }

    public static byte[] hexStringToBytes(String var0) {
        byte[] var6;
        if (var0 != null && !var0.equals("")) {
            var0 = var0.toUpperCase();
            int var1 = var0.length() / 2;
            char[] var2 = var0.toCharArray();
            byte[] var3 = new byte[var1];
            int var4 = 0;

            while (true) {
                var6 = var3;
                if (var4 >= var1) {
                    break;
                }

                int var5 = var4 * 2;
                var3[var4] = (byte) ((byte) (charToByte(var2[var5]) << 4 | charToByte(var2[var5 + 1])));
                ++var4;
            }
        } else {
            var6 = null;
        }

        return var6;
    }

    public static byte[] intToBytes(int var0) {
        byte var1 = (byte) (var0 & 255);
        byte var2 = (byte) (var0 >> 8 & 255);
        byte var3 = (byte) (var0 >> 16 & 255);
        return new byte[]{(byte) (var0 >>> 24), var3, var2, var1};
    }

    public static void main(String[] var0) {
        var0 = new String[]{"A28E8900000081E0", "B3888900000081E0", "848A8900000081E0", "83928900000081E0", "83928900000081E0", "96908900000081E0", "95888900000081E0", "75368800000081E0", "7D368800000081E0", "573C8800000081E0"};

        for (int var1 = 0; var1 < var0.length; ++var1) {
            System.out.println(bytesToHexString(hexStringToBytes(var0[var1])));
        }

    }
}
