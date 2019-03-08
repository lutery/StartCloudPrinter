package cn.com.start.cloudprinter.startcloudprinter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.itep.printer.usb.UsbPrinter;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;
import one.chchy.libqrcode.generator.QRCode;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.iv_device_id)ImageView mIVDeviceID;
    @BindView(R.id.tv_tip)TextView mTVTip;

    private Handler mHandlerUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mHandlerUI = new Handler(getMainLooper());

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
    }
}
