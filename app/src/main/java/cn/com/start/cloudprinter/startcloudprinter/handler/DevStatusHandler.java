package cn.com.start.cloudprinter.startcloudprinter.handler;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import cn.com.start.cloudprinter.startcloudprinter.event.ExceptionEvent;
import cn.com.start.cloudprinter.startcloudprinter.po.OrderObj;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;

public class DevStatusHandler extends AbsHandler<BytesRequest> {
    @Override
    protected boolean innerHandle(OrderObj orderObj) {

        if (orderObj == null){
            return false;
        }

        if (orderObj.getOrder()[0] != 0x12){
            return false;
        }

        try {
            JSONObject statusObj = new JSONObject();
            statusObj.put("status", 32);

            String statusStr = statusObj.toString();

            byte[] statusBytes= statusStr.getBytes("gb18030");
            byte[] length = ToolUtil.intToBytes(statusBytes.length);

            outputStream.write(new byte[]{(byte)0xf1, 0x00, 0x00, 0x00});
            outputStream.write(length);
            outputStream.write(mVerifyTool.verifyType().getType());
            outputStream.write(mVerifyTool.generateVerifyCode(statusBytes));
            outputStream.write(statusBytes);
            outputStream.write((byte)0x24);

        } catch (JSONException e) {
            EventBus.getDefault().post(new ExceptionEvent(new Exception("")));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        return false;
    }
}
