package cn.com.start.cloudprinter.startcloudprinter.handler;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.com.start.cloudprinter.startcloudprinter.event.ExceptionEvent;
import cn.com.start.cloudprinter.startcloudprinter.handler.netty.DeviceOrder;
import cn.com.start.cloudprinter.startcloudprinter.order.PrinterOrder;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;
import io.netty.channel.ChannelHandlerContext;

public class PrnInfoHandler extends AbsHandler {

    private static final String TAG = PrnInfoHandler.class.getSimpleName();

    @Override
    protected boolean handle(ChannelHandlerContext channelHandlerContext, DeviceOrder deviceOrder) {

        if (deviceOrder.getOrderType()[0] != PrinterOrder.PRNINFO.getOrder()[0]){
            return false;
        }

        try {
            JSONObject jsonPrnInfo = new JSONObject(new String(deviceOrder.getOrderContent(), "gb18030"));

            String prnid = jsonPrnInfo.getString("prnid");
            int size = jsonPrnInfo.getInt("size");

            Log.d(TAG, "prnid is " + prnid + ", size is " + size);

//            Log.d(TAG, ToolUtil.byte2HexStr(deviceOrder.combine()));

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new ExceptionEvent(e));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new ExceptionEvent(e));
        }

        return false;
    }
}
