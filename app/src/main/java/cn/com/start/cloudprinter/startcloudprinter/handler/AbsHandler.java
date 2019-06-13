package cn.com.start.cloudprinter.startcloudprinter.handler;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.com.start.cloudprinter.startcloudprinter.PrinterService;
import cn.com.start.cloudprinter.startcloudprinter.event.ExceptionEvent;
import cn.com.start.cloudprinter.startcloudprinter.handler.netty.DeviceOrder;
import cn.com.start.cloudprinter.startcloudprinter.po.OrderObj;
import cn.com.start.cloudprinter.startcloudprinter.tool.verify.IVerify;
import cn.com.start.cloudprinter.startcloudprinter.tool.verify.VerifyType;
import cn.com.start.cloudprinter.startcloudprinter.tool.verify.impl.CRC16CCITTVerify;
import cn.com.start.cloudprinter.startcloudprinter.tool.verify.impl.CRC16XModemVerify;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

/**
 * Created by lutery on 2017/12/25.
 */

@Data
public abstract class AbsHandler {

    private static final String TAG = AbsHandler.class.getSimpleName();

    protected AbsHandler mNextHandler;
    protected InputStream inputStream;
    protected IVerify mVerifyTool = new CRC16CCITTVerify();
    protected Channel mChannel;

    public final void handleRequest(ChannelHandlerContext channelHandlerContext, DeviceOrder deviceOrder){
        Log.d(TAG, "handle request");

        mChannel = PrinterService.getChannel();
        if (this.handle(channelHandlerContext, deviceOrder)){
            return;
        }

        if (this.mNextHandler != null){
            mNextHandler.handleRequest(channelHandlerContext, deviceOrder);
        }
    }

    protected abstract boolean handle(ChannelHandlerContext channelHandlerContext, DeviceOrder deviceOrder);
}
