package cn.com.start.cloudprinter.startcloudprinter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Executors;

import cn.com.start.cloudprinter.startcloudprinter.event.ExceptionEvent;
import cn.com.start.cloudprinter.startcloudprinter.event.GeneralEvent;
import cn.com.start.cloudprinter.startcloudprinter.event.PrintEvent;
import cn.com.start.cloudprinter.startcloudprinter.handler.DevInfoHandler;
import cn.com.start.cloudprinter.startcloudprinter.handler.netty.DelimiterPrinterOrderFrameDecoder;
import cn.com.start.cloudprinter.startcloudprinter.handler.netty.DeviceOrderHandler;
import cn.com.start.cloudprinter.startcloudprinter.order.PrinterOrder;
import cn.com.start.cloudprinter.startcloudprinter.tool.verify.impl.CRC16CCITTVerify;
import cn.com.start.cloudprinter.startcloudprinter.util.DeviceStatus;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;

/**
 * Created by 程辉 on 2017/11/28.
 */

/**
 * 打印后台服务
 */
public class PrinterService extends Service {

    private final String TAG = PrinterService.class.getSimpleName();
    private final String CHANNEL_ID = PrinterService.class.getSimpleName();
    private final String NOTIFICATION_ACTION = PrinterService.class.getName() + ".NOTIFICATION_ACTION";
    private final int REQUEST_CODE_BROADCAST = 1;
    private final int NOTIFICATION_ID = 2;

    private RemoteViews mRemoteViews;
    private Notification mNotification;
    private NotificationManager mNotificationManager;

    private EventLoopGroup mainGroup;
    private Bootstrap clientBootstrap;
    @Getter
    private static Channel channel;

    // 服务器ip地址
//    private final String mServerIP = "test.print.startprinter.com.cn";
//    private final String mServerIP = "startprinter.com.cn";
//    private final String mServerIP = "192.168.227.3";
//    private final String mServerIP = "192.168.2.109";
//    private final String mServerIP = "192.168.66.72";
//    private final String mServerIP = "172.20.10.8";
//    private final String mServerIP = "10.0.0.23";
    private final String mServerIP = "192.168.101.3";

