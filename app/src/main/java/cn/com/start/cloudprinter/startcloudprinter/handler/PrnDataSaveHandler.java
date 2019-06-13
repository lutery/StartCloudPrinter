package cn.com.start.cloudprinter.startcloudprinter.handler;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import cn.com.itep.printer.usb.UsbPrinter;
import cn.com.start.cloudprinter.startcloudprinter.StartCloudApplication;
import cn.com.start.cloudprinter.startcloudprinter.event.ExceptionEvent;
import cn.com.start.cloudprinter.startcloudprinter.handler.netty.DeviceOrder;
import cn.com.start.cloudprinter.startcloudprinter.order.PrinterOrder;
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

        if (deviceOrder.getOrderType()[0] != PrinterOrder.PRNDATA.getOrder()[0]){
            return false;
        }

//        Log.d(TAG, ToolUtil.byte2HexStr(deviceOrder.combine()));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss_SSS");
        String date = simpleDateFormat.format(new Date());

        String toResult = "ok";
        try {
            if (!mVerifyTool.verifyContent(deviceOrder.getVerifyCode(), deviceOrder.getOrderContent())){
                toResult = "failed";
                Log.d(TAG, "可打印数据校验失败");
                throw new IOException("可打印数据校验失败，请求服务器重新发送数据");
            }

            if ((new Random(System.currentTimeMillis()).nextInt(100)) <= 20){
                toResult = "failed";
                Log.d(TAG, "网络发生波动，请服务器重新发送数据");
                throw new IOException("网络发生波动，请服务器重新发送数据");
            }

            Log.d(TAG, "开始睡眠");
            Thread.sleep(new Random(System.currentTimeMillis()).nextInt(20) * 1000);
            Log.d(TAG, "结束睡眠");


//            ToolUtil.byte2HexStr(mVerifyTool.generateVerifyCode(new byte[]{
//                    0x1b, 0x40, 0x1b, 0x4a , 0x4b , 0x1b , 0x24 , 0x47 , 0x00 , 0x1d , 0x38 , 0x4c ,
//                    0x62 , 0x00 , 0x00 , 0x00 , 0x30 , 0x70 , 0x30 , 0x01 , 0x01 , 0x31 , 0x54 , 0x00 , 0x08 ,
//                    0x00 , 0x0e , 0x00 , 0x00 , 0x00 , 0x00 , 0x00 , 0x00 , 0x00 , 0x00 , 0x00 , (byte)0xc0 ,
//                    0x3f , 0x00 , 0x00 , 0x00 , 0x00 , 0x00 , 0x03 , (byte)0xc0 , 0x00 , 0x03 , (byte)0xe0 ,
//                    0x3f , (byte)0x80 , 0x00 , 0x18 , 0x00 , 0x00 , 0x07 , (byte)0xe0 , 0x00 , 0x07 , (byte)0xf0 , 0x7f ,
//                    (byte)0x80 , 0x00 , 0x7e , 0x00 , 0x00 , 0x0f , (byte)0xf0 , 0x00 , 0x07 , (byte)0xf0 , 0x7f ,
//                    (byte)0x80 , 0x00 , 0x7e , 0x00 , 0x00 , 0x0f , (byte)0xf0 , 0x00}));
            Log.d(TAG, ToolUtil.byte2HexStr(deviceOrder.getOrderContent()));
            File prnDir = new File("/sdcard/cloudprinter/");
            if (!prnDir.exists()){
                prnDir.mkdirs();
            }

            String prnFilePath = new StringBuilder().append("/sdcard/cloudprinter/").append(date).append(".prn").toString();
            Log.d(TAG, prnFilePath);
            File prnFile = new File(prnFilePath);
            if (prnFile.exists()) {
                prnFile.delete();
            }

            prnFile.createNewFile();

            OutputStream outputStream = new FileOutputStream(prnFile);

            outputStream.write(deviceOrder.getOrderContent());
//            outputStream.write(deviceOrder.combine());
            outputStream.flush();
            outputStream.close();

            Log.d(TAG, "Prn Data Save Complete");

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        ByteBuf byteBuf = Unpooled.buffer(1024);
//        byteBuf.writeBytes(ToolUtil.getResultMsg("ok", mVerifyTool));
        byteBuf.writeBytes(ToolUtil.getResultMsg(toResult, mVerifyTool));

        channelHandlerContext.writeAndFlush(byteBuf);

        return true;
    }
}
