package cn.com.start.cloudprinter.startcloudprinter.tool.verify.impl;

import cn.com.startprinter.serverprinter2.exception.runtime.VerifyException;
import cn.com.startprinter.serverprinter2.tool.verify.IVerify;
import cn.com.startprinter.serverprinter2.tool.verify.VerifyType;
import cn.com.startprinter.serverprinter2.util.GeneraUtil;

import java.security.NoSuchAlgorithmException;

public class MD5Verify implements IVerify {
    @Override
    public boolean verifyContent(byte[] verifyCode, byte[] verifyContent) throws VerifyException {
        boolean verifyRes = false;

        try {
            byte[] infoMd5 = GeneraUtil.bytesToMD5(verifyContent);

            for (int i = 0; i < infoMd5.length; ++i){
                if (verifyCode[i] != infoMd5[i]){
                    throw new VerifyException("MD5码不匹配");
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
        byte[] md5Code = new byte[16];

        do{
            if (instructCode.length < 25){
                break;
            }

            for (int i = 0; i < 16; ++i){
                md5Code[i] = instructCode[i + 9];
            }

        }while (false);

        return md5Code;
    }

    @Override
    public byte[] generateVerifyCode(byte[] content) throws NoSuchAlgorithmException {

        return GeneraUtil.bytesToMD5(content);
    }

    @Override
    public VerifyType verifyType() {
        return VerifyType.MD5;
    }
}