    // 服务器端口
    private final int mServerPort = 9100;
//    private final int mServerPort = 9101;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(CHANNEL_ID, "onCreate");

//        Executors.newSingleThreadExecutor().execute(new PrinterRunnable(ToolUtil.getUniqueId(this)));
        Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    initNetty();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        });

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep(5000);

                initWebSocket();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        EventBus.getDefault().register(this);

        showNotification();
    }

    private void initWebSocket() throws URISyntaxException {
//        SubcribeClient subcribeClient = new SubcribeClient(new URI("ws://10.0.0.8:8080/printer/subscribe?devId=536a92b1b4fdd5341ade2b4d7779e8af"), new HashMap<String, String>(){
//            {put("Authorization", "bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJsdXRlcnkiLCJzY29wZSI6WyJhbGwiXSwiY29tcGFueSI6InN0YXJ0IiwiZXhwIjoxNTY0MTA2NzU1LCJhdXRob3JpdGllcyI6WyJST0xFX0FETUlOIiwiUk9MRV9QUklOVEVSIl0sImp0aSI6IjlkMTgxMDNkLWM0MmItNGQyZC04YTdkLTYxOTBiYjA2N2U1MyIsImNsaWVudF9pZCI6ImNsb3VkcHJpbnRlciJ9.o7L8kzOkSK0X3gGckFdOcybfM-paB7MvzOMq9UOiv4g");}
//        });

        SubcribeClient subcribeClient = new SubcribeClient(new URI("ws://print.startprinter.com.cn/printer/subscribe?devId=536a92b1b4fdd5341ade2b4d7779e8af"), new HashMap<String, String>(){
            {put("Authorization", "bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJsdXRlcnkiLCJzY29wZSI6WyJhbGwiXSwiY29tcGFueSI6InN0YXJ0IiwiZXhwIjoxNTY1MjI4MzY3LCJhdXRob3JpdGllcyI6WyJST0xFX0FETUlOIiwiUk9MRV9QUklOVEVSIl0sImp0aSI6IjQyNTg3Y2M0LTQzMmItNGNjOC05ZmVmLTQ1NGZkMWZkMTIyZiIsImNsaWVudF9pZCI6ImNsb3VkcHJpbnRlciJ9.CM2PeerMqCeMZnfZ5UdlWBpK0QKCFfw2iKy12IImirI");}
        });
        subcribeClient.connect();
    }

    private void initNetty() throws InterruptedException {
        mainGroup = new NioEventLoopGroup();

        clientBootstrap = new Bootstrap().group(mainGroup)
                .channel(NioSocketChannel.class)
                .handler(new DeviceInitializer());

        Log.d(TAG, "start connnect server");
        ChannelFuture channelFuture = clientBootstrap.connect(mServerIP, mServerPort)
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()){
                            channel = future.channel();
                            channel.writeAndFlush(sendDevInit());
                        }
                        else {
                            EventBus.getDefault().post(new ExceptionEvent(new Exception("netty连接失败")));
                        }
                    }
                }).sync();

        Log.d(TAG, "disconnect server");
        channelFuture.channel().closeFuture().sync();
    }

    private ByteBuf sendDevStatus() throws JSONException, UnsupportedEncodingException {
        DeviceStatus[] deviceStatuses = DeviceStatus.values();
        DeviceStatus deviceStatus = deviceStatuses[new Random().nextInt(deviceStatuses.length)];

        JSONObject statusObj = new JSONObject();
        statusObj.put("status", deviceStatus.getCode());

        String statusStr = statusObj.toString();

        byte[] statusBytes= statusStr.getBytes("gb18030");
        byte[] length = ToolUtil.intToBytes(statusBytes.length);

        ByteBuf byteBuf = Unpooled.buffer(1024);

        Log.d(TAG, "devLength = " + ToolUtil.byte2HexStr(length));
        Log.d(TAG, "verifyCode = " + ToolUtil.byte2HexStr(new CRC16CCITTVerify().generateVerifyCode(statusBytes)));
        Log.d(TAG, "devInfoBytes = " + ToolUtil.byte2HexStr(statusBytes));

        byteBuf.writeBytes(PrinterOrder.DEVSTATUS.getOrder());
        byteBuf.writeBytes(length);
        byteBuf.writeByte(new CRC16CCITTVerify().verifyType().getType());
        byteBuf.writeBytes(new CRC16CCITTVerify().generateVerifyCode(statusBytes));
        byteBuf.writeBytes(statusBytes);
        byteBuf.writeByte((byte)0x24);

        return byteBuf;
    }

    private ByteBuf sendDevInit() {
//        ByteBuf byteBuf = Unpooled.buffer(32);
//        Log.d(TAG, "DEVINIT order = " + ToolUtil.byte2HexStr(PrinterOrder.DEVINIT.getOrder()));
//
//        byteBuf.writeBytes(PrinterOrder.DEVINIT.getOrder());
//        byteBuf.writeBytes(new byte[]{0x00, 0x00, 0x00, 0x00});
//        byteBuf.writeBytes(new byte[]{0x05, 0x00, 0x00});
//        byteBuf.writeBytes(new byte[]{0x24});
//
//        return byteBuf;

        String devId = ToolUtil.getUniqueId(StartCloudApplication.getSContext());

        Log.d(TAG, "devId is " + devId);

        JSONObject devJson = new JSONObject();
        try {
            devJson.put("deviceid", devId);

//            if (new Random().nextInt(100) < 50) {
//                devJson.put("driver", "ZPL");
//                devJson.put("page", "100X180");
//                devJson.put("resolution", "203X203");
//            }
//            else {
                devJson.put("driver", "ESC");
                devJson.put("page", "210X297");
                devJson.put("resolution", "180X180");
//            }
//            devJson.put("packageSize", 1024 * 1024 * 9);
            devJson.put("packageSize", 4096);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String devStr = devJson.toString();
        Log.d(TAG, "devStr is " + devStr);

        byte[] devInfoBytes = new byte[0];
        try {
            devInfoBytes = devStr.getBytes("gb18030");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] devLength = ToolUtil.intToBytes(devInfoBytes.length);

        byte[] verifyCode = new CRC16CCITTVerify().generateVerifyCode(devInfoBytes);

        Log.d(TAG, "devLength = " + ToolUtil.byte2HexStr(devLength));
        Log.d(TAG, "verifyCode = " + ToolUtil.byte2HexStr(verifyCode));
        Log.d(TAG, "devInfoBytes = " + ToolUtil.byte2HexStr(devInfoBytes));

        ByteBuf byteBuf = Unpooled.buffer(1024);
        byteBuf.writeBytes(PrinterOrder.DEVINFO.getOrder());
        byteBuf.writeBytes(devLength);
        byteBuf.writeBytes(new byte[]{0x05});
        byteBuf.writeBytes(verifyCode);
        byteBuf.writeBytes(devInfoBytes);
        byteBuf.writeBytes(new byte[]{0x24});

        return byteBuf;
    }

    private ByteBuf sendLogout(){
        ByteBuf byteBuf = Unpooled.buffer(32);
        byteBuf.writeBytes(PrinterOrder.LOGOUT.getOrder());
        byteBuf.writeBytes(new byte[]{00, 00, 00, 00, 05, 00, 00, 0x24});

        return byteBuf;
    }

    private ByteBuf sendWeightInfo(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("indentinfo", "1163545004693");
            jsonObject.put("weight", String.valueOf(new Random().nextFloat()));
//            jsonObject.put("weight", "a");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String devStr = jsonObject.toString();
        Log.d(TAG, "devStr is " + devStr);

        byte[] weightInfoBytes = new byte[0];
        try {
            weightInfoBytes = devStr.getBytes("gb18030");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] devLength = ToolUtil.intToBytes(weightInfoBytes.length);

        byte[] verifyCode = new CRC16CCITTVerify().generateVerifyCode(weightInfoBytes);

        Log.d(TAG, "devLength = " + ToolUtil.byte2HexStr(devLength));
        Log.d(TAG, "verifyCode = " + ToolUtil.byte2HexStr(verifyCode));
        Log.d(TAG, "devInfoBytes = " + ToolUtil.byte2HexStr(weightInfoBytes));

        ByteBuf byteBuf = Unpooled.buffer(1024);
        byteBuf.writeBytes(PrinterOrder.WEIGHTINFO.getOrder());
        byteBuf.writeBytes(devLength);
        byteBuf.writeBytes(new byte[]{0x05});
        byteBuf.writeBytes(verifyCode);
        byteBuf.writeBytes(weightInfoBytes);
        byteBuf.writeBytes(new byte[]{0x24});

        return byteBuf;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(CHANNEL_ID, "onStartCommand");

        if (channel != null) {

            if (intent.getIntExtra("ttype", 0) == 1) {
                channel.writeAndFlush(sendDevInit());
            }
            else if (intent.getIntExtra("ttype", 0) == 2){
                channel.writeAndFlush(sendWeightInfo());
            }
            else if (intent.getIntExtra("ttype", 0) == 3){
                channel.writeAndFlush(sendLogout());
            }
            else if (intent.getIntExtra("ttype", 0) == 4){
                try {
                    channel.writeAndFlush(sendDevStatus());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "service destory");
        EventBus.getDefault().unregister(this);
    }

    /**
     * 显示通知
     */
    private void showNotification(){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setOngoing(true);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setAutoCancel(false);
        mRemoteViews = new RemoteViews(this.getPackageName(), R.layout.notification_print);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE_BROADCAST, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContent(mRemoteViews);
        builder.setContentIntent(pendingIntent);
        mNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

//        builder//.setContentTitle("测试标题")//设置通知栏标题
//                //.setContentText("测试内容")
//                .setContent(mRemoteViews)
//    .setContentIntent(getDefalutIntent(Notification.FLAG_AUTO_CANCEL)) //设置通知栏点击意图
////  .setNumber(number) //设置通知集合的数量
//                .setTicker("测试通知来啦") //通知首次出现在通知栏，带上升动画效果的
//                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
//                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
////  .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
//                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
//                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
//                //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
//                .setSmallIcon(R.mipmap.ic_launcher);//设置通知小ICON

        mNotification = builder.build();
        mNotification.flags = Notification.FLAG_NO_CLEAR;
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    public PendingIntent getDefalutIntent(int flags){
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPrintEvent(PrintEvent pe){
        updatePrinterInfo(pe.getMsg());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGeneralEvent(GeneralEvent ge){
        updatePrinterInfo(ge.getMsg());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExceptionEvent(ExceptionEvent ee){
        updatePrinterInfo(ee.getMException().toString());
    }

    public void updatePrinterInfo(String info){
        Log.d(TAG, info);
        mRemoteViews.setTextViewText(R.id.printer_server_info, info);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    class DeviceInitializer extends ChannelInitializer<SocketChannel>{

        @Override
        protected void initChannel(SocketChannel ch) {
            ch.pipeline()
                    .addLast(new IdleStateHandler(0, 0, 30))
                    .addLast(new DelimiterPrinterOrderFrameDecoder(4, 4, 1, (byte)0x24))
                    .addLast(new DeviceOrderHandler());
        }
    }
}
