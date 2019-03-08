package cn.com.start.cloudprinter.startcloudprinter.order;

/**
 * Created by lutery on 2017/11/29.
 */

public enum PrinterOrder {

    DEVINFO((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00),
    DEVINIT((byte)0x11, (byte)0x00, (byte)0x00, (byte)0x00),
    PRNDATA((byte)0xf0, (byte)0x00, (byte)0x00, (byte)0x00),
    PRNOK((byte)0x0f, (byte)0x00, (byte)0x00, (byte)0x00),

    ;

    private byte[] order = new byte[4];
    PrinterOrder(byte one, byte two, byte three, byte four){
        order[0] = one;
        order[1] = two;
        order[2] = three;
        order[3] = four;
    }

    public byte[] getOrder(){
        return order;
    }
}
