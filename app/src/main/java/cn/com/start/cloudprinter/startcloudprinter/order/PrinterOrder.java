package cn.com.start.cloudprinter.startcloudprinter.order;

/**
 * Created by lutery on 2017/11/29.
 */

public enum PrinterOrder {

    DEVINFO((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00),
    GETDEVINFO((byte)0x10, (byte)0x00, (byte)0x00, (byte)0x00),
    DEVINIT((byte)0x11, (byte)0x00, (byte)0x00, (byte)0x00),
    DEVSTATUS((byte)0xf1, (byte)0x00, (byte)0x00, (byte)0x00),
    GETSTATUS((byte)0x12, (byte)0x00, (byte)0x00, (byte)0x00),
    PRNDATA((byte)0xf0, (byte)0x00, (byte)0x00, (byte)0x00),
    PRNINFO((byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00),
    PRNOK((byte)0x0f, (byte)0x00, (byte)0x00, (byte)0x00),
    HEARTBEAT((byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00),
    RESULTINFO((byte)0x0f, (byte)0x00, (byte)0x00, (byte)0x00),
    WEIGHTINFO((byte)0xd1, (byte)0x00, (byte)0x00, (byte)0x00),
    LOGOUT((byte)0xdc, (byte)0x00, (byte)0x00, (byte)0x00),
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
