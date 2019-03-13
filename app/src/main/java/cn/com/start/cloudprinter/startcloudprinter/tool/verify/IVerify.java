package cn.com.start.cloudprinter.startcloudprinter.tool.verify;

import java.security.NoSuchAlgorithmException;

public interface IVerify {

    boolean verifyContent(byte[] verifyCode, byte[] verifyContent);
    byte[] extractVerifyCode(byte[] instructCode);
    byte[] generateVerifyCode(byte[] content) throws NoSuchAlgorithmException;
    VerifyType verifyType();
}
