package cn.com.start.cloudprinter.startcloudprinter.handler;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import cn.com.start.cloudprinter.startcloudprinter.event.ExceptionEvent;
import cn.com.start.cloudprinter.startcloudprinter.handler.netty.DeviceOrder;
import cn.com.start.cloudprinter.startcloudprinter.po.OrderObj;
import cn.com.start.cloudprinter.startcloudprinter.util.DeviceStatus;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class DevStatusHandler extends AbsHandler{

    private final static String TAG = DevStatusHandler.class.getSimpleName();

    @Override
    protected boolean handle(ChannelHandlerContext channelHandlerContext, DeviceOrder deviceOrder) {

        if (deviceOrder.getOrderType()[0] != (byte)0x12){
            return false;
        }

        try {
            DeviceStatus[] deviceStatuses = DeviceStatus.values();
            DeviceStatus deviceStatus = deviceStatuses[new Random().nextInt(deviceStatuses.length)];

            JSONObject statusObj = new JSONObject();
            statusObj.put("status", deviceStatus.getCode());

            String statusStr = statusObj.toString();

            byte[] statusBytes= statusStr.getBytes("gb18030");
            byte[] length = ToolUtil.intToBytes(statusBytes.length);

            ByteBuf byteBuf = Unpooled.buffer(1024);

            byteBuf.writeBytes(new byte[]{(byte)0x12, 0x00, 0x00, 0x00});
            byteBuf.writeBytes(length);
            byteBuf.writeByte(mVerifyTool.verifyType().getType());
            byteBuf.writeBytes(mVerifyTool.generateVerifyCode(statusBytes));
            byteBuf.writeBytes(statusBytes);
            byteBuf.writeByte((byte)0x24);

            channelHandlerContext.writeAndFlush(byteBuf);
            Log.d(TAG, "send dev status complete");

        } catch (JSONException e) {
            EventBus.getDefault().post(new ExceptionEvent(new Exception("")));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        return true;
    }
}
