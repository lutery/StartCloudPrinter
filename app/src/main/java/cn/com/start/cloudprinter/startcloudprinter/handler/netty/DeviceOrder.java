package cn.com.start.cloudprinter.startcloudprinter.handler.netty;

import lombok.Getter;

import java.nio.ByteBuffer;

@Getter
public class DeviceOrder {

    private byte[] orderType;
    private byte[] lengthBytes;
    private byte[] verifyType;
    private byte[] verifyCode;
    private byte[] orderContent;
    private byte[] end;

    public DeviceOrder(int typeLength, int lengthByte, int verityLength, int jsonLength){
        this.orderType = new byte[typeLength];
        this.lengthBytes = new byte[lengthByte];
        this.verifyCode = new byte[verityLength];
        this.orderContent = new byte[jsonLength];
        this.end = new byte[1];
        this.verifyType = new byte[1];
    }

    public byte[] combine(){
        ByteBuffer combineOrder = ByteBuffer.allocate(orderType.length + lengthBytes.length
                                                    + verifyType.length + verifyCode.length
                                                    + orderContent.length + end.length);

        combineOrder.put(orderType).put(lengthBytes).put(verifyType).put(verifyCode).put(orderContent).put(end);

        return combineOrder.array();
    }
}
