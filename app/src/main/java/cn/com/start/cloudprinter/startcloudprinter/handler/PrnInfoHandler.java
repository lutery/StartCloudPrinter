package cn.com.start.cloudprinter.startcloudprinter.handler;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.com.start.cloudprinter.startcloudprinter.event.ExceptionEvent;
import cn.com.start.cloudprinter.startcloudprinter.po.OrderObj;

public class PrnInfoHandler extends AbsHandler<BytesRequest> {

    private static final String TAG = PrnInfoHandler.class.getSimpleName();

    @Override
    protected boolean innerHandle(OrderObj orderObj) {

        if (orderObj == null){
            return false;
        }

        if (orderObj.getOrder()[0] != 0x01){
            return false;
        }

        try {
            JSONObject jsonPrnInfo = new JSONObject(new String(orderObj.getContent(), "gb18030"));

            String prnid = jsonPrnInfo.getString("prnid");
            int size = jsonPrnInfo.getInt("size");

            Log.d(TAG, "prnid is " + prnid + ", size is " + size);

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
