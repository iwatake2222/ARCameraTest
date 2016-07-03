package com.iwiw.take.arcameratest;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

/**
 * Created by takeshi on 02/07/16.
 */
public interface ImageProcessing {
    Mat doProcess(Mat matInput);
}
