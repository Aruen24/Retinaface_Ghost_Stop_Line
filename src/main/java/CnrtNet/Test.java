
package CnrtNet;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import java.awt.*;
import java.util.List;
import java.util.Vector;


public class Test {
    /**
     * 图片转成RGB byte数组
     * @param img
     * @return
     * @auther Aruen
     * @date 2020.10.29
     */
    public static byte[] image2ByteArr(BufferedImage img) {
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
                //System.out.println(String.valueOf((i*h+j)*3));
            }
        }
        return rgb;
    }

    /**
     * resize图片大小
     * @param src
     * @param newWidth
     * @param newHeight
     * @return
     * @throws IOException
     * @auther Aruen
     * @date 2020.10.29
     */
    public static BufferedImage resize(String src,int newWidth,int newHeight) {
        try {
            File srcFile = new File(src);
            BufferedImage img = ImageIO.read(srcFile);
            int w = img.getWidth();
            int h = img.getHeight();
            BufferedImage dimg = new BufferedImage(newWidth, newHeight, img.getType());
            Graphics2D g = dimg.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, newWidth, newHeight, 0, 0, w, h, null);
            g.dispose();
//            ImageIO.write(dimg, "jpg", toFile);
            return dimg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 图片在多级目录下处理
     * @param picPath
     * @return
     * @auther Aruen
     * @date 2020.10.30
     */
    public static List<String> multifile(String picPath){
        List<String> files = new ArrayList<String>();
        File file = new File(picPath);
        if(file!=null){// 判断对象是否为空
            if(!file.isFile()){
                File[] tempList = file.listFiles() ;// 列出全部的文件
                for(int i=0;i<tempList.length;i++){
                    if(tempList[i].isDirectory()){// 如果是目录，二级目录
                        File[] p_path = tempList[i].listFiles() ;// 列出全部的文件
                        for(int j=0;j<p_path.length;j++){
                            if(p_path[j].isDirectory()){// 如果是目录,三级目录
                                File[] p_path_result = p_path[j].listFiles() ;// 列出全部的文件
                                for(int k=0;k<p_path_result.length;k++){
                                    files.add(p_path_result[k].toString());
                                }

                            }else if(p_path[j].isFile()){
                                files.add(p_path[j].toString());// 如果不是目录，输出路径
                            }
                        }
                    }else if(tempList[i].isFile()){
                        files.add(tempList[i].toString());// 如果不是目录，输出路径
                    }
                }
            }else{
                files.add(file.toString());// 如果不是目录，输出路径
            }
            return files;
        }else{
            return null;
        }
    }

    public static Vector<Box> detectExpandFacesByRgb(float[][] result, Integer pic_resize) {

        Vector<Box> boxes = new Vector<Box>();
        for(int k=0; k<result.length; ++k) {
            Box box = new Box();
            box.box[0] = (int)result[k][0];
            box.box[1] = (int)result[k][1];
            box.box[2] = (int)result[k][2];
            box.box[3] = (int)result[k][3];

            for(int j=0; j<10; ++j) {
                box.landmark[j] = result[k][j+6];
            }

            box.score = result[k][5];

            box.calAngles();
            box.limit_square(pic_resize, pic_resize);
            boxes.add(box);
        }

        return boxes;
    }

    /**
     * 获得处理结果
     * @param modelPath
     * @param picSize
     * @param fileResult
     **@auther Aruen
     * @date 2020.10.30
     */
    public static void getResultByRgb(String modelPath, int picSize, List<String> fileResult){
        CnrtNet net = new CnrtNet(modelPath, 0, 0);
        int count = 0;
        for(String img_name : fileResult) {
            BufferedImage bi = null;

            try {
                bi = ImageIO.read(new File(img_name));
//                bi = resize(img_name, picSize, picSize);
            } catch (Exception e) {
                e.printStackTrace();
            }

            byte[] rgb = image2ByteArr(bi);
            long startTime = System.currentTimeMillis();
//            float[][] output = net.defaultForward(rgb, bi.getWidth(), bi.getHeight(), 0);
            float[][] output = net.defaultForwardExpandImgRgb(rgb, bi.getWidth(), bi.getHeight(), picSize, 0);
//            float[][] output = net.defaultForwardResizeRgb(rgb, picSize, picSize, 0);

            Vector<Box> boxes = detectExpandFacesByRgb(output, picSize);
            System.out.println("img_name："+img_name+"output.length"+output.length);
            long endTime = System.currentTimeMillis();


            for (int i = 0; i < output.length; i++) {
                System.out.println ( Arrays.toString (output[i]));
            }


            count++;
            System.out.println("模型计算耗时："+(endTime-startTime)+"ms******"+"处理图片数："+count+"----图片路径："+img_name);
        }
        net.release();
    }




    public static void main(String args[]) {
        String modelPath = args[0];
        String picPath = args[1];
        int picSize = Integer.parseInt(args[2]);

        List<String> fileResult = multifile(picPath);
        if(fileResult != null) {
            //得到结果，根据传入图片的byte数组，图片resize在java端处理
            getResultByRgb(modelPath, picSize, fileResult);
        }

    }
}
