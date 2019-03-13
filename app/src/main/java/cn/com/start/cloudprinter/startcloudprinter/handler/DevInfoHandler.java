package cn.com.start.cloudprinter.startcloudprinter.handler;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import cn.com.start.cloudprinter.startcloudprinter.StartCloudApplication;
import cn.com.start.cloudprinter.startcloudprinter.event.ExceptionEvent;
import cn.com.start.cloudprinter.startcloudprinter.order.PrinterOrder;
import cn.com.start.cloudprinter.startcloudprinter.po.OrderObj;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;

/**
 * Created by lutery on 2017/12/26.
 */

public class DevInfoHandler extends AbsHandler<BytesRequest> {

    private final String TAG = DevInfoHandler.class.getSimpleName();

    @Override
    protected boolean innerHandle(OrderObj orderObj) {

        if (orderObj == null){
            return false;
        }

        if (orderObj.getOrder()[0] != (byte)0x10){
            return false;
        }

        String devId = ToolUtil.getUniqueId(StartCloudApplication.getSContext());

        JSONObject devJson = new JSONObject();
        try {
            devJson.put("deviceid", devId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String devStr = devJson.toString();

        byte[] devInfoBytes = new byte[0];
        try {
            devInfoBytes = devStr.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] devLength = ToolUtil.intToBytes(devInfoBytes.length);

        byte[] verifyCode = ToolUtil.toCRC16Bytes(devInfoBytes);

        try {
            Log.d(TAG, "devLength = " + ToolUtil.byte2HexStr(devLength));
            Log.d(TAG, "verifyCode = " + ToolUtil.byte2HexStr(verifyCode));
            Log.d(TAG, "devInfoBytes = " + ToolUtil.byte2HexStr(devInfoBytes));

            outputStream.write(PrinterOrder.DEVINFO.getOrder());
            outputStream.write(devLength);
            outputStream.write(new byte[]{0x03});
            outputStream.write(verifyCode);
            outputStream.write(devInfoBytes);
            outputStream.write(new byte[]{0x24});
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();

            Log.d(TAG, "发送失败");
        }

        Log.d(TAG, "处理结束");
        return true;
    }
}
