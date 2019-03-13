package cn.com.start.cloudprinter.startcloudprinter.tool.verify.impl;

import cn.com.startprinter.serverprinter2.exception.runtime.VerifyException;
import cn.com.startprinter.serverprinter2.tool.verify.IVerify;
import cn.com.startprinter.serverprinter2.tool.verify.VerifyType;
import cn.com.startprinter.serverprinter2.util.GeneraUtil;
import cn.com.startprinter.serverprinter2.util.Log;

import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

public class CRC32Verify implements IVerify {

    @Override
    public boolean verifyContent(byte[] verifyCode, byte[] verifyContent) throws VerifyException {
        boolean verifyRes = false;

        try {
            byte[] crc32Code = generateVerifyCode(verifyContent);

            if (verifyCode.length != crc32Code.length){
                return false;
            }

            for (int i = 0; i < verifyCode.length; ++i){
                if (verifyCode[i] != crc32Code[i]){
                    throw new Exception("校验码不匹配，校验失败");
                }
            }

            verifyRes = true;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return verifyRes;
    }

    @Override
    public byte[] extractVerifyCode(byte[] instructCode) {
        byte[] crc32Code = new byte[4];

        do{
            if (instructCode.length < 13){
                break;
            }

            for (int i = 0; i < 4; ++i){
                crc32Code[i] = instructCode[i + 9];
            }

        }while (false);

        return crc32Code;
    }

    @Override
    public byte[] generateVerifyCode(byte[] content) {
        CRC32 crc32 = new CRC32();
        crc32.update(content);
        long crc32Value = crc32.getValue();
        Log.d(CRC32Verify.class.getSimpleName(), "CRC32 Value Long is " + crc32Value);

        byte[] crc32Code = GeneraUtil.long2ByteArray(crc32Value);;
        byte[] rVal = new byte[4];

        for (int i = 0; i < 4; ++i){
            rVal[i] = crc32Code[i + 4];
        }


        return rVal;
    }

    @Override
    public VerifyType verifyType() {
        return VerifyType.CRC32;
    }


}
