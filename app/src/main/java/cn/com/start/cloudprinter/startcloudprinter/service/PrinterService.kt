package cn.com.start.cloudprinter.startcloudprinter.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.EventLoopGroup

class PrinterService : Service() {

    private val TAG = PrinterService::class.simpleName
    private val REQUEST_CODE_BROADCAST = 1
    private val NOTIFICATION_ID = 2

    private var remoteViews:RemoteViews? = null
    private var notification:Notification? = null
    private var notificationManager:NotificationManager? = null

    private var mainGroup:EventLoopGroup? = null
    private var clientBootstrap:Bootstrap? = null

    companion object {
        var channel:Channel? = null
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

//        intent.get

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}
