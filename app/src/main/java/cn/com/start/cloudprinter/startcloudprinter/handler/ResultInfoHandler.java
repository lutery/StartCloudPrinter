package cn.com.start.cloudprinter.startcloudprinter.handler;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.com.start.cloudprinter.startcloudprinter.event.ExceptionEvent;
import cn.com.start.cloudprinter.startcloudprinter.po.OrderObj;

public class ResultInfoHandler extends AbsHandler<BytesRequest> {

    private final static String TAG = ResultInfoHandler.class.getSimpleName();

    @Override
    protected boolean innerHandle(OrderObj orderObj) {

        if (orderObj == null){
            return false;
        }

        if (orderObj.getOrder()[0] != 0x0f){
            return false;
        }

        try {
            JSONObject resultInfoObj = new JSONObject(new String(orderObj.getContent(), "gb18030"));

            String result = resultInfoObj.getString("result");

            Log.d(TAG, "result is " + result);

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
