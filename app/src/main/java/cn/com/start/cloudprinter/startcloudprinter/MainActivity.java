package cn.com.start.cloudprinter.startcloudprinter;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import chchy.one.checkpermission.PermissionCheck;
import chchy.one.checkpermission.PermissionResult;
import cn.com.itep.printer.usb.UsbPrinter;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;
import one.chchy.libqrcode.generator.QRCode;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.iv_device_id)ImageView mIVDeviceID;
    @BindView(R.id.tv_tip)TextView mTVTip;
    @BindView(R.id.reset)Button mResetBtn;
    @BindView(R.id.weight)Button mWeightBtn;
    @BindView(R.id.logout)Button mLogOutBtn;
    @BindView(R.id.staus)Button mStatusBtn;

    private Handler mHandlerUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mHandlerUI = new Handler(getMainLooper());

        PermissionCheck.checkPermission(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, new PermissionResult() {
            @Override
            public void permissionResult(int resultCode) {
                Log.d(TAG, "resultCode = " + resultCode);
            }
        });

        new Thread(() -> {
                String uniqueId = ToolUtil.getUniqueId(MainActivity.this);
                Bitmap qrCode = QRCode.createQRCode(uniqueId);

                mHandlerUI.post(() -> {
                    mIVDeviceID.setImageBitmap(qrCode);
                    mTVTip.setText(uniqueId);
                });
        }).start();

        Intent intent = new Intent(this, PrinterService.class);
        startService(intent);

        UsbPrinter usbPrinter = UsbPrinter.getInstance(StartCloudApplication.getSContext());

        if (!usbPrinter.IsConnect()) {

            usbPrinter.findDevice();

            if (usbPrinter.getDevices().size() > 0) {
                usbPrinter.openDevice(usbPrinter.getDevice(0));
            }
        }

        mResetBtn.setOnClickListener(view->{
//            Intent intent1 = new Intent(this, PrinterService.class);
            intent.putExtra("ttype", 1);
            startService(intent);
        });

        mWeightBtn.setOnClickListener(view -> {
            intent.putExtra("ttype", 2);
            startService(intent);
        });

        mLogOutBtn.setOnClickListener( view -> {
            intent.putExtra("ttype", 3);
            startService(intent);
        });

        mStatusBtn.setOnClickListener( view -> {
            intent.putExtra("ttype", 4);
            startService(intent);
        });
    }
}
