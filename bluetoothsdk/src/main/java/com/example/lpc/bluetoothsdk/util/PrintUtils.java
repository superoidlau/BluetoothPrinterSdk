package com.example.lpc.bluetoothsdk.util;

/**
 * Description: 蓝牙打印机指令的定义
 * Created by lpc on 2017/3/20.
 */
public class PrintUtils {

    //初始化打印机
    public static final byte[] INIT_PRINT = {0x1B,0x40};

    //默认的行间距：33
    public static final byte[] DEFAULT_LINE_SPACE = {0x1B,0x32};

    //设置打印速度
    public static final byte[] PRINT_SPEED_LOW = {0x1C,0x73,0x00};          //低速
    public static final byte[] PRINT_SPEED_MIDDLE = {0x1C,0x73,0x01};       //中速
    public static final byte[] PRINT_SPEED_HIGH = {0x1C,0x73,0x02};         //高速

    //对齐方式
    public static final byte[] PRINT_ALIGN_LEFT = {0x1B,0x61,0x00};         //左对齐
    public static final byte[] PRINT_ALIGN_MIDDLE = {0x1B,0x61,0x01};       //居中对齐
    public static final byte[] PRINT_ALIGN_RIGHT = {0x1B,0x61,0x02};        //右对齐

    //设置粗体
    public static final byte[] PRINT_SET_BOLD = {0x1B,0x45,0x01};
    //解除粗体模式
    public static final byte[] PRINT_REMOVE_BOLD = {0x1B,0x45,0x00};

}
