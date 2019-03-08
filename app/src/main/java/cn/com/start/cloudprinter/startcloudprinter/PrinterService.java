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

import java.util.concurrent.Executors;

import cn.com.start.cloudprinter.startcloudprinter.event.ExceptionEvent;
import cn.com.start.cloudprinter.startcloudprinter.event.GeneralEvent;
import cn.com.start.cloudprinter.startcloudprinter.event.PrintEvent;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;

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

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(CHANNEL_ID, "onCreate");

        Executors.newSingleThreadExecutor().execute(new PrinterRunnable(ToolUtil.getUniqueId(this)));
        EventBus.getDefault().register(this);

        showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(CHANNEL_ID, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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
}
