package com.kostianikov.pacs.model.preProcessing;

import com.kostianikov.pacs.controller.error.NoFaceException;
import lombok.extern.log4j.Log4j2;
import org.opencv.core.*;
import org.opencv.face.Face;
import org.opencv.face.Facemark;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
@Component
public class Preprocessor {

    final private String filenameFaceCascade;
    final private String filenameFacemark;
    private Facemark fm;
    private CascadeClassifier faceCascade;


    public Preprocessor(@Value("${preprocessor.filenameFaceCascade}") String filenameFaceCascade,
                        @Value("${preprocessor.filenameFacemark}") String filenameFacemark) {
        this.filenameFaceCascade = filenameFaceCascade;
        this.filenameFacemark = filenameFacemark;
    }

    @PostConstruct
    public void postConstruct(){
        faceCascade = new CascadeClassifier();

        fm = Face.createFacemarkLBF();
        fm.loadModel("data/faceLandmarkModel/lbfmodel.yaml");
        if (!faceCascade.load("data/haarCascade/haarcascade_frontalface_default.xml")) {
            System.err.println("--(!)Error loading face cascade: " + filenameFaceCascade);
            //System.exit(-100);
        }
    }

    public String preprocessing(String pathToVImage, String pathToRImage, String pathToDImage) throws NoFaceException {
        Mat conected = doconcat(pathToVImage, pathToRImage, faceCascade);

        String name  = UUID.randomUUID().toString()+".jpg";
        log.info(pathToDImage +"/"+name);
        output(pathToDImage +"/"+name, conected);

        return name;
    }

    private void output(String pathToDImage, Mat img) {
            //createdir(pathTodir);
            Imgcodecs.imwrite(pathToDImage, img);
            log.info("Concatenated image was written to:" + pathToDImage);

    }

