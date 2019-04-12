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
import cn.com.start.cloudprinter.startcloudprinter.handler.netty.DeviceOrder;
import cn.com.start.cloudprinter.startcloudprinter.po.OrderObj;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by lutery on 2018/1/16.
 */

public class PrnDataSaveHandler extends AbsHandler {

    private final static String TAG = PrnDataSaveHandler.class.getSimpleName();

    @Override
    protected boolean handle(ChannelHandlerContext channelHandlerContext, DeviceOrder deviceOrder) {

        if (deviceOrder.getOrderType()[0] != (byte)0xf0){
            return false;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        String date = simpleDateFormat.format(new Date());

        try {
            Log.d(TAG, ToolUtil.byte2HexStr(deviceOrder.getOrderContent()));
            String prnFilePath = new StringBuilder().append("/sdcard/cloudprinter/").append(date).append(".prn").toString();
            Log.d(TAG, prnFilePath);
            File prnFile = new File(prnFilePath);
            if (prnFile.exists()) {
                prnFile.delete();
            }

            prnFile.createNewFile();

            OutputStream outputStream = new FileOutputStream(prnFile);

            outputStream.write(deviceOrder.getOrderContent());
            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteBuf byteBuf = Unpooled.buffer(1024);
        byteBuf.writeBytes(ToolUtil.getResultMsg("ok", mVerifyTool));

        channelHandlerContext.writeAndFlush(byteBuf);

        return true;
    }
}
