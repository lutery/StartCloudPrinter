package cn.com.start.cloudprinter.startcloudprinter.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.zip.CRC32;

import chchy.one.checkpermission.PermissionCheck;
import chchy.one.checkpermission.PermissionResult;
import cn.com.start.cloudprinter.startcloudprinter.tool.verify.IVerify;
import cn.com.start.cloudprinter.startcloudprinter.tool.verify.VerifyType;

/**
 * Created by 程辉 on 2017/11/23.
 */

public class ToolUtil {

    private static final String TAG = ToolUtil.class.getSimpleName();

    public static byte[] getResultMsg(String result, IVerify verifyTool) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);


        try {
//            String msg = String.format("{\"result\":\"%s\"}", result);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("result", result);
            String msg = jsonObject.toString();

            byteBuffer.put(ByteBuffer.wrap(new byte[]{0x0f, 0x00, 0x00, 0x00}));
            byteBuffer.put(ByteBuffer.wrap(ToolUtil.intToBytes(msg.getBytes("gb18030").length)));
            byteBuffer.put(ByteBuffer.wrap(new byte[]{0x03}));
            byteBuffer.put(ByteBuffer.wrap(verifyTool.generateVerifyCode(msg.getBytes("gb18030"))));
            byteBuffer.put(ByteBuffer.wrap(msg.getBytes("gb18030")));
            byteBuffer.put(ByteBuffer.wrap(new byte[]{0x24}));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        byteBuffer.flip();

        byte[] resultMsgBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(resultMsgBytes);

        return resultMsgBytes;
    }

    public static int getVerifyTypeLength(Byte type){

        if (type == VerifyType.CRC8.getType()){
            return 1;
        }
        else if (type == VerifyType.CRC16.getType() ||
        type == VerifyType.CRCCCITT.getType()){
            return 2;
        }
        else if (type == VerifyType.CRC32.getType()){
            return 4;
        }
        else if (type == VerifyType.MD5.getType()){
            return 16;
        }

        return Integer.MAX_VALUE / 2;
    }


    public static String getUniqueId(Context context) {
        final SharedPreferences sp = context.getSharedPreferences(ConstantUtil.APP_INFO, Context.MODE_PRIVATE);

        String deviceid = sp.getString(ConstantUtil.DEVICE_ID, "");

        if (deviceid.length() <= 0){
            deviceid = ToolUtil.toMD5(UUID.randomUUID().toString());

            sp.edit().putString(ConstantUtil.DEVICE_ID, deviceid).commit();
        }

        return deviceid;
    }

    public static byte[] long2ByteArray(long lVal){
        byte[] bVals = new byte[8];

        bVals[0] = (byte)((lVal >> 56) & 0xff);
        bVals[1] = (byte)((lVal >> 48) & 0xff);
        bVals[2] = (byte)((lVal >> 40) & 0xff);
        bVals[3] = (byte)((lVal >> 32) & 0xff);
        bVals[4] = (byte)((lVal >> 24) & 0xff);
        bVals[5] = (byte)((lVal >> 16) & 0xff);
        bVals[6] = (byte)((lVal >> 8) & 0xff);
        bVals[7] = (byte)((lVal >> 0) & 0xff);

        return bVals;
    }

    public static byte[] longToBytes(long iVal){
        byte[] bVals = new byte[4];

        bVals[0] = (byte)((iVal >> 24) & 0xff);
        bVals[1] = (byte)((iVal >> 16) & 0xff);
        bVals[2] = (byte)((iVal >> 8) & 0xff);
        bVals[3] = (byte)((iVal >> 0) & 0xff);

        return bVals;
    }

    public static byte[] intToBytes(int iVal){
        byte[] bVals = new byte[4];

        bVals[0] = (byte)((iVal >> 24) & 0xff);
        bVals[1] = (byte)((iVal >> 16) & 0xff);
        bVals[2] = (byte)((iVal >> 8) & 0xff);
        bVals[3] = (byte)((iVal >> 0) & 0xff);

        return bVals;
    }

    /**
     * byte[] 转 int，低位在前，高位在后
     * @param bs
     * @return
     */
    public static int byteArray2Int(byte... bs){
        byte[] byteArray = new byte[4];

        for (int i = 0; i < bs.length && i < 4; ++i){
            byteArray[i] = bs[i];
        }

        return byteArray[0] & 0xff |
                (byteArray[1] & 0xff) << 8 |
                (byteArray[2] & 0xff) << 16 |
                (byteArray[3] & 0xff) << 24;
    }

    public static byte[] toCRC32Bytes(byte[] bytes){
        CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        long crc32Val = crc32.getValue();
        Log.d(TAG, "crc32Val = " + crc32Val);

        return longToBytes(crc32Val);
    }

