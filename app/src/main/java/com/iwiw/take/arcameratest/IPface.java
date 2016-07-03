package com.iwiw.take.arcameratest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by takeshi on 02/07/16.
 */
public class IPface implements ImageProcessing {
    public enum FILTER_TYPE {
        RECTANGLE,
        OVERLAY,
        MOSAIC;
    }

    private Activity m_parentActivity;
    private CascadeClassifier m_cascadeClassifier;
    private FILTER_TYPE m_filterType;
    private Mat m_overlayImage;
    private boolean m_isInitialized = false;

    public IPface(Activity activity, FILTER_TYPE filterType){
        m_parentActivity = activity;
        m_filterType = filterType;
    }

    public void init() {
        m_isInitialized = true;
        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = m_parentActivity.getResources().openRawResource(R.raw.haarcascade_frontalface_default);
            File cascadeDir = m_parentActivity.getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");
            FileOutputStream os = new FileOutputStream(cascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cascade classifier
            m_cascadeClassifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
            m_cascadeClassifier.load(cascadeFile.getAbsolutePath());

            if (m_cascadeClassifier.empty()) {
                m_cascadeClassifier = null;
                Utility.logError("Error to load cascade classifier");
            }
            cascadeDir.delete();
            cascadeFile.delete();
        } catch (Exception e) {
            Utility.logError(e.getMessage());
        }


        Bitmap bmp = BitmapFactory.decodeResource(m_parentActivity.getResources(), R.mipmap.laughing_man);
        m_overlayImage = new Mat();
        Utils.bitmapToMat(bmp, m_overlayImage);
    }

    @Override
    public Mat doProcess(Mat matInput) {
        if(m_isInitialized == false) init();
        Mat matGray = new Mat();
        Imgproc.cvtColor(matInput, matGray, Imgproc.COLOR_RGBA2GRAY);
        MatOfRect faces = new MatOfRect();

        if (m_cascadeClassifier != null) {
            m_cascadeClassifier.detectMultiScale(matGray, faces, 1.2, 3, 0,
                    new Size(matGray.height()*0.2, matGray.height()*0.2), new Size());
        }

        Rect[] facesArray = faces.toArray();
        switch (m_filterType){
            case RECTANGLE:
                for (int i = 0; i <facesArray.length; i++) {
                    Imgproc.rectangle(matInput, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
                }
                break;
            case OVERLAY:
                for (int i = 0; i <facesArray.length; i++) {
                    Point [] srcTri = new Point[]{
                            new Point(0.0f, 0.0f),
                            new Point(m_overlayImage.width(), 0.0f),
                            new Point(m_overlayImage.width(), m_overlayImage.height()),
                    };
                    Point [] dstTri = new Point[] {
                            new Point(facesArray[i].tl().x, facesArray[i].tl().y),
                            new Point(facesArray[i].br().x, facesArray[i].tl().y),
                            new Point(facesArray[i].br().x, facesArray[i].br().y),
                    };
                    Mat affineTrans = Imgproc.getAffineTransform(new MatOfPoint2f(srcTri), new MatOfPoint2f(dstTri));
                    Imgproc.warpAffine(m_overlayImage, matInput, affineTrans, matInput.size(), Imgproc.INTER_LINEAR, Core.BORDER_TRANSPARENT, new Scalar(0, 0, 0));
                }
                break;
            case MOSAIC:
                break;
            default:
                break;
        }

        return matInput;
    }
}
