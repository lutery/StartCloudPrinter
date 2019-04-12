package cn.com.start.cloudprinter.startcloudprinter.handler;

import android.util.Log;

import cn.com.start.cloudprinter.startcloudprinter.handler.netty.DeviceOrder;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;
import io.netty.channel.ChannelHandlerContext;

public class UnknownOrderHandler extends AbsHandler {

    private final static String TAG = UnknownOrderHandler.class.getSimpleName();

    @Override
    protected boolean handle(ChannelHandlerContext channelHandlerContext, DeviceOrder deviceOrder) {

        Log.d(TAG, "##############receive unknown data from server##############");
        Log.d(TAG, ToolUtil.byte2HexStr(deviceOrder.combine()));
        Log.d(TAG, "##############receive unknown data from server##############");

        return true;
    }
}
