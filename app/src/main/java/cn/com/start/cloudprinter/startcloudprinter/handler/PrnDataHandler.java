package cn.com.start.cloudprinter.startcloudprinter.handler;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import cn.com.itep.printer.DeviceInfo;
import cn.com.itep.printer.usb.UsbPrinter;
import cn.com.start.cloudprinter.startcloudprinter.StartCloudApplication;
import cn.com.start.cloudprinter.startcloudprinter.event.ExceptionEvent;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;

/**
 * Created by lutery on 2017/12/25.
 */

public class PrnDataHandler extends AbsHandler {

    private String TAG = PrnDataHandler.class.getSimpleName();

    @Override
    protected boolean handle(AbsRequest request) {
        Log.d(TAG, "handle");

        if (true || !(request instanceof BytesRequest)){
            return false;
        }

        BytesRequest bytesRequest = (BytesRequest)request;

        byte[] data = bytesRequest.getContent();

        Log.d(TAG, new StringBuilder().append("data is null").append(data == null).toString());

        if (data.length < 9){
            EventBus.getDefault().post(new ExceptionEvent(new Exception("数据长度不对")));

            return false;
        }

        if (data[0] != (byte)0xf0){
            return false;
        }


//        try {
//            File prnFile = new File("/sdcard/123.prn");
//            if (prnFile.exists()){
//                prnFile.delete();
//            }
//
//            prnFile.createNewFile();
//
//            OutputStream outputStream = new FileOutputStream(prnFile);
//
//            outputStream.write(data);
//            outputStream.flush();
//            outputStream.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        UsbPrinter usbPrinter = UsbPrinter.getInstance(StartCloudApplication.getSContext());

        if (!usbPrinter.IsConnect()){
            EventBus.getDefault().post(new ExceptionEvent(new Exception("未连接设备")));
            return false;
        }

        byte[] lengthBytes = new byte[4];
        lengthBytes[0] = data[4];
        lengthBytes[1] = data[5];
        lengthBytes[2] = data[6];
        lengthBytes[3] = data[7];

        int contentLength = ToolUtil.bytesToInt(lengthBytes);
        Log.d(TAG, "contentLength = " + contentLength);

        if (contentLength != (data.length - 24)){
            EventBus.getDefault().post(new ExceptionEvent(new Exception("数据内容长度不匹配，请重新发送")));
            return true;
        }

        byte[] verifyCode = new byte[16];

        for (int i = 0; i < 16; ++i){
            verifyCode[i] = data[i + 8];
        }

        byte[] content = new byte[contentLength];

        for (int i = 0; i < contentLength; ++i){
            content[i] = data[24 + i];
        }

        byte[] md5Code = ToolUtil.toMD5Bytes(content);

        for (int i = 0; i < md5Code.length; ++i){
            if (md5Code[i] != verifyCode[i]){
                EventBus.getDefault().post(new ExceptionEvent(new Exception("数据校验出错，请重新发送")));
                return true;
            }
        }

        usbPrinter.writeDevice(content, content.length);

        return true;
    }
}
