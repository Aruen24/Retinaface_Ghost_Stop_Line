package CnrtNet;


import static java.lang.Math.abs;
import static java.lang.Math.max;

public class Box {
    public  int[] box;       //left:box[0],top:box[1],right:box[2],bottom:box[3]
    public  float score;    //probability
    public  float[] bbr;    //bounding box regression
    public  boolean deleted;
    public  float[] landmark; //facial landmark.只有ONet输出Landmark
    public  float[] angles;
    public  int track_id;
    Box(){
        box=new int[]{0,0,0,0};
        bbr=new float[4];
        deleted=false;
        landmark = new float[10];
        angles=new float[3];
        track_id = -1;
    }
    public int left(){return box[0];}
    public int right(){return box[2];}
    public int top(){return box[1];}
    public int bottom(){return box[3];}
    public int width(){return box[2]-box[0];}
    public int height(){return box[3]-box[1];}
    //转为rect
//    public Rect transform2Rect(){
//        Rect rect=new Rect();
//        rect.left= Math.round(box[0]);
//        rect.top= Math.round(box[1]);
//        rect.right= Math.round(box[2]);
//        rect.bottom= Math.round(box[3]);
//        return  rect;
//    }
    //面积
    public  int area(){
        return width()*height();
    }
    //Bounding Box Regression
    public void calibrate(){
        int w=box[2]-box[0]+1;
        int h=box[3]-box[1]+1;
        box[0]=(int)(box[0]+w*bbr[0]);
        box[1]=(int)(box[1]+h*bbr[1]);
        box[2]=(int)(box[2]+w*bbr[2]);
        box[3]=(int)(box[3]+h*bbr[3]);
        for (int i=0;i<4;i++) bbr[i]=0.0f;
    }
    //当前box转为正方形
    public void toSquareShape(){
        int w=width();
        int h=height();
        if (w>h){
            box[1]-=(w-h)/2;
            box[3]+=(w-h+1)/2;
        }else{
            box[0]-=(h-w)/2;
            box[2]+=(h-w+1)/2;
        }
    }
    //当前box转为正方形并扩大边界
    public void toSquareShape(int margin){
        int w=width();
        int h=height();
        if (w>h){
            box[1]-=(w-h)/2;
            box[3]+=(w-h+1)/2;
        }else{
            box[0]-=(h-w)/2;
            box[2]+=(h-w+1)/2;
        }

        box[0] -= margin/2;
        box[1] -= margin/2;
        box[2] += margin/2;
        box[3] += margin/2;
    }
    //当前box转为正方形并按比例扩大边界
    public void toSquareShape(float expandPercent){
        assert expandPercent >= 0;
        int w=width();
        int h=height();
        if (w>h){
            box[1]-=(w-h)/2;
            box[3]+=(w-h+1)/2;
        }else{
            box[0]-=(h-w)/2;
            box[2]+=(h-w+1)/2;
        }

        int expand = (int)(width() * expandPercent / 2);

        box[0] -= expand;
        box[1] -= expand;
        box[2] += expand;
        box[3] += expand;
    }
    //防止边界溢出，并维持square大小
    public void limit_square(int w,int h){
        if (box[0]<0 || box[1]<0){
            int len=max(-box[0],-box[1]);
            box[0]+=len;
            box[1]+=len;
        }
        if (box[2]>=w || box[3]>=h){
            int len=max(box[2]-w+1,box[3]-h+1);
            box[2]-=len;
            box[3]-=len;
        }
    }
    public void limit_square(){
        int w=width();
        int h=height();
        if (box[0]<0 || box[1]<0){
            int len=max(-box[0],-box[1]);
            box[0]+=len;
            box[1]+=len;
        }
        if (box[2]>=w || box[3]>=h){
            int len=max(box[2]-w+1,box[3]-h+1);
            box[2]-=len;
            box[3]-=len;
        }
    }
    public void limit_square2(int w,int h){
        if (width() > w) box[2]-=width()-w;
        if (height()> h) box[3]-=height()-h;
        if (box[0]<0){
            int sz=-box[0];
            box[0]+=sz;
            box[2]+=sz;
        }
        if (box[1]<0){
            int sz=-box[1];
            box[1]+=sz;
            box[3]+=sz;
        }
        if (box[2]>=w){
            int sz=box[2]-w+1;
            box[2]-=sz;
            box[0]-=sz;
        }
        if (box[3]>=h){
            int sz=box[3]-h+1;
            box[3]-=sz;
            box[1]-=sz;
        }
    }

