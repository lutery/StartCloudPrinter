package cn.com.start.cloudprinter.startcloudprinter.handler;

import lombok.Data;

/**
 * Created by lutery on 2017/12/25.
 */

@Data
public abstract class AbsHandler {

    protected AbsHandler mNextHandler;

    public final void handleRequest(AbsRequest absRequest){
        if (this.handle(absRequest)){
            return;
        }

        if (this.mNextHandler != null){
            mNextHandler.handleRequest(absRequest);
        }
    }

    protected abstract boolean handle(AbsRequest request);
}
