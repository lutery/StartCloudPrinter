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
import cn.com.start.cloudprinter.startcloudprinter.handler.netty.DeviceOrder;
import cn.com.start.cloudprinter.startcloudprinter.po.OrderObj;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by lutery on 2017/12/25.
 */

public class PrnDataHandler extends AbsHandler {

    private String TAG = PrnDataHandler.class.getSimpleName();

    @Override
    protected boolean handle(ChannelHandlerContext channelHandlerContext, DeviceOrder deviceOrder) {
        Log.d(TAG, "handle");

        return false;

//        UsbPrinter usbPrinter = UsbPrinter.getInstance(StartCloudApplication.getSContext());
//
//        if (!usbPrinter.IsConnect()){
//            EventBus.getDefault().post(new ExceptionEvent(new Exception("未连接设备")));
//            return false;
//        }
//
//        usbPrinter.writeDevice(deviceOrder.getOrderContent(), deviceOrder.getOrderContent().length);
//
//        return true;
    }
}