    public void calAngles() {
        // left eye
        int x1 = (int)landmark[0];
        int y1 = (int)landmark[5];

        // right eye
        int x2 = (int)landmark[1];
        int y2 = (int)landmark[6];

        // nose
        int x3 = (int)landmark[2];
        int y3 = (int)landmark[7];

        // left side of mouth
        int x4 = (int)landmark[3];
        int y4 = (int)landmark[8];

        // right side of mouth
        int x5 = (int)landmark[4];
        int y5 = (int)landmark[9];

        // distant of eyes
        int x_dist = x1 - x2;
        int y_dist = y1 - y2;

        // 旋转角
        double circle_angle_tan = (double)y_dist / x_dist;
        angles[0] = (float)Math.toDegrees(Math.atan(circle_angle_tan));

        // mid point of 2 eyes
        int x_mid_1 = (x1 + x2) / 2;
        int y_mid_1 = (y1 + y2) / 2;

        // mid point of mouth
        int x_mid_2 = (x4 + x5) / 2;
        int y_mid_2 = (y4 + y5) / 2;

        // 两眼中点, 嘴中点, 鼻点构成的三角形三边长的平方
        double a_sqr = Math.pow(x3-x_mid_2, 2) + Math.pow(y3-y_mid_2, 2);
        double b_sqr = Math.pow(x_mid_2-x_mid_1, 2) + Math.pow(y_mid_2-y_mid_1, 2);
        double c_sqr = Math.pow(x_mid_1-x3, 2) + Math.pow(y_mid_1-y3, 2);

        // 判断鼻子方向, tmp>0鼻子在右侧, tmp<0鼻子在左侧
        int tmp = (y_mid_2-y_mid_1)*x3 + (x_mid_1-x_mid_2)*y3 - x_mid_1*y_mid_2 + x_mid_2*y_mid_1;
        int side = 1;
        if(tmp < 0) {
            side = -1;
        }

        // 余弦定理
        double left_right_cosine = (b_sqr+c_sqr-a_sqr) / (2*Math.sqrt(b_sqr)*Math.sqrt(c_sqr));
        double nose_angle = Math.toDegrees(Math.acos(Math.min(left_right_cosine, 0.99999)));
        angles[1] = (float)Math.toDegrees(Math.asin(Math.min(nose_angle,36.0)/36.0)) * side;

        // 两个中点分别到鼻的y轴距离
        int y1_dist = y_mid_1 - y3;
        int y2_dist = y_mid_2 - y3;

        // tan=(a-b)/(a(b+1))*cot(theta) a=1.15
        double b = abs(y1_dist/(double)y2_dist);
        double up_down_tan = 0.0;
        if(b > 1.5) {
            up_down_tan = (1.45-b)/(1.45*(b+1)) * (Math.cos(Math.PI/180.0*36)/Math.sin(Math.PI/180.0*36));
        } else if(b > 0.8) {
            up_down_tan = (1.15-b)/(1.15*(b+1)) * (Math.cos(Math.PI/180.0*36)/Math.sin(Math.PI/180.0*36));
        } else {
            up_down_tan = (0.795-b)/(0.795*(b+1)) * (Math.cos(Math.PI/180.0*36)/Math.sin(Math.PI/180.0*36));
        }
        angles[2] = (float)Math.toDegrees(Math.atan(up_down_tan));
    }
}





