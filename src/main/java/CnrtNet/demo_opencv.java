//package CnrtNet;
//
//import org.opencv.core.*;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.io.InputStream;
//
//
//public class demo {
//    public static void main(String[] args) {
//        try{
//            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//
//            Mat src= Imgcodecs.imread("./test_feature.png");
//            //读取图像到矩阵中
//            if(src.empty()){
//                throw new Exception("no file");
//            }
//
//            Mat dst=src.clone();
//            //复制矩阵进入dst
//
////            float scale=0.5f;
////            float width=src.width();
////            float height=src.height();
//
////            Imgproc.resize(src, dst, new Size(width*scale,height*scale));
////            Imgcodecs.imwrite("./resize0.5.jpg",dst);
////
////            scale=1.5f;
////            Imgproc.resize(src, dst, new Size(width*scale,height*scale));
////            Imgcodecs.imwrite("./resize1.5.jpg",dst);
//
//            Imgproc.resize(src, dst, new Size(112,112));
//            BufferedImage result = Mat2BufImg(dst, ".jpg");
//
//            Imgcodecs.imwrite("./resize400.jpg", dst);
//        }catch(Exception e){
//            System.out.println("例外：" + e);
//        }
//
//    }
//
//
//    public static BufferedImage Mat2BufImg (Mat matrix, String fileExtension) {
//        // convert the matrix into a matrix of bytes appropriate for
//        // this file extension
//        MatOfByte mob = new MatOfByte();
//        Imgcodecs.imencode(fileExtension, matrix, mob);
//        // convert the "matrix of bytes" into a byte array
//        byte[] byteArray = mob.toArray();
//        BufferedImage bufImage = null;
//        try {
//            InputStream in = new ByteArrayInputStream(byteArray);
//            bufImage = ImageIO.read(in);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return bufImage;
//    }
//
//}