    @Deprecated
    private void createdir(Path pathTodir) {
        if (!Files.exists(pathTodir)) {
            try {
                Files.createDirectory(pathTodir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Directory " + pathTodir + " created");
        } else {

            System.out.println("Directory already exists");
        }
    }



    private Mat doconcat(String pathToVImage, String pathToRImage, CascadeClassifier faceCascade) throws NoFaceException {
        Mat frameV = new Mat();
        // frame = Imgcodecs.imread("images/lm_15_out.jpg");
        frameV = Imgcodecs.imread(pathToVImage);

        Mat frameL = new Mat();
        // frame = Imgcodecs.imread("images/lm_15_out.jpg");
        frameL = Imgcodecs.imread(pathToRImage);

        MatOfPoint2f landmarksV = null;

        try {
            landmarksV = detectLandmarks(frameV, faceCascade);
        } catch (NoFaceException e) {
            throw new NoFaceException();
        }

        Mat frameRotatedV = rotate(landmarksV, frameV);

        MatOfPoint2f landmarkRotatedV = new MatOfPoint2f();

        try {
            landmarkRotatedV = detectLandmarks(frameRotatedV, faceCascade);
        } catch (NoFaceException e) {
            throw new NoFaceException();
        }

        MatOfPoint2f landmarksL = null;
        try {
            landmarksL = detectLandmarks(frameL, faceCascade);
        } catch (NoFaceException e) {
            // TODO Auto-generated catch block
            throw new NoFaceException();
        }

        Mat frameRotatedL = rotate(landmarksL, frameL);

        MatOfPoint2f landmarkRotatedL = new MatOfPoint2f();

        try {
            landmarkRotatedL = detectLandmarks(frameRotatedL, faceCascade);
        } catch (NoFaceException e) {
            // TODO Auto-generated catch block
            throw new NoFaceException();
        }

//        double distVeyesV = dictEyes(landmarkRotatedV);
//        double distVeyesL = dictEyes(landmarkRotatedL);
//
//        double scope = 0.0;
//        boolean vLarger = false;
//
//        if (distVeyesV / distVeyesL > 1) {
//            scope = distVeyesV / distVeyesL;
//            vLarger = true;
//        } else {
//            scope = distVeyesL / distVeyesV;
//            vLarger = false;
//        }

        double verticalSizeV = dictVertical(landmarkRotatedV) * 1.05;
        double horizontalSizeV = dictHorizontal(landmarkRotatedV) * 1.05;

        double verticalSizeL = dictVertical(landmarkRotatedL) * 1.05;
        double horizontalSizeL = dictHorizontal(landmarkRotatedL) * 1.05;

        Point pointV = pointCentr(landmarkRotatedV);

        pointV.x = (pointV.x - horizontalSizeV) < 0 ? 0 : (pointV.x - horizontalSizeV);
        pointV.y = (pointV.y - verticalSizeV) < 0 ? 0 : (pointV.y - verticalSizeV);

        horizontalSizeV = (horizontalSizeV * 2) + pointV.x < frameRotatedV.size().width ? (horizontalSizeV * 2) : frameRotatedV.size().width - pointV.x;
        verticalSizeV = (verticalSizeV * 2) + pointV.y < frameRotatedV.size().height ? (verticalSizeV * 2) : frameRotatedV.size().height - pointV.y;

        Rect rectCropV = new Rect(pointV, new Size(horizontalSizeV, verticalSizeV));

        Mat frameCroppedV = new Mat(frameRotatedV, rectCropV);

        Imgproc.resize(frameCroppedV, frameCroppedV, new Size(224, 224));
        // Imgcodecs.imwrite("images/crop_6_out.jpg", frameCroppedV);

        Point pointL = pointCentr(landmarkRotatedL);

        pointL.x = (pointL.x - horizontalSizeL) < 0 ? 0 : (pointL.x - horizontalSizeL);
        pointL.y = (pointL.y - verticalSizeL) < 0 ? 0 : (pointL.y - verticalSizeL);

        horizontalSizeL = (horizontalSizeL * 2) + pointL.x < frameRotatedL.size().width ? (horizontalSizeL * 2) : frameRotatedL.size().width - pointL.x;
        verticalSizeL = (verticalSizeL * 2) + pointL.y < frameRotatedL.size().height ? (verticalSizeL * 2) : frameRotatedL.size().height - pointL.y;

        Rect rectCropL = new Rect(pointL, new Size(horizontalSizeL, verticalSizeL));

        System.out.println("frameRotatedL" + frameRotatedL.size() + "rectCropL" + rectCropL.toString());

        Mat frameCroppedL = new Mat(frameRotatedL, rectCropL);

        Imgproc.resize(frameCroppedL, frameCroppedL, new Size(224, 224));

        List<Mat> frames = new ArrayList<Mat>();
        frames.add(frameCroppedV);
        frames.add(frameCroppedL);

        Mat conected = new Mat();
        Core.hconcat(frames, conected);

        return conected;
    }

    public MatOfPoint2f detectLandmarks(Mat frame, CascadeClassifier faceCascade) throws NoFaceException {
        Mat frameGray = new Mat();

        // Imgproc.resize(frame, frame, new Size(460, 460));
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(frameGray, frameGray);
        // -- Detect faces

        MatOfRect faces = detect(frame, faceCascade);

        ArrayList<MatOfPoint2f> landmarks;
        boolean flag = faces.size().height != 0.0;
        if (flag) {

            faces = returnOnlyBiggest(faces);

            landmarks = new ArrayList<MatOfPoint2f>();

            try {
                fm.fit(frameGray, faces, landmarks);
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        } else {
            throw (new NoFaceException());
        }
        return landmarks.get(0);
    }

    public MatOfRect detect(Mat frame, CascadeClassifier faceCascade) {
        Mat frameGray = new Mat();

        // Imgproc.resize(frame, frame, new Size(460, 460));
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(frameGray, frameGray);
        // -- Detect faces

        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(frameGray, faces);
        System.out.println(faces.size() + " faces found");

        return faces;
    }

    private MatOfRect returnOnlyBiggest(MatOfRect faces) {
        // peredelat` ??
        double rh = 0;
        double rw = 0;
        Rect rectTemp = new Rect();
        for (Rect rect : faces.toArray()) {
            if (rect.height > rh && rect.width > rw) {
                rectTemp = rect;
            }
        }

        return new MatOfRect(rectTemp);
    }

    private Mat rotate(MatOfPoint2f landmark, Mat frame) {

        double XL = (landmark.get(45, 0)[0] + landmark.get(42, 0)[0]) / 2;
        double YL = (landmark.get(45, 0)[1] + landmark.get(42, 0)[1]) / 2;
        double XR = (landmark.get(39, 0)[0] + landmark.get(36, 0)[0]) / 2;
        double YR = (landmark.get(39, 0)[1] + landmark.get(36, 0)[1]) / 2;
        // The coord. center
        double X0 = (XL + XR) / 2;
        double Y0 = (YL + YR) / 2;

        double dxx = XR - X0;
        double dyx = -(YR - Y0);

        double dxy = -(landmark.get(8, 0)[0] - X0);
        double dyy = landmark.get(8, 0)[1] - Y0;

        double X0c16 = (landmark.get(0, 0)[0] + landmark.get(16, 0)[0]) / 2;
        double Y0c16 = (landmark.get(0, 0)[1] + landmark.get(16, 0)[1]) / 2;

        double dxz = X0c16 - X0;
        double dyz = -(Y0c16 - Y0);

        double Kx = dyx / dxx;
        double Ky = dxy / dyy;
        double Kz = dyz / dxz;

        double Bz = -((X0c16 * Kz) - Y0c16);

        ////// System.out.println(Math.atan(Kx) + " " + Math.atan(Ky) + " " +
        ////// Math.atan(Kz));

        // Imgproc.line(frame, new Point(X0, Y0), new Point(XL, YL), new Scalar(0, 0,
        // 255), 2);
        // Imgproc.line(frame, new Point(X0, Y0), new Point(landmark.get(8, 0)[0],
        // landmark.get(8, 0)[1]), new Scalar(0, 255, 0), 2);
        // Imgproc.line(frame, new Point(X0, Y0), new Point(100, Kz * 100 + Bz), new
        // Scalar(255, 0, 0), 2);

        Point center = new Point(X0, Y0);
        // double angle = Math.toDegrees(Math.atan2(Y0c16 - Y0, X0c16 - X0));
        // double angle = Math.toDegrees(Math.atan2(YR - YL, XR - XL)) + 180;
        double angle = Math.toDegrees(Math.atan2(landmark.get(16, 0)[1] - landmark.get(0, 0)[1], landmark.get(16, 0)[0] - landmark.get(0, 0)[0]));

        Mat ft = new Mat();

        double scale = 1;
        Mat rotMat = Imgproc.getRotationMatrix2D(center, angle, scale);

        Mat warpRotateDst = new Mat();
        Imgproc.warpAffine(frame, ft, rotMat, frame.size());

        //Imgcodecs.imwrite("images/trans_8_out.jpg", ft);

        return ft;
    }

    private Point pointCentr(MatOfPoint2f landmark) {
        double XL = (landmark.get(45, 0)[0] + landmark.get(42, 0)[0]) / 2;
        double YL = (landmark.get(45, 0)[1] + landmark.get(42, 0)[1]) / 2;
        double XR = (landmark.get(39, 0)[0] + landmark.get(36, 0)[0]) / 2;
        double YR = (landmark.get(39, 0)[1] + landmark.get(36, 0)[1]) / 2;

        double XC = (XL + XR) / 2;
        double YC = (YL + YR) / 2;

        return new Point(XC, YC);
    }

    private double dictEyes(MatOfPoint2f landmark) {
        double XL = (landmark.get(45, 0)[0] + landmark.get(42, 0)[0]) / 2;
        double YL = (landmark.get(45, 0)[1] + landmark.get(42, 0)[1]) / 2;
        double XR = (landmark.get(39, 0)[0] + landmark.get(36, 0)[0]) / 2;
        double YR = (landmark.get(39, 0)[1] + landmark.get(36, 0)[1]) / 2;

        return Math.sqrt(Math.pow((XR - XL), 2) + Math.pow((YR - YL), 2));
    }

    private double dictHorizontal(MatOfPoint2f landmark) {
        double XL = (landmark.get(45, 0)[0] + landmark.get(42, 0)[0]) / 2;
        double YL = (landmark.get(45, 0)[1] + landmark.get(42, 0)[1]) / 2;
        double XR = (landmark.get(39, 0)[0] + landmark.get(36, 0)[0]) / 2;
        double YR = (landmark.get(39, 0)[1] + landmark.get(36, 0)[1]) / 2;

        double XC = (XL + XR) / 2;
        double YC = (YL + YR) / 2;

        double X0 = landmark.get(0, 0)[0];
        double Y0 = landmark.get(0, 0)[1];

        double sizeC0 = Math.abs(Math.sqrt(Math.pow((XC - X0), 2) + Math.pow((YC - Y0), 2)));

        double X16 = landmark.get(16, 0)[0];
        double Y16 = landmark.get(16, 0)[1];

        double sizeC16 = Math.abs(Math.sqrt(Math.pow((X16 - XC), 2) + Math.pow((Y16 - YC), 2)));

        return sizeC0 > sizeC16 ? sizeC0 : sizeC16;
    }

    private double dictVertical(MatOfPoint2f landmark) {
        double XL = (landmark.get(45, 0)[0] + landmark.get(42, 0)[0]) / 2;
        double YL = (landmark.get(45, 0)[1] + landmark.get(42, 0)[1]) / 2;
        double XR = (landmark.get(39, 0)[0] + landmark.get(36, 0)[0]) / 2;
        double YR = (landmark.get(39, 0)[1] + landmark.get(36, 0)[1]) / 2;

        double XC = (XL + XR) / 2;
        double YC = (YL + YR) / 2;

        double X8 = landmark.get(8, 0)[0];
        double Y8 = landmark.get(8, 0)[1];

        double sizeC8 = Math.abs(Math.sqrt(Math.pow((XC - X8), 2) + Math.pow((YC - Y8), 2)));

        return sizeC8;
    }


}
