package cn.com.start.cloudprinter.startcloudprinter.util;

import lombok.Getter;

public enum DeviceStatus {

    shortOfPaper(0x48, "缺纸"),
    ready(0x18, "就绪"),
    offline(0x02, "离线"),
    printing(0x03, "正在打印"),
    ;

    @Getter
    int code;
    String msg;
    DeviceStatus(int statusCode, String msg){
        this.code = statusCode;
    }
}
