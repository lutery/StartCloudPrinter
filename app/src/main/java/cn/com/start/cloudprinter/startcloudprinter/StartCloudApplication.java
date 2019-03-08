package cn.com.start.cloudprinter.startcloudprinter;

import android.app.Application;
import android.content.Context;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import lombok.Getter;

/**
 * Created by lutery on 2017/12/25.
 */

public class StartCloudApplication extends Application {

    @Getter
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = getApplicationContext();

        Logger.addLogAdapter(new AndroidLogAdapter());
    }
}
