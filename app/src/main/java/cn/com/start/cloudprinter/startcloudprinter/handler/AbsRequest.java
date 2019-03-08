package cn.com.start.cloudprinter.startcloudprinter.handler;

import java.io.InputStream;
import java.io.OutputStream;

import lombok.Data;

/**
 * Created by lutery on 2017/12/25.
 */

@Data
public abstract class AbsRequest <T> {

    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected T content;
}
