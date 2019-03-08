package cn.com.start.cloudprinter.startcloudprinter.event;

import lombok.Data;

/**
 * Created by lutery on 2017/11/29.
 */
@Data
public class PrintEvent {

    private byte[] order;
    private byte[] length;
    private byte[] verifyCode;
    private byte[] data;
    private String msg;
}
