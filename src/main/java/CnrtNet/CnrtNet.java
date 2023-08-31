package CnrtNet;

public class CnrtNet {
    static {
        System.loadLibrary("cnrtNet");
//        System.load("/home/wang/share/cnrtNetJNI/lib/libcnrtNet.so");
    }

    private long netPtr;
    private native long createNet(String model_path, int dev_id, int dev_channel);
    private native void releaseNet(long net);

    private native long getSession(long net);
    private native long forkSession(long net);
    private native void releaseSession(long sess);
    private native int jniSetCPUCore(int core1, int core2, int core3, int core4);

    //传入图片byte数组，不做resize
    public native float[][] forward(byte[] data, int width, int height, int data_type, long sess);
    //传入图片byte数组，并做resize，BGR->RGB，用于特征提取
    public native float[][] forwardResizeRgb(byte[] data, int width, int height, int resize, int data_type, long sess);
    //传入图片byte数组，并做resize、图片扩充，BGR，用于retinalface人脸检测
    public native float[][] forwardExpandImgRgb(byte[] data, int width, int height, int resize, int data_type, long sess);

    public CnrtNet(String model_path, int dev_id, int dev_channel) {
        netPtr = createNet(model_path, dev_id, dev_channel);
    }

    public void release() {
        releaseNet(netPtr);
    }

    /**
     * 绑核
     * @param number
     * @return
     */
    public int setCPUCore(int number) {
        if(number%4 == 0){
            return jniSetCPUCore(0,-1,-1,-1);
        }else if(number%4 == 1){
            return jniSetCPUCore(-1,1,-1,-1);
        }else if(number%4 == 2){
            return jniSetCPUCore(-1,-1,2,-1);
        }else {
            return jniSetCPUCore(-1,-1,-1,3);
        }
    }

    public void releaseNewSession(long sess) {
        releaseSession(sess);
    }

    public long getDefaultSession() {
        return getSession(netPtr);
    }

    public long forkSession() {
        return forkSession(netPtr);
    }

    public float[][] defaultForward(byte[] data, int width, int height, int data_type) {
        return forward(data, width, height, data_type, getDefaultSession());
    }

    public float[][] defaultForwardExpandImgRgb(byte[] data, int width, int height, int resize, int data_type) {
        return forwardExpandImgRgb(data, width, height, resize, data_type, getDefaultSession());
    }

    public float[][] defaultForwardResizeRgb(byte[] data, int width, int height, int resize, int data_type) {
        return forwardResizeRgb(data, width, height, resize, data_type, getDefaultSession());
    }
}
