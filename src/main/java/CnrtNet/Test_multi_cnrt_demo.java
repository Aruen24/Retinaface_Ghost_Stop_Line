package CnrtNet;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

public class Test_multi_cnrt_demo extends Thread{
    private int number;
    private CnrtNet net;
    private CountDownLatch latch;
    private List<String> fileResult;
    private int startIndex;
    private int endIndex;
    private int picSize;
    private long new_sess;
//    private String result_path;

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

    public static int[][][] image2FloatArr(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[][][] floatValues = new int[w][h][3];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int val = img.getRGB(j, i);
                floatValues[j][i][0] = (val >> 16) & 0xFF;
                floatValues[j][i][1] = (val >> 8) & 0xFF;
                floatValues[j][i][2] = val & 0xFF;
                //System.out.println(Arrays.toString(floatValues[j][i]));
            }
        }
        return floatValues;
    }


    /**
     * 图片在多级目录下处理
     * @param picPath
     * @return
     * @auther Aruen
     * @date 2020.10.30
     */
    public static List<String> multifile(String picPath, int pic_batch){
        List<String> files = new ArrayList<String>();
        File file = new File(picPath);
        if(file!=null){// 判断对象是否为空
            for (int h = 0; h < pic_batch; h++) {
                if (!file.isFile()) {
                    File[] tempList = file.listFiles();// 列出全部的文件

                    for (int i = 0; i < tempList.length; i++) {
                        if (tempList[i].isDirectory()) {// 如果是目录，二级目录
                            File[] p_path = tempList[i].listFiles();// 列出全部的文件
                            for (int j = 0; j < p_path.length; j++) {
                                if (p_path[j].isDirectory()) {// 如果是目录,三级目录
                                    File[] p_path_result = p_path[j].listFiles();// 列出全部的文件
                                    for (int k = 0; k < p_path_result.length; k++) {
                                        files.add(p_path_result[k].toString());
                                    }

                                } else if (p_path[j].isFile()) {
                                    files.add(p_path[j].toString());// 如果不是目录，输出路径
                                }
                            }
                        } else if (tempList[i].isFile()) {
                            files.add(tempList[i].toString());// 如果不是目录，输出路径
                        }
                    }


                } else {
                    files.add(file.toString());// 如果不是目录，输出路径
                }
            }
            return files;
        }else{
            return null;
        }
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
     * 获得处理结果
     * @param modelPath
     * @param picSize
     * @param fileResult
     **@auther Aruen
     * @date 2020.10.30
     */
    public static void getResult(String modelPath, int picSize, List<String> fileResult){
        CnrtNet net = new CnrtNet(modelPath, 0, 0);
        int count = 0;
        for(String img_name : fileResult) {
            BufferedImage bi = null;

            try {
//            bi = ImageIO.read(file);
                bi = resize(img_name, picSize, picSize);
            } catch (Exception e) {
                e.printStackTrace();
            }

            byte[] rgb = image2ByteArr(bi);
            long startTime = System.currentTimeMillis();
            float[][] output = net.defaultForward(rgb, bi.getWidth(), bi.getHeight(), 0);
            long endTime = System.currentTimeMillis();
            for (int i = 0; i < output.length; i++) {
                System.out.println ( Arrays.toString (output[i]));
            }
            count++;
            System.out.println("The model deal time："+(endTime-startTime)+"ms******"+"deal picture count："+count+"----picpath："+img_name);
        }
        net.release();
    }

    public Test_multi_cnrt_demo(CnrtNet net, int number, CountDownLatch latch, List<String> fileResult, int picSize, long new_sess, int startIndex, int endIndex)
    {
        this.net = net;
        this.number = number;
        this.latch = latch;
        this.fileResult = fileResult;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.picSize = picSize;
        this.new_sess = new_sess;
//        this.result_path = result_path;
    }

    public void run()
    {
        try {
            BufferedImage img_buff=null;
            Vector<Box> boxes=null;
//            net.setCPUCore(number);

            //输出结果到指定文件路径
//            PrintStream ps = new PrintStream(result_path);
//            System.setOut(ps);//把创建的打印输出流赋给系统。即系统下次向 ps输出
            List<String> subList = fileResult.subList(startIndex, endIndex);
            int count = 0;
            long avg_t = 0;
            long total_t = 0;
            for(String img_name : subList) {
                BufferedImage bi = null;

                try {
//            bi = ImageIO.read(file);
                    bi = resize(img_name, picSize, picSize);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                byte[] rgb = image2ByteArr(bi);
                long startTime = System.currentTimeMillis();
                float[][] new_output = net.forward(rgb, bi.getWidth(), bi.getHeight(), 0, new_sess);
                long endTime = System.currentTimeMillis();

//                for (int i = 0; i < new_output.length; i++) {
//                    System.out.println ( Arrays.toString (new_output[i]));
//                }
                avg_t = endTime - startTime;
                total_t += avg_t;
                count++;
                System.out.println("current thread name："+Thread.currentThread().getName()+"----model calcu time："+(endTime-startTime)+"ms******"+"deal pictures count："+count+"----picpath："+img_name);
            }
            System.out.println("model average time："+(total_t/subList.size())+"ms");
            net.releaseNewSession(new_sess);
        } finally {
            if (latch != null) {
                latch.countDown();
            }
        }
    }


    public static void main(String args[]) throws InterruptedException {
        // 模型路径
        String model_path = args[0];
        //处理图片路径
        String picture_path = args[1];
        //图片resize大小
        int picSize = Integer.parseInt(args[2]);


        //初始线程数
        String thread_num = args[3];

        //图片batch
        int pic_batch = Integer.parseInt(args[4]);

        int num = Integer.parseInt(thread_num);

        List<String> fileResult = multifile(picture_path, pic_batch);
        int length = fileResult.size();

        int baseNum = length / num;
        int remainderNum = length % num;
        int end  = 0;
        //创建推理对象
        CnrtNet net = new CnrtNet(model_path, 0, 0);

        CountDownLatch latch = new CountDownLatch(num);//初始化countDown
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < num; i++) {
            int start = end ;
            end = start + baseNum;
            if(i == (num-1)){
                end = length;
            }else if( i < remainderNum){
                end = end + 1;
            }
            long new_sess = net.forkSession();

            Thread thread = new Test_multi_cnrt_demo(net, i, latch, fileResult, picSize, new_sess, start , end);
            thread.start();
        }

        latch.await();//等待所有线程完成工作
        net.release(); //所有线程完成后释放c++对象
        long endTime = System.currentTimeMillis();
        long t1 = endTime - startTime;
    	System.out.println("total_time:"+t1+"ms");
    }
}