//import static java.lang.Math.abs;
//import static java.lang.Math.max;
//
//public class Box {
//    public  int[] box;       //left:box[0],top:box[1],right:box[2],bottom:box[3]
//    public  float score;    //probability
//    public  float[] bbr;    //bounding box regression
//    public  boolean deleted;
//    public  float[] landmark; //facial landmark.只有ONet输出Landmark
//    public  float[] angles;
//    public  int track_id;
//    Box(){
//        box=new int[]{0,0,0,0};
//        bbr=new float[4];
//        deleted=false;
//        landmark = new float[10];
//        angles=new float[3];
//        track_id = -1;
//    }
//    public int left(){return box[0];}
//    public int right(){return box[2];}
//    public int top(){return box[1];}
//    public int bottom(){return box[3];}
//    public int width(){return box[2]-box[0];}
//    public int height(){return box[3]-box[1];}
//    //转为rect
////    public Rect transform2Rect(){
////        Rect rect=new Rect();
////        rect.left= Math.round(box[0]);
////        rect.top= Math.round(box[1]);
////        rect.right= Math.round(box[2]);
////        rect.bottom= Math.round(box[3]);
////        return  rect;
////    }
//    //面积
//    public  int area(){
//        return width()*height();
//    }
//    //Bounding Box Regression
//    public void calibrate(){
//        int w=box[2]-box[0]+1;
//        int h=box[3]-box[1]+1;
//        box[0]=(int)(box[0]+w*bbr[0]);
//        box[1]=(int)(box[1]+h*bbr[1]);
//        box[2]=(int)(box[2]+w*bbr[2]);
//        box[3]=(int)(box[3]+h*bbr[3]);
//        for (int i=0;i<4;i++) bbr[i]=0.0f;
//    }
//    //当前box转为正方形
//    public void toSquareShape(){
//        int w=width();
//        int h=height();
//        if (w>h){
//            box[1]-=(w-h)/2;
//            box[3]+=(w-h+1)/2;
//        }else{
//            box[0]-=(h-w)/2;
//            box[2]+=(h-w+1)/2;
//        }
//    }
//    //当前box转为正方形并扩大边界
//    public void toSquareShape(int margin){
//        int w=width();
//        int h=height();
//        if (w>h){
//            box[1]-=(w-h)/2;
//            box[3]+=(w-h+1)/2;
//        }else{
//            box[0]-=(h-w)/2;
//            box[2]+=(h-w+1)/2;
//        }
//
//        box[0] -= margin/2;
//        box[1] -= margin/2;
//        box[2] += margin/2;
//        box[3] += margin/2;
//    }
//    //当前box转为正方形并按比例扩大边界
//    public void toSquareShape(float expandPercent){
//        assert expandPercent >= 0;
//        int w=width();
//        int h=height();
//        if (w>h){
//            box[1]-=(w-h)/2;
//            box[3]+=(w-h+1)/2;
//        }else{
//            box[0]-=(h-w)/2;
//            box[2]+=(h-w+1)/2;
//        }
//
//        int expand = (int)(width() * expandPercent / 2);
//
//        box[0] -= expand;
//        box[1] -= expand;
//        box[2] += expand;
//        box[3] += expand;
//    }
//    //防止边界溢出，并维持square大小
//    public void limit_square(int w,int h){
//        if (box[0]<0 || box[1]<0){
//            int len=max(-box[0],-box[1]);
//            box[0]+=len;
//            box[1]+=len;
//        }
//        if (box[2]>=w || box[3]>=h){
//            int len=max(box[2]-w+1,box[3]-h+1);
//            box[2]-=len;
//            box[3]-=len;
//        }
//    }
//    public void limit_square(){
//        int w=width();
//        int h=height();
//        if (box[0]<0 || box[1]<0){
//            int len=max(-box[0],-box[1]);
//            box[0]+=len;
//            box[1]+=len;
//        }
//        if (box[2]>=w || box[3]>=h){
//            int len=max(box[2]-w+1,box[3]-h+1);
//            box[2]-=len;
//            box[3]-=len;
//        }
//    }
//    public void limit_square2(int w,int h){
//        if (width() > w) box[2]-=width()-w;
//        if (height()> h) box[3]-=height()-h;
//        if (box[0]<0){
//            int sz=-box[0];
//            box[0]+=sz;
//            box[2]+=sz;
//        }
//        if (box[1]<0){
//            int sz=-box[1];
//            box[1]+=sz;
//            box[3]+=sz;
//        }
//        if (box[2]>=w){
//            int sz=box[2]-w+1;
//            box[2]-=sz;
//            box[0]-=sz;
//        }
//        if (box[3]>=h){
//            int sz=box[3]-h+1;
//            box[3]-=sz;
//            box[1]-=sz;
//        }
//    }
//
//    public void calAngles() {
//        // left eye
//        int x1 = (int)landmark[0];
//        int y1 = (int)landmark[5];
//
//        // right eye
//        int x2 = (int)landmark[1];
//        int y2 = (int)landmark[6];
//
//        // nose
//        int x3 = (int)landmark[2];
//        int y3 = (int)landmark[7];
//
//        // left side of mouth
//        int x4 = (int)landmark[3];
//        int y4 = (int)landmark[8];
//
//        // right side of mouth
//        int x5 = (int)landmark[4];
//        int y5 = (int)landmark[9];
//
//        // distant of eyes
//        int x_dist = x1 - x2;
//        int y_dist = y1 - y2;
//
//        // 旋转角
//        double circle_angle_tan = (double)y_dist / x_dist;
//        angles[0] = (float)Math.toDegrees(Math.atan(circle_angle_tan));
//
//        // mid point of 2 eyes
//        int x_mid_1 = (x1 + x2) / 2;
//        int y_mid_1 = (y1 + y2) / 2;
//
//        // mid point of mouth
//        int x_mid_2 = (x4 + x5) / 2;
//        int y_mid_2 = (y4 + y5) / 2;
//
//        // 两眼中点, 嘴中点, 鼻点构成的三角形三边长的平方
//        double a_sqr = Math.pow(x3-x_mid_2, 2) + Math.pow(y3-y_mid_2, 2);
//        double b_sqr = Math.pow(x_mid_2-x_mid_1, 2) + Math.pow(y_mid_2-y_mid_1, 2);
//        double c_sqr = Math.pow(x_mid_1-x3, 2) + Math.pow(y_mid_1-y3, 2);
//
//        // 判断鼻子方向, tmp>0鼻子在右侧, tmp<0鼻子在左侧
//        int tmp = (y_mid_2-y_mid_1)*x3 + (x_mid_1-x_mid_2)*y3 - x_mid_1*y_mid_2 + x_mid_2*y_mid_1;
//        int side = 1;
//        if(tmp < 0) {
//            side = -1;
//        }
//
//        // 余弦定理
//        double left_right_cosine = (b_sqr+c_sqr-a_sqr) / (2*Math.sqrt(b_sqr)*Math.sqrt(c_sqr));
//        double nose_angle = Math.toDegrees(Math.acos(Math.min(left_right_cosine, 0.99999)));
//        angles[1] = (float)Math.toDegrees(Math.asin(Math.min(nose_angle,36.0)/36.0)) * side;
//
//        // 两个中点分别到鼻的y轴距离
//        int y1_dist = y_mid_1 - y3;
//        int y2_dist = y_mid_2 - y3;
//
//        // tan=(a-b)/(a(b+1))*cot(theta) a=1.15
//        double b = abs(y1_dist/(double)y2_dist);
//        double up_down_tan = (1.15-b)/(1.15*(b+1)) * (Math.cos(Math.PI/180.0*36)/Math.sin(Math.PI/180.0*36));
//        angles[2] = (float)Math.toDegrees(Math.atan(up_down_tan));
//    }
//}



