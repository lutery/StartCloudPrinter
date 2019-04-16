package cn.com.start.cloudprinter.startcloudprinter.tool.verify;

import lombok.Getter;

public enum VerifyType {

    MD5((byte)0x01),
    CRC8((byte)0x02),
    CRC16((byte)0x03),
    CRC32((byte)0x04),
    CRCCCITT((byte)0x05);

    @Getter
    byte type;
    VerifyType(byte type){
        this.type = type;
    }
}
