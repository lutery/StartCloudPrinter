package cn.com.start.cloudprinter.startcloudprinter.handler;

import android.util.Log;

import cn.com.start.cloudprinter.startcloudprinter.handler.netty.DeviceOrder;
import io.netty.channel.ChannelHandlerContext;

public class HeartBeatHandler extends AbsHandler {

    private final static String TAG = HeartBeatHandler.class.getSimpleName();

    @Override
    protected boolean handle(ChannelHandlerContext channelHandlerContext, DeviceOrder deviceOrder) {

        if (deviceOrder.getOrderType()[0] != (byte)0xff){
            return false;
        }

        Log.d(TAG, "receive server send heart beat");

        return true;
    }
}
