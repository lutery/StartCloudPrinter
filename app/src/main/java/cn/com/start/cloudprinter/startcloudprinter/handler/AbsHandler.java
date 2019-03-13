package cn.com.start.cloudprinter.startcloudprinter.handler;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.com.start.cloudprinter.startcloudprinter.event.ExceptionEvent;
import cn.com.start.cloudprinter.startcloudprinter.po.OrderObj;
import cn.com.start.cloudprinter.startcloudprinter.tool.verify.IVerify;
import cn.com.start.cloudprinter.startcloudprinter.tool.verify.VerifyType;
import cn.com.start.cloudprinter.startcloudprinter.util.ToolUtil;
import lombok.Data;

/**
 * Created by lutery on 2017/12/25.
 */

@Data
public abstract class AbsHandler<MyRequest extends AbsRequest > {

    protected AbsHandler mNextHandler;
    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected IVerify mVerifyTool;

    public final void handleRequest(MyRequest absRequest){
        if (this.handle(absRequest)){
            return;
        }

        if (this.mNextHandler != null){
            mNextHandler.handleRequest(absRequest);
        }
    }

    protected final boolean handle(MyRequest request){
        try {
            inputStream = request.getInputStream();
            outputStream = request.getOutputStream();
            OrderObj orderObj = this.preProcess(request);
            return this.innerHandle(orderObj);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    protected OrderObj preProcess(MyRequest myRequest) throws IOException {
        assert myRequest instanceof BytesRequest;

        BytesRequest bytesRequest = (BytesRequest) myRequest;

        byte[] data = bytesRequest.getContent();

        if (data.length < 9) {
            EventBus.getDefault().post(new ExceptionEvent(new Exception("数据长度不对")));

            return null;
        }

        int length;

        InputStream inputStream = new ByteArrayInputStream(data);

        OrderObj orderObj = new OrderObj();

        byte[] readBufs = new byte[4];
        inputStream.read(readBufs);
        orderObj.setOrder(readBufs);
        inputStream.read(readBufs);
        orderObj.setLength(readBufs);
        readBufs = new byte[1];
        inputStream.read(readBufs);
        orderObj.setVerityType(readBufs[0]);

        if (orderObj.getVerityType() == VerifyType.MD5.getType()) {
            if (inputStream.available() > 16) {
                EventBus.getDefault().post(new ExceptionEvent(new Exception("校验长度错误")));
                return null;
            }

            readBufs = new byte[16];
            inputStream.read(readBufs);

        } else if (orderObj.getVerityType() == VerifyType.CRC8.getType()) {
            if (inputStream.available() > 1) {
                EventBus.getDefault().post(new ExceptionEvent(new Exception("校验长度错误")));
                return null;
            }

            readBufs = new byte[1];
            inputStream.read(readBufs);
        } else if (orderObj.getVerityType() == VerifyType.CRC32.getType()) {
            if (inputStream.available() > 3) {
                EventBus.getDefault().post(new ExceptionEvent(new Exception("校验长度错误")));
                return null;
            }

            readBufs = new byte[3];
            inputStream.read(readBufs);
        } else {
            if (inputStream.available() > 2) {
                EventBus.getDefault().post(new ExceptionEvent(new Exception("校验长度错误")));
                return null;
            }

            readBufs = new byte[2];
            inputStream.read(readBufs);
        }

        orderObj.setVerityCode(readBufs);

        int contentLength = ToolUtil.bytesToInt(orderObj.getLength());

        if (inputStream.available() <= contentLength) {
            return null;
        }

        readBufs = new byte[contentLength];
        inputStream.read(readBufs);

        orderObj.setContent(readBufs);

        if (mVerifyTool.verifyContent(orderObj.getVerityCode(), orderObj.getContent())) {
            EventBus.getDefault().post(new ExceptionEvent(new Exception("数据校验出错，请重新发送")));
            return null;
        }

        readBufs = new byte[1];
        inputStream.read(readBufs);

        orderObj.setEnd(readBufs[0]);

        return orderObj;
    }

    protected abstract boolean innerHandle(OrderObj orderObj);
}