//    public static void main(String[] argc){
//        byte[] tmp = toCRC16Bytes("121345".getBytes());
//        System.out.print(tmp);
//    }

    public static byte[] toCRC16Bytes(byte[] bytes){
        char crctable[] = { 0x0000, 0x1021, 0x2042, 0x3063,
                0x4084, 0x50a5, 0x60c6, 0x70e7, 0x8108, 0x9129, 0xa14a, 0xb16b,
                0xc18c, 0xd1ad, 0xe1ce, 0xf1ef, 0x1231, 0x0210, 0x3273, 0x2252,
                0x52b5, 0x4294, 0x72f7, 0x62d6, 0x9339, 0x8318, 0xb37b, 0xa35a,
                0xd3bd, 0xc39c, 0xf3ff, 0xe3de, 0x2462, 0x3443, 0x0420, 0x1401,
                0x64e6, 0x74c7, 0x44a4, 0x5485, 0xa56a, 0xb54b, 0x8528, 0x9509,
                0xe5ee, 0xf5cf, 0xc5ac, 0xd58d, 0x3653, 0x2672, 0x1611, 0x0630,
                0x76d7, 0x66f6, 0x5695, 0x46b4, 0xb75b, 0xa77a, 0x9719, 0x8738,
                0xf7df, 0xe7fe, 0xd79d, 0xc7bc, 0x48c4, 0x58e5, 0x6886, 0x78a7,
                0x0840, 0x1861, 0x2802, 0x3823, 0xc9cc, 0xd9ed, 0xe98e, 0xf9af,
                0x8948, 0x9969, 0xa90a, 0xb92b, 0x5af5, 0x4ad4, 0x7ab7, 0x6a96,
                0x1a71, 0x0a50, 0x3a33, 0x2a12, 0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e,
                0x9b79, 0x8b58, 0xbb3b, 0xab1a, 0x6ca6, 0x7c87, 0x4ce4, 0x5cc5,
                0x2c22, 0x3c03, 0x0c60, 0x1c41, 0xedae, 0xfd8f, 0xcdec, 0xddcd,
                0xad2a, 0xbd0b, 0x8d68, 0x9d49, 0x7e97, 0x6eb6, 0x5ed5, 0x4ef4,
                0x3e13, 0x2e32, 0x1e51, 0x0e70, 0xff9f, 0xefbe, 0xdfdd, 0xcffc,
                0xbf1b, 0xaf3a, 0x9f59, 0x8f78, 0x9188, 0x81a9, 0xb1ca, 0xa1eb,
                0xd10c, 0xc12d, 0xf14e, 0xe16f, 0x1080, 0x00a1, 0x30c2, 0x20e3,
                0x5004, 0x4025, 0x7046, 0x6067, 0x83b9, 0x9398, 0xa3fb, 0xb3da,
                0xc33d, 0xd31c, 0xe37f, 0xf35e, 0x02b1, 0x1290, 0x22f3, 0x32d2,
                0x4235, 0x5214, 0x6277, 0x7256, 0xb5ea, 0xa5cb, 0x95a8, 0x8589,
                0xf56e, 0xe54f, 0xd52c, 0xc50d, 0x34e2, 0x24c3, 0x14a0, 0x0481,
                0x7466, 0x6447, 0x5424, 0x4405, 0xa7db, 0xb7fa, 0x8799, 0x97b8,
                0xe75f, 0xf77e, 0xc71d, 0xd73c, 0x26d3, 0x36f2, 0x0691, 0x16b0,
                0x6657, 0x7676, 0x4615, 0x5634, 0xd94c, 0xc96d, 0xf90e, 0xe92f,
                0x99c8, 0x89e9, 0xb98a, 0xa9ab, 0x5844, 0x4865, 0x7806, 0x6827,
                0x18c0, 0x08e1, 0x3882, 0x28a3, 0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e,
                0x8bf9, 0x9bd8, 0xabbb, 0xbb9a, 0x4a75, 0x5a54, 0x6a37, 0x7a16,
                0x0af1, 0x1ad0, 0x2ab3, 0x3a92, 0xfd2e, 0xed0f, 0xdd6c, 0xcd4d,
                0xbdaa, 0xad8b, 0x9de8, 0x8dc9, 0x7c26, 0x6c07, 0x5c64, 0x4c45,
                0x3ca2, 0x2c83, 0x1ce0, 0x0cc1, 0xef1f, 0xff3e, 0xcf5d, 0xdf7c,
                0xaf9b, 0xbfba, 0x8fd9, 0x9ff8, 0x6e17, 0x7e36, 0x4e55, 0x5e74,
                0x2e93, 0x3eb2, 0x0ed1, 0x1ef0 };

        char crc = 0x0000;
        for (byte b : bytes) {
            crc = (char) ((crc << 8) ^ crctable[((crc >> 8) ^ b) & 0x00ff]);
        }

        byte[] crcBytes = new byte[2];

        crcBytes[0] = (byte) ((crc >> 8) & 0xff);
        crcBytes[1] = (byte)(crc & 0xff);

        return crcBytes;
    }

    public static byte[] toMD5Bytes(byte[] bytes){
        byte[] digest = null;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            digest = messageDigest.digest(bytes);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return digest;
    }

    public static String toMD5(String text) {
        StringBuilder strBuilder = new StringBuilder();

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digest = messageDigest.digest(text.getBytes());

            for (int i = 0; i < digest.length; ++i){
                int digestInt = digest[i] & 0xff;

                String hexStr = Integer.toHexString(digestInt);

                if (hexStr.length() < 2){
                    strBuilder.append(0);
                }

                strBuilder.append(hexStr);
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return strBuilder.toString();
    }

    public static String getSerial(Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            return Build.SERIAL;
        } else {
            return getSerial26Hight(context);
        }
    }

    @TargetApi(26)
    public static String getSerial26Hight(Context context) {
        if (PermissionCheck.checkPermission(context, Manifest.permission.READ_PHONE_STATE, new PermissionResult() {
            @Override
            public void permissionResult(int resultCode) {

            }
        })){
            return Build.getSerial();
        }

        return "";
    }

    /**
     * 字节数组转16进制字符串
     * @param b
     * @return
     */
    public static String byte2HexStr(byte[] b){
//        String hexStr = "";
//        String byteStr = "";
//
//        for (int n = 0; n < b.length; ++n){
//            byteStr = (Integer.toHexString(b[n] & 0xff));
//            if (byteStr.length() == 1){
//                hexStr = hexStr + "0" + byteStr;
//            }
//            else{
//                hexStr = hexStr + byteStr;
//            }
//        }
//
//        return hexStr.toLowerCase();
        return ToolUtil.byte2HexStr(b, 0, b.length);
    }

    /**
     * 将字节数组转换为16进制字符串
     * @param b 字节数组
     * @param off 偏移
     * @param len 长度
     * @return
     */
    public static String byte2HexStr(byte[] b, int off, int len){
        String hexStr = "";
        String byteStr = "";

        for (int n = off, count = off + len; n < count; ++n){
            byteStr = (Integer.toHexString(b[n] & 0xff));
            if (byteStr.length() == 1){
                hexStr = hexStr + "0" + byteStr;
            }
            else{
                hexStr = hexStr + byteStr;
            }
        }

        return hexStr.toLowerCase();
    }
}
