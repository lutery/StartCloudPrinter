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
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;

/**
 * Created by lutery on 2017/12/26.
 */

public class DevInfoHandler extends AbsHandler {

    private final String TAG = DevInfoHandler.class.getSimpleName();

    @Override
    protected boolean handle(AbsRequest request) {

        if (!(request instanceof BytesRequest)){
            return false;
        }

        BytesRequest bytesRequest = (BytesRequest)request;

        byte[] data = bytesRequest.getContent();

        if (data.length < 9){
            EventBus.getDefault().post(new ExceptionEvent(new Exception("数据长度不对")));

            return false;
        }

        if (data[0] != (byte)0x10){
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

            bytesRequest.getOutputStream().write(PrinterOrder.DEVINFO.getOrder());
            bytesRequest.getOutputStream().write(devLength);
            bytesRequest.getOutputStream().write(new byte[]{0x03});
            bytesRequest.getOutputStream().write(verifyCode);
            bytesRequest.getOutputStream().write(devInfoBytes);
            bytesRequest.getOutputStream().write(new byte[]{0x24});
            bytesRequest.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();

            Log.d(TAG, "发送失败");
        }

        Log.d(TAG, "处理结束");
        return true;
    }
}
