package cn.com.start.cloudprinter.startcloudprinter.handler;

import android.util.Log;

import cn.com.start.cloudprinter.startcloudprinter.handler.netty.DeviceOrder;
import cn.com.start.cloudprinter.startcloudprinter.order.PrinterOrder;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;
import io.netty.channel.ChannelHandlerContext;

public class HeartBeatHandler extends AbsHandler {

    private final static String TAG = HeartBeatHandler.class.getSimpleName();

    @Override
    protected boolean handle(ChannelHandlerContext channelHandlerContext, DeviceOrder deviceOrder) {

        if (deviceOrder.getOrderType()[0] != PrinterOrder.HEARTBEAT.getOrder()[0]){
            return false;
        }

        Log.d(TAG, "receive server send heart beat");
//        Log.d(TAG, "heart beat is " + ToolUtil.byte2HexStr(deviceOrder.combine()));

        return true;
    }
}
