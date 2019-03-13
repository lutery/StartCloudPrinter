package cn.com.start.cloudprinter.startcloudprinter.po;

import lombok.Data;

@Data
public class OrderObj {

    private byte[] order;
    private byte[] length;
    private byte verityType;
    private byte[] verityCode;
    private byte[] content;
    private byte end;
}
