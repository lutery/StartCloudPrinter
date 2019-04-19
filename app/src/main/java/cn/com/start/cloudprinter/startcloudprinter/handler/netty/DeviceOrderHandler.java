package cn.com.start.cloudprinter.startcloudprinter.handler.netty;

import android.util.Log;

import java.security.NoSuchAlgorithmException;

import cn.com.start.cloudprinter.startcloudprinter.handler.AbsHandler;
import cn.com.start.cloudprinter.startcloudprinter.handler.DevInfoHandler;
import cn.com.start.cloudprinter.startcloudprinter.handler.DevStatusHandler;
import cn.com.start.cloudprinter.startcloudprinter.handler.HeartBeatHandler;
import cn.com.start.cloudprinter.startcloudprinter.handler.PrnDataHandler;
import cn.com.start.cloudprinter.startcloudprinter.handler.PrnDataSaveHandler;
import cn.com.start.cloudprinter.startcloudprinter.handler.PrnInfoHandler;
import cn.com.start.cloudprinter.startcloudprinter.handler.ResultInfoHandler;
import cn.com.start.cloudprinter.startcloudprinter.handler.UnknownOrderHandler;
import cn.com.start.cloudprinter.startcloudprinter.order.PrinterOrder;
import cn.com.start.cloudprinter.startcloudprinter.tool.verify.IVerify;
import cn.com.start.cloudprinter.startcloudprinter.tool.verify.impl.CRC16CCITTVerify;
import cn.com.start.cloudprinter.startcloudprinter.tool.verify.impl.CRC16XModemVerify;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

public class DeviceOrderHandler extends SimpleChannelInboundHandler<DeviceOrder> {

    private final static String TAG = DeviceOrderHandler.class.getSimpleName();

    public AbsHandler mfirstHandler;

    public DeviceOrderHandler(){
        mfirstHandler = new PrnDataHandler();

        AbsHandler devInfoHandler = new DevInfoHandler();
        AbsHandler devStatusHandler = new DevStatusHandler();
        AbsHandler prnDataSavehandler = new PrnDataSaveHandler();
        AbsHandler prnInfoHandler = new PrnInfoHandler();
        AbsHandler resultInfoHandler = new ResultInfoHandler();
        AbsHandler heartBeatHandler = new HeartBeatHandler();

        mfirstHandler.setMNextHandler(devInfoHandler);
        devInfoHandler.setMNextHandler(devStatusHandler);
        devStatusHandler.setMNextHandler(prnDataSavehandler);
        prnDataSavehandler.setMNextHandler(prnInfoHandler);
        prnInfoHandler.setMNextHandler(resultInfoHandler);
        resultInfoHandler.setMNextHandler(heartBeatHandler);
        heartBeatHandler.setMNextHandler(new UnknownOrderHandler());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DeviceOrder deviceOrder) throws Exception {
        mfirstHandler.handleRequest(channelHandlerContext, deviceOrder);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            switch (((IdleStateEvent) evt).state()){
                case ALL_IDLE:
                    this.handleAllIdle(ctx);
                    break;
                case READER_IDLE:
                    this.handleReaderIdle(ctx);
                    break;
                case WRITER_IDLE:
                    this.handleWriterIdle(ctx);
                    break;
                    default:
                        break;
            }
        }

        super.userEventTriggered(ctx, evt);
    }

    protected void sendHeartBeatMsg(ChannelHandlerContext ctx){
        ByteBuf byteBuf = Unpooled.buffer(30);

        byteBuf.writeBytes(new byte[]{(byte)0xff, 0x00, 0x00, 0x00});
        byteBuf.writeBytes(new byte[]{0x00, 0x00, 0x00, 0x00});
        byteBuf.writeBytes(new byte[]{0x03, 0x00, 0x00, 0x24});

        ctx.writeAndFlush(byteBuf);
    }

    protected void handleReaderIdle(ChannelHandlerContext ctx){
        Log.d(TAG, "===Reader_IDLE===");
    }

    protected void handleWriterIdle(ChannelHandlerContext ctx){
        Log.d(TAG, "===Writer_IDLE===");
    }

    protected void handleAllIdle(ChannelHandlerContext ctx){
        Log.d(TAG, "===AllIdle===");
        try {
            ctx.writeAndFlush(headerBeat());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    protected ByteBuf headerBeat() throws NoSuchAlgorithmException {
        ByteBuf byteBuf = Unpooled.buffer(30);

        IVerify iVerify = new CRC16CCITTVerify();

        byteBuf.writeBytes(PrinterOrder.HEARTBEAT.getOrder());
        byteBuf.writeBytes(new byte[]{0x00, 0x00, 0x00, 0x00});
        byteBuf.writeBytes(new byte[]{iVerify.verifyType().getType()});
        byteBuf.writeBytes(iVerify.generateVerifyCode(null));
        byteBuf.writeBytes(new byte[]{0x24});

        return byteBuf;
    }
}
