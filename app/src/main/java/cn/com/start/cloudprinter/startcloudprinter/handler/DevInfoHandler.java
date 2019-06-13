package cn.com.start.cloudprinter.startcloudprinter.handler;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import cn.com.start.cloudprinter.startcloudprinter.StartCloudApplication;
import cn.com.start.cloudprinter.startcloudprinter.event.ExceptionEvent;
import cn.com.start.cloudprinter.startcloudprinter.handler.netty.DeviceOrder;
import cn.com.start.cloudprinter.startcloudprinter.order.PrinterOrder;
import cn.com.start.cloudprinter.startcloudprinter.po.OrderObj;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by lutery on 2017/12/26.
 */

public class DevInfoHandler extends AbsHandler {

    private final String TAG = DevInfoHandler.class.getSimpleName();

    @Override
    protected boolean handle(ChannelHandlerContext channelHandlerContext, DeviceOrder deviceOrder) {

        if (deviceOrder.getOrderType()[0] != PrinterOrder.GETDEVINFO.getOrder()[0]) {
            return false;
        }

        String devId = ToolUtil.getUniqueId(StartCloudApplication.getSContext());

        Log.d(TAG, "devId is " + devId);

        JSONObject devJson = new JSONObject();
        try {
            devJson.put("deviceid", devId);
            devJson.put("driver", "ZPL");
            devJson.put("page", "80X200");
            devJson.put("resolution", "203X203");
//            devJson.put("packageSize", 1024 * 1024 * 9);
//            devJson.put("packageSize", 1024);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String devStr = devJson.toString();

        byte[] devInfoBytes = new byte[0];
        try {
            devInfoBytes = devStr.getBytes("gb18030");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] devLength = ToolUtil.intToBytes(devInfoBytes.length);

        byte[] verifyCode = new byte[0];
        try {
            verifyCode = mVerifyTool.generateVerifyCode(devInfoBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "devLength = " + ToolUtil.byte2HexStr(devLength));
        Log.d(TAG, "verifyCode = " + ToolUtil.byte2HexStr(verifyCode));
        Log.d(TAG, "devInfoBytes = " + ToolUtil.byte2HexStr(devInfoBytes));

        ByteBuf byteBuf = Unpooled.buffer(1024);
        byteBuf.writeBytes(PrinterOrder.DEVINFO.getOrder());
        byteBuf.writeBytes(devLength);
        byteBuf.writeBytes(new byte[]{0x05});
        byteBuf.writeBytes(verifyCode);
        byteBuf.writeBytes(devInfoBytes);
        byteBuf.writeBytes(new byte[]{0x24});

        channelHandlerContext.writeAndFlush(byteBuf);

        Log.d(TAG, "处理结束");
        return true;
    }
}
