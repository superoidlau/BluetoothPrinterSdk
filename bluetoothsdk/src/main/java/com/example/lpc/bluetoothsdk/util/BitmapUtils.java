package com.example.lpc.bluetoothsdk.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

/**
 * Description:
 * Created by lpc on 2017/3/27.
 */
public class BitmapUtils {

    // nWidth必须为8的倍数,这个只需在上层控制即可
    // 之所以弄成一维数组，是因为一维数组速度会快一点
    private static int[] p0 = { 0, 0x80 };
    private static int[] p1 = { 0, 0x40 };
    private static int[] p2 = { 0, 0x20 };
    private static int[] p3 = { 0, 0x10 };
    private static int[] p4 = { 0, 0x08 };
    private static int[] p5 = { 0, 0x04 };
    private static int[] p6 = { 0, 0x02 };

    //
    public static void format_K_threshold(int[] orgpixels, int xsize,
                                          int ysize, byte[] despixels) {

        int graytotal = 0;
        int grayave = 128;
        int i, j;
        int gray;

        int k = 0;
        for (i = 0; i < ysize; i++) {

            for (j = 0; j < xsize; j++) {

                gray = orgpixels[k] & 0xff;
                graytotal += gray;
                k++;
            }
        }
        grayave = graytotal / ysize / xsize;

        // 二值化
        k = 0;
        for (i = 0; i < ysize; i++) {

            for (j = 0; j < xsize; j++) {

                gray = orgpixels[k] & 0xff;

                if (gray > grayave)
                    despixels[k] = 0;// white
                else
                    despixels[k] = 1;

                k++;
            }
        }
    }

    // 缩放，暂时需要public以便调试，完成之后不用这个。
    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {

        // load the origial Bitmap
        Bitmap BitmapOrg = bitmap;

        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        // calculate the scale
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the Bitmap
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);

        // make a Drawable from Bitmap to allow to set the Bitmap
        // to the ImageView, ImageButton or what ever
        return resizedBitmap;
    }

    // 转成灰度图
    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(1);     //设置颜色矩阵的饱和度，0为灰色，1为原图
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    /**
     * 将ARGB图转化为二值图，0代表黑，1代表白
     * */
    public static byte[] bitmapToBWPix(Bitmap bitmap){
        int mWidth = bitmap.getWidth();
        int mHeight = bitmap.getHeight();

        int[] pixels = new int[mWidth * mHeight];
        byte[] data = new byte[mWidth * mHeight];

        bitmap.getPixels(pixels, 0, mWidth, 0, 0,
                mWidth, mHeight);

//        format_K_dither16x16(pixels,bitmap.getWidth(), bitmap.getHeight(), data);
//        format_K_threshold(bitmap);
        format_K_threshold(pixels,bitmap.getWidth(), bitmap.getHeight(), data);
//        format_K_dither8x8(pixels,bitmap.getWidth(), bitmap.getHeight(), data);
        return data;
    }

    private static byte[] eachLinePixToCmd(byte[] src, int nWidth, int nMode) {
        int nHeight = src.length / nWidth;
        int nBytesPerLine = nWidth / 8;
        byte[] data = new byte[nHeight * (8 + nBytesPerLine)];
        int offset;
        int k = 0;
        for (int i = 0; i < nHeight; i++) {
            offset = i * (8 + nBytesPerLine);
            data[offset + 0] = 0x1d;
            data[offset + 1] = 0x76;
            data[offset + 2] = 0x30;
            data[offset + 3] = (byte) (nMode & 0x01);
            data[offset + 4] = (byte) (nBytesPerLine % 0x100);
            data[offset + 5] = (byte) (nBytesPerLine / 0x100);
            data[offset + 6] = 0x01;
            data[offset + 7] = 0x00;
            for (int j = 0; j < nBytesPerLine; j++) {
                data[offset + 8 + j] = (byte) (p0[src[k]] + p1[src[k + 1]]
                        + p2[src[k + 2]] + p3[src[k + 3]] + p4[src[k + 4]]
                        + p5[src[k + 5]] + p6[src[k + 6]] + src[k + 7]);
                k = k + 8;
            }
        }

        return data;
    }

    public static byte[] bitmapToByte(Bitmap mBitmap, int nWidth, int nMode){

        Log.e("lpc", "图片宽度：" + nWidth);
        // 先转黑白，再调用函数缩放位图
        // 不转黑白
        int width = ((nWidth + 7) / 8) * 8;
        int height = mBitmap.getHeight() * width / mBitmap.getWidth();
        height = ((height + 7) / 8) * 8;
        Bitmap rszBitmap = resizeImage(mBitmap, width, height);
        Bitmap grayBitmap = toGrayscale(rszBitmap);
        byte[] dithered = bitmapToBWPix(grayBitmap);

        byte[] data = eachLinePixToCmd(dithered, nWidth, nMode);

        return data;
    }
}
