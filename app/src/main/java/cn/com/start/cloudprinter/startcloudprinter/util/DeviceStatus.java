package cn.com.start.cloudprinter.startcloudprinter.util;

import lombok.Getter;

public enum DeviceStatus {

    shortOfPaper(0x30, "缺纸"),
    ready(0x18, "就绪"),
    offline(0xfe, "离线"),
    printing(0x03, "正在打印"),
    unknown(0xff, "未知状态"),
    ;

    @Getter
    int code;
    String msg;
    DeviceStatus(int statusCode, String msg){
        this.code = statusCode;
    }
}