//旧的角度计算方式
//import static java.lang.Math.abs;
//import static java.lang.Math.max;
//import static java.lang.Math.min;
//
//public class Box {
//    // left:box[0],top:box[1],right:box[2],bottom:box[3]
//    public int[] box;
//    public float score;
//    // bounding box regression
//    public float[] bbr;
//    public boolean deleted;
//    public  float[] landmark;
//    public  float[] angles;
//
//    Box() {
//        box = new int[4];
//        bbr = new float[4];
//        landmark = new float[10];
//        deleted = false;
//        angles=new float[3];
//    }
//
//    public int left() {
//        return box[0];
//    }
//
//    public int right() {
//        return box[2];
//    }
//
//    public int top() {
//        return box[1];
//    }
//
//    public int bottom() {
//        return box[3];
//    }
//
//    public int width() {
//        return box[2] - box[0] + 1;
//    }
//
//    public int height() {
//        return box[3] - box[1] + 1;
//    }
//
//    //面积
//    public int area() {
//        return width() * height();
//    }
//
//    //Bounding Box Regression
//    public void calibrate() {
//        int w = box[2] - box[0] + 1;
//        int h = box[3] - box[1] + 1;
//        box[0] = (int) (box[0] + w * bbr[0]);
//        box[1] = (int) (box[1] + h * bbr[1]);
//        box[2] = (int) (box[2] + w * bbr[2]);
//        box[3] = (int) (box[3] + h * bbr[3]);
//        for (int i = 0; i < 4; i++) {
//            bbr[i] = 0.0f;
//        }
//    }
//
//    //当前box转为正方形
//    public void toSquareShape() {
//        int w = width();
//        int h = height();
//        if (w > h) {
//            box[1] -= (w - h) / 2;
//            box[3] += (w - h + 1) / 2;
//        } else {
//            box[0] -= (h - w) / 2;
//            box[2] += (h - w + 1) / 2;
//        }
//    }
//
//    //防止边界溢出，并维持square大小
//    public void limit_square(int w, int h) {
//        if (box[0] < 0 || box[1] < 0) {
//            int len = max(-box[0], -box[1]);
//            box[0] += len;
//            box[1] += len;
//        }
//        if (box[2] >= w || box[3] >= h) {
//            int len = max(box[2] - w + 1, box[3] - h + 1);
//            box[2] -= len;
//            box[3] -= len;
//        }
//    }
//
//    public void limit_square2(int w, int h) {
//        if (width() > w) {
//            box[2] -= width() - w;
//        }
//        if (height() > h) {
//            box[3] -= height() - h;
//        }
//        if (box[0] < 0) {
//            int sz = -box[0];
//            box[0] += sz;
//            box[2] += sz;
//        }
//        if (box[1] < 0) {
//            int sz = -box[1];
//            box[1] += sz;
//            box[3] += sz;
//        }
//        if (box[2] >= w) {
//            int sz = box[2] - w + 1;
//            box[2] -= sz;
//            box[0] -= sz;
//        }
//        if (box[3] >= h) {
//            int sz = box[3] - h + 1;
//            box[3] -= sz;
//            box[1] -= sz;
//        }
//    }
//
//    public void calAngles() {
//        // left eye
//        int x1 = (int)landmark[0];
//        int y1 = (int)landmark[5];
//
//        // right eye
//        int x2 = (int)landmark[1];
//        int y2 = (int)landmark[6];
//
//        // nose
//        int x3 = (int)landmark[2];
//        int y3 = (int)landmark[7];
//
//        // left side of mouth
//        int x4 = (int)landmark[3];
//        int y4 = (int)landmark[8];
//
//        // right side of mouth
//        int x5 = (int)landmark[4];
//        int y5 = (int)landmark[9];
//
//        // distant of eyes
//        int x_dist = x1 - x2;
//        int y_dist = y1 - y2;
//
//        // cal cosine value and angle degrees
//        double circle_angle_cosine = (double)abs(x_dist) / Math.sqrt(x_dist*x_dist + y_dist*y_dist);
//        angles[0] = (float)Math.toDegrees(Math.acos(min(circle_angle_cosine, 0.99999)));
//
//        // mid point of 2 eyes
//        int x_mid_1 = (x1 + x2) / 2;
//        int y_mid_1 = (y1 + y2) / 2;
//
//        // mid point of mouth
//        int x_mid_2 = (x4 + x5) / 2;
//        int y_mid_2 = (y4 + y5) / 2;
//
//        // 两眼中点, 嘴中点, 鼻点构成的三角形三边长的平方
//        double a_sqr = Math.pow(x3-x_mid_2, 2) + Math.pow(y3-y_mid_2, 2);
//        double b_sqr = Math.pow(x_mid_2-x_mid_1, 2) + Math.pow(y_mid_2-y_mid_1, 2);
//        double c_sqr = Math.pow(x_mid_1-x3, 2) + Math.pow(y_mid_1-y3, 2);
//
//        // 余弦定理
//        double left_right_cosine = (double)(b_sqr+c_sqr-a_sqr) / (2*Math.sqrt(b_sqr)*Math.sqrt(c_sqr));
//        angles[1] = (float)Math.toDegrees(Math.acos(min(Math.pow(left_right_cosine,1.7), 0.99999)));
//
//        // 两个中点分别到鼻的y轴距离
//        int y1_dist = y_mid_1 - y3;
//        int y2_dist = y_mid_2 - y3;
//
//        // 两距离之差比之和作为正弦值
//        double up_down_sine = (double)(abs(abs(y1_dist) - abs(y2_dist))) / (double)(abs(y1_dist) + abs(y2_dist));
//        angles[2] = (float)Math.toDegrees(Math.asin(min(up_down_sine, 0.99999)));
//    }
//}
