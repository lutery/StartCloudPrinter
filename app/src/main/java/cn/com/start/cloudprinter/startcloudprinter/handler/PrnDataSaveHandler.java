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
import cn.com.start.cloudprinter.startcloudprinter.po.OrderObj;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;

/**
 * Created by lutery on 2018/1/16.
 */

public class PrnDataSaveHandler extends AbsHandler<BytesRequest> {

    private final static String TAG = PrnDataSaveHandler.class.getSimpleName();

    @Override
    protected boolean innerHandle(OrderObj orderObj) {

        Log.d(TAG, "handle");

        if (orderObj == null){
            return false;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = simpleDateFormat.format(new Date());

        try {
            File prnFile = new File(new StringBuilder().append("/sdcard/").append(date).append(".prn").toString());
            if (prnFile.exists()) {
                prnFile.delete();
            }

            prnFile.createNewFile();

            OutputStream outputStream = new FileOutputStream(prnFile);

            outputStream.write(orderObj.getContent());
            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
