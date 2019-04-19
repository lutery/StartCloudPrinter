package cn.com.start.cloudprinter.startcloudprinter.handler.netty;

import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.stream.IntStream;

public class DelimiterPrinterOrderFrameDecoder extends ByteToMessageDecoder {

    private int lengthOfOrder;
    private int lengthOfContent;
    private int lengthOfCheck;
    private Byte endByte;

    public DelimiterPrinterOrderFrameDecoder(int lengthOfOrder, int lengthContent, int lengthOfCheck, Byte endByte){
        this.lengthOfOrder = lengthOfOrder;
        this.lengthOfContent = lengthContent;
        this.lengthOfCheck = lengthOfCheck;
        this.endByte = endByte;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        Object order = this.decode(byteBuf);
        if (order != null){
            list.add(order);
        }
    }

    protected Object decode(ByteBuf byteBuf){
        int length = byteBuf.readableBytes();
        int endOfIndex = 0;

        int headerLength = this.lengthOfCheck + this.lengthOfContent + this.lengthOfOrder;

        if (length <= headerLength){
            return null;
        }

        Byte verifyByte = byteBuf.getByte(8);
        int lengthOfVerifyType = ToolUtil.getVerifyTypeLength(verifyByte);

        if (length <= (headerLength + lengthOfVerifyType)){
            return null;
        }

        byte[] lenthBytes = new byte[4];
        byteBuf.getBytes(4, lenthBytes);

        int jsonLength = ToolUtil.byteArray2Int(lenthBytes[3], lenthBytes[2], lenthBytes[1], lenthBytes[0]);

        if (length <= (headerLength + lengthOfVerifyType + jsonLength)){
            return null;
        }

        endOfIndex = headerLength + lengthOfVerifyType + jsonLength;

        if (this.endByte != byteBuf.getByte(endOfIndex)){
            //ToDo 这个问题很严重，得谨慎处理
            return null;
        }

        DeviceOrder deviceOrder = new DeviceOrder(4, 4, lengthOfVerifyType, jsonLength);
        byteBuf.readBytes(deviceOrder.getOrderType());
        byteBuf.readBytes(deviceOrder.getLengthBytes());
        byteBuf.readBytes(deviceOrder.getVerifyType());
        byteBuf.readBytes(deviceOrder.getVerifyCode());
        byteBuf.readBytes(deviceOrder.getOrderContent());
        byteBuf.readBytes(deviceOrder.getEnd());

        return deviceOrder;
    }
}
