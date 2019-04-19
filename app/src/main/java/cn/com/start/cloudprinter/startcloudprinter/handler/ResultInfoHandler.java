package cn.com.start.cloudprinter.startcloudprinter.handler;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.com.start.cloudprinter.startcloudprinter.event.ExceptionEvent;
import cn.com.start.cloudprinter.startcloudprinter.handler.netty.DeviceOrder;
import cn.com.start.cloudprinter.startcloudprinter.po.OrderObj;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;
import io.netty.channel.ChannelHandlerContext;

public class ResultInfoHandler extends AbsHandler {

    private final static String TAG = ResultInfoHandler.class.getSimpleName();

    @Override
    protected boolean handle(ChannelHandlerContext channelHandlerContext, DeviceOrder deviceOrder) {

        if (deviceOrder.getOrderType()[0] != 0x0f){
            return false;
        }

        try {
            JSONObject resultInfoObj = new JSONObject(new String(deviceOrder.getOrderContent(), "gb18030"));

            String result = resultInfoObj.getString("result");

            Log.d(TAG, "result is " + result);

            return true;

        } catch (JSONException e) {
//            Log.d(TAG, ToolUtil.byte2HexStr(deviceOrder.combine()));
            e.printStackTrace();
            EventBus.getDefault().post(new ExceptionEvent(e));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new ExceptionEvent(e));
        }

        return false;
    }
}
