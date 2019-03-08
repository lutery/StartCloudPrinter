package cn.com.start.cloudprinter.startcloudprinter;

import android.util.Log;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.com.start.cloudprinter.startcloudprinter.event.ExceptionEvent;
import cn.com.start.cloudprinter.startcloudprinter.handler.AbsHandler;
import cn.com.start.cloudprinter.startcloudprinter.handler.AbsRequest;
import cn.com.start.cloudprinter.startcloudprinter.handler.BytesRequest;
import cn.com.start.cloudprinter.startcloudprinter.handler.DevInfoHandler;
import cn.com.start.cloudprinter.startcloudprinter.handler.PrnDataHandler;
import cn.com.start.cloudprinter.startcloudprinter.handler.PrnDataSaveHandler;
import cn.com.start.cloudprinter.startcloudprinter.order.PrinterOrder;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;

/**
 * Created by lutery on 2017/11/29.
 */

/**
 * 打印线程
 */
public class PrinterRunnable implements Runnable {

    private final String TAG = PrinterRunnable.class.getSimpleName();

    // 服务器ip地址
//    private final String mServerIP = "58.87.111.219";
    private final String mServerIP = "192.168.2.103";

    // 服务器端口
    private final int mServerPort = 9200;
    private Socket mClient;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private String mDevId;
    private AbsHandler mFirstHandler;

    /**
     *
     * @param devId 设备id
     */
    public PrinterRunnable(String devId){
        mDevId = devId;

        mFirstHandler = new PrnDataHandler();
        AbsHandler devInfoHandler = new DevInfoHandler();
        AbsHandler prnSaveHandler = new PrnDataSaveHandler();

        mFirstHandler.setMNextHandler(devInfoHandler);
        devInfoHandler.setMNextHandler(prnSaveHandler);

    }

    @Override
    public void run() {
        while (true){
            connectToServer();

            try {
//                sendDevInfo();
                sendDevInit();
            }
            catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            // 监听循环
            while (true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    int length = mInputStream.available();

                    if (length > 4){
                        ByteBuffer serverData = ByteBuffer.allocate(length * 2);

                        int readLength = 0;
                        byte[] readBuffer = new byte[1024];

                        try {
                            while ((readLength = mInputStream.read(readBuffer)) > 0) {


                                int position = serverData.position();

                                if ((position + readLength) > serverData.capacity()) {
                                    ByteBuffer newData = ByteBuffer.allocate((serverData.capacity() + readLength) * 2);

                                    serverData.flip();

                                    byte[] tempData = new byte[serverData.limit()];
                                    serverData.get(tempData);

                                    newData.put(tempData);

                                    serverData = newData;
                                }

                                serverData.put(readBuffer, 0, readLength);

                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        catch (SocketTimeoutException ste){
//                            ste.printStackTrace();
                        }

                        Logger.d("Handle Data Start");
                        serverData.flip();

                        byte[] recvData = new byte[serverData.limit()];
                        serverData.get(recvData);

                        AbsRequest absRequest = new BytesRequest();

                        absRequest.setInputStream(mInputStream);
                        absRequest.setOutputStream(mOutputStream);
                        absRequest.setContent(recvData);

                        mFirstHandler.handleRequest(absRequest);

//                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                        Logger.d("SendRecvComplete Time ", formatter.format(new Date()));
                        Logger.d("SendRecvComplete Start");

                        sendRecvComplete();

                        Logger.d("SendRecvComplete End");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }


            }
        }
    }

    private void sendRecvComplete() throws IOException {

        byte[] msgLength = new byte[4];
        byte[] verifyCode;

        String msgContent = "{\"result\":\"ok\"}";
        byte[] contentLength = ToolUtil.intToBytes(msgContent.length());
        byte[] content = msgContent.getBytes();
        verifyCode = ToolUtil.toCRC16Bytes(content);

        mOutputStream.write(PrinterOrder.PRNOK.getOrder());
        mOutputStream.write(contentLength);
        mOutputStream.write(0x03);
        mOutputStream.write(verifyCode);
        mOutputStream.write(content);
        mOutputStream.write(0x24);
        mOutputStream.flush();
    }

    private void sendDevInit() throws IOException {
        Log.d(TAG, "DEVINIT order = " + ToolUtil.byte2HexStr(PrinterOrder.DEVINIT.getOrder()));

        mOutputStream.write(PrinterOrder.DEVINIT.getOrder());
        mOutputStream.write(new byte[]{0x00, 0x00, 0x00, 0x00});
        mOutputStream.write(new byte[]{0x03, 0x00, 0x00});
        mOutputStream.write(new byte[]{0x24});
        mOutputStream.flush();
    }

    /**
     * 发送设备的信息
     * @throws IOException
     */
    private void sendDevInfo() throws IOException {
        JSONObject devJson = new JSONObject();
        try {
            devJson.put("deviceid", mDevId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String devStr = devJson.toString();

        byte[] devInfoBytes = new byte[0];
        try {
            devInfoBytes = devStr.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] devLength = ToolUtil.intToBytes(devStr.length());
        byte[] verifyCode = ToolUtil.toMD5Bytes(devInfoBytes);

        Log.d(TAG, "devLength = " + ToolUtil.byte2HexStr(devLength));
        Log.d(TAG, "verifyCode = " + ToolUtil.byte2HexStr(verifyCode));
        Log.d(TAG, "devInfoBytes = " + ToolUtil.byte2HexStr(devInfoBytes));

        mOutputStream.write(PrinterOrder.DEVINFO.getOrder());
        mOutputStream.write(devLength);
        mOutputStream.write(verifyCode);
        mOutputStream.write(devInfoBytes);
    }

    /**
     * 连接服务器
     */
    private void connectToServer() {
        mClient = new Socket();
        SocketAddress serverAddress = new InetSocketAddress(mServerIP, mServerPort);

        while (true) {
            try {
                mClient.connect(serverAddress, 60000);

                mClient.setSoTimeout(3000);
                mInputStream = mClient.getInputStream();
                mOutputStream = mClient.getOutputStream();

                break;
            }
            catch (SocketTimeoutException ste){
                ste.printStackTrace();

                EventBus.getDefault().post(new ExceptionEvent(ste));
            }
            catch (IOException e) {
                e.printStackTrace();

                EventBus.getDefault().post(new ExceptionEvent(e));
            }
        }
    }
}
