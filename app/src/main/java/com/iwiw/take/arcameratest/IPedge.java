package com.iwiw.take.arcameratest;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Created by takeshi on 02/07/16.
 */
public class IPedge implements ImageProcessing {
    @Override
    public Mat doProcess(Mat matInput) {
        Mat matGray = new Mat();
        Imgproc.cvtColor(matInput, matGray, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.Canny(matGray, matGray, 50, 150);
        return matGray;
    }
}
