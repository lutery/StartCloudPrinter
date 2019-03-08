package cn.com.start.cloudprinter.startcloudprinter.handler;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.com.itep.printer.usb.UsbPrinter;
import cn.com.start.cloudprinter.startcloudprinter.StartCloudApplication;
import cn.com.start.cloudprinter.startcloudprinter.event.ExceptionEvent;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;

/**
 * Created by lutery on 2018/1/16.
 */

public class PrnDataSaveHandler extends AbsHandler {

    private final static String TAG = PrnDataSaveHandler.class.getSimpleName();

    @Override
    protected boolean handle(AbsRequest request) {

        Log.d(TAG, "handle");

        if (!(request instanceof BytesRequest)) {
            return false;
        }

        BytesRequest bytesRequest = (BytesRequest) request;

        byte[] data = bytesRequest.getContent();

        Log.d(TAG, new StringBuilder().append("data is null").append(data == null).toString());

        if (data.length < 8) {
            EventBus.getDefault().post(new ExceptionEvent(new Exception("数据长度不对")));

            return false;
        }

        if ((data[0] & 0xf0) == 0x00) {
            return false;
        }

        byte[] lengthBytes = new byte[4];
        lengthBytes[0] = data[4];
        lengthBytes[1] = data[5];
        lengthBytes[2] = data[6];
        lengthBytes[3] = data[7];

        int contentLength = ToolUtil.bytesToInt(lengthBytes);
        Log.d(TAG, "contentLength = " + contentLength);

        int headerLength = 9;
        int verifyLength = 0;
        byte verifyType = data[8];

        switch (verifyType){
            case 0x01:
                verifyLength = 16;
                break;

            case 0x02:
                verifyLength = 1;
                break;

            case 0x03:
                verifyLength = 2;
                break;

            case 0x04:
                verifyLength = 4;
                break;
        }

        headerLength += verifyLength;

        if (contentLength != (data.length - headerLength - 1)) {
            EventBus.getDefault().post(new ExceptionEvent(new Exception("数据内容长度不匹配，请重新发送")));
            return true;
        }

        byte[] verifyCode = new byte[verifyLength];

        for (int i = 0; i < verifyLength; ++i) {
            verifyCode[i] = data[i + 9];
        }

        byte[] content = new byte[contentLength];

        for (int i = 0; i < contentLength; ++i) {
            content[i] = data[headerLength + i];
        }

        byte[] contentVerifyCode = ToolUtil.toCRC16Bytes(content);

        for (int i = 0; i < contentVerifyCode.length; ++i) {
            if (contentVerifyCode[i] != verifyCode[i]) {
                EventBus.getDefault().post(new ExceptionEvent(new Exception("数据校验出错，请重新发送")));
                return true;
            }
        }

//        usbPrinter.writeDevice(content, content.length);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = simpleDateFormat.format(new Date());

        try {
            File prnFile = new File(new StringBuilder().append("/sdcard/").append(date).append(".prn").toString());
            if (prnFile.exists()) {
                prnFile.delete();
            }

            prnFile.createNewFile();

            OutputStream outputStream = new FileOutputStream(prnFile);

            outputStream.write(data);
            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
