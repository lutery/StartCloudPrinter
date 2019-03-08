package cn.com.start.cloudprinter.startcloudprinter.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lutery on 2017/11/29.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionEvent {

    private Exception mException;
}
