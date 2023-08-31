package CnrtNet;

//import com.smdt.mips.constv.Const;


import java.awt.image.BufferedImage;
import java.io.*;

public class ImageUtiles {


    /**
     * 根据图片路径，把图片转为byte数组
     * @param imgPath  图片路径
     * @return      byte[]
     */
    public static byte[] image2Bytes(String imgPath) {
        FileInputStream fin;
        byte[] bytes = null;
        try {
            fin = new FileInputStream(new File(imgPath));
            bytes = new byte[fin.available()];
            //将文件内容写入字节数组
            fin.read(bytes);
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bytes;
    }

//    public static byte[] image2Bytes(String imgPath) {
//        byte[] data = null;
//
//        FileImageInputStream input = null;
//        try {
//            input = new FileImageInputStream(new File(imgPath));  //报错的地方
//            ByteArrayOutputStream output = new ByteArrayOutputStream();
//            byte[] buf = new byte[1024];
//            int numBytesRead = 0;
//            while ((numBytesRead = input.read(buf)) != -1) {
//                output.write(buf, 0, numBytesRead);
//            }
//            data = output.toByteArray();
//            output.close();
//            input.close();
//        }
//        catch (FileNotFoundException ex1) {
//            ex1.printStackTrace();
//        }
//        catch (IOException ex1) {
//            ex1.printStackTrace();
//        }
//        return data;
//    }


    /**
     * 根据图像数据缓冲区，把图片转为byte数组
     * @param img  图像数据缓冲区
     * @return      rgb的byte[]
     */
    public static byte[] image2ByteRgb(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        byte[] rgb = new byte[w*h*3];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int val = img.getRGB(j, i);
                int red = (val >> 16) & 0xFF;
                int green = (val >> 8) & 0xFF;
                int blue = val & 0xFF;

                rgb[(i*w+j)*3] = (byte) red;
                rgb[(i*w+j)*3+1] = (byte) green;
                rgb[(i*w+j)*3+2] = (byte) blue;
            }
        }

        return rgb;
    }

    /**
     * 从输入流中获取数据
     * @param inStream 输入流
     * @return
     * @throws Exception
     */
    public static byte[] readInputStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while( (len=inStream.read(buffer)) != -1 ){
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }
}
