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
import cn.com.start.cloudprinter.startcloudprinter.po.OrderObj;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;

/**
 * Created by lutery on 2017/12/25.
 */

public class PrnDataHandler extends AbsHandler<BytesRequest> {

    private String TAG = PrnDataHandler.class.getSimpleName();

    @Override
    protected boolean innerHandle(OrderObj orderObj) {
        Log.d(TAG, "handle");

        if (orderObj == null){
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

//        if (mVerifyTool.verifyContent(orderObj.getVerityCode(), orderObj.getContent())) {
//            EventBus.getDefault().post(new ExceptionEvent(new Exception("数据校验出错，请重新发送")));
//            return true;
//        }

        usbPrinter.writeDevice(orderObj.getContent(), orderObj.getContent().length);

        return true;
    }
}
