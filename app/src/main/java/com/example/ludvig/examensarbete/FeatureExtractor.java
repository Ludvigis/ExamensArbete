package com.example.ludvig.examensarbete;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ludvig on 2018-04-04.
 */

public class FeatureExtractor {
    private Mat hsv;
    private Mat warped;
    public FeatureExtractor(){
        hsv = new Mat();
        warped = new Mat();
    }

    public Mat detectShapeCountCurve(Mat img) {
        Mat edges = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.Canny(img, edges, 100, 300);
//Imgproc.blur(edges, edges, new Size(2, 2));
        Imgproc.findContours(edges, contours, hierarchy,
                Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//third argument negative = draw all contours...
        Imgproc.drawContours(img, contours, -1, new Scalar(255,0,0),5);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        MatOfPoint2f curve = new MatOfPoint2f();
        for (int i = 0; i < contours.size(); i++) {
            Rect rect = Imgproc.boundingRect(contours.get(i));
            int centerX = rect.x + rect.width/2;
            int centerY = rect.y + rect.height/2;
            //marks center
            //Imgproc.circle(edges,new Point(centerX,centerY),8,new Scalar(0,0,0),-1);
            //Imgproc.circle(edges,new Point(centerX,centerY),4,new Scalar(255,255,255),-1);

            //writes out contour integer.
            //Imgproc.putText(edges, Integer.toString(i), new Point(centerX + 10,centerY), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255,100,255));
            MatOfPoint c = contours.get(i);
            curve.fromList(c.toList());

            Imgproc.approxPolyDP(curve, approxCurve, 0.02*
                    Imgproc.arcLength(curve, true), true);
            if(approxCurve.total() == 3) {
                System.out.println(i + ": " + " Triangel");
                Imgproc.putText(img,"T", new Point(centerX + 10,centerY), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0,0,0));
            }else if(approxCurve.total() == 4) {
                System.out.println(i + ": " + " Fyrkant");
                Imgproc.putText(img,"S", new Point(centerX + 10,centerY), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0,0,0));
            }else if(approxCurve.total() > 7) {
                System.out.println(i + ": " + " Cirkel");
                Imgproc.putText(img,"C", new Point(centerX + 10,centerY), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0,0,0));
            }else {
                System.out.println(i + ": " + approxCurve.total());

            }
        }
        return img;
    }
    public Mat detectShapeShapeFactor(Mat img){
        Mat edges = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.Canny(img, edges, 100, 300);
//Imgproc.blur(edges, edges, new Size(2, 2));
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);
        return img;
    }

    public Mat findSign(Mat img , Scalar lowerBound, Scalar upperBound, DrawMode drawMode) {
        Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV);
        Core.inRange(hsv, lowerBound, upperBound, hsv);
        Imgproc.morphologyEx(hsv, hsv,
                Imgproc.MORPH_OPEN,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                        new Size(7,7)) );
        Imgproc.morphologyEx(hsv, hsv,
                Imgproc.MORPH_CLOSE,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                        new Size(7,7)) );
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(hsv, contours, hierarchy, Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);
        double maxArea = 0;
        int maxAreaIndex = 0;
        MatOfPoint2f maxCurve = new MatOfPoint2f();
        MatOfPoint contour = null;
        MatOfPoint2f approxCurve = new MatOfPoint2f();

        //Mat imgCon = img.clone();   //TODO remove for performance? use drawmode to only draw useful stuff

        if(drawMode ==DrawMode.IMAGE){
            Imgproc.drawContours(img, contours, -1, new Scalar(255,0,0),3);
        }


        //TODO try finding x biggest contour and then check if there is a square...

        for(int i = 0; i < contours.size();i++) {
            contour = contours.get(i);
            double area = Imgproc.contourArea(contour);
            if(area > maxArea) {
                MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
                Imgproc.approxPolyDP(curve, approxCurve, 0.04 * Imgproc.arcLength(curve, true), true);
                if(approxCurve.total() == 4) {
                    maxArea = area;
                    maxAreaIndex = i;
                    maxCurve = approxCurve;
                }
            }
        }
        if(maxCurve.total() == 4) {
            if(drawMode == DrawMode.IMAGE){
                Imgproc.drawContours(img, contours, maxAreaIndex, new Scalar(0,255,0),3);
            }
            double[] corner1 = maxCurve.get(0, 0);
            Point p1 = new Point(corner1[0],corner1[1]);

            double[] corner2 = maxCurve.get(1, 0);
            Point p2 = new Point(corner2[0],corner2[1]);

            double[] corner3 = maxCurve.get(2, 0);
            Point p3 = new Point(corner3[0],corner3[1]);

            double[] corner4 = maxCurve.get(3, 0);
            Point p4 = new Point(corner4[0],corner4[1]);

            List<Point> cornerList = new ArrayList<Point>();
            cornerList.add(p1);
            cornerList.add(p2);
            cornerList.add(p3);
            cornerList.add(p4);

            Mat corners = sortCorners(cornerList);
            if(drawMode == DrawMode.IMAGE) {
                Imgproc.putText(img, "Top L", new Point(corners.get(0, 0)),
                        Core.FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(0, 0, 255), 5);
                Imgproc.putText(img, "Top R", new Point(corners.get(1, 0)),
                        Core.FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(0, 0, 255), 5);
                Imgproc.putText(img, "Bot R", new Point(corners.get(2, 0)),
                        Core.FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(0, 0, 255), 5);
                Imgproc.putText(img, "Bot L", new Point(corners.get(3, 0)),
                        Core.FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(0, 0, 255), 5);
            }
            //new result of img.width x img.height resolution...
            Point topLeft = new Point(0,0);
            Point topRight = new Point(img.width(),0);
            Point bottomRight = new Point(img.width(),img.height());
            Point bottomLeft = new Point(0,img.height());
            List<Point> dimensionList = new ArrayList<Point>();
            dimensionList.add(topLeft);
            dimensionList.add(topRight);
            dimensionList.add(bottomRight);
            dimensionList.add(bottomLeft);
            Mat dimensions = Converters.vector_Point2f_to_Mat(dimensionList);
            Mat perspectiveTransform = Imgproc.getPerspectiveTransform(corners,dimensions);
            Imgproc.warpPerspective(img, warped, perspectiveTransform, new Size(img.width(),img.height()),Imgproc.INTER_CUBIC);     //TODO change to smaller size;
        }
        //Imgproc.resize(img, img, new Size(500,500));
        //Imgproc.resize(imgCon, imgCon, new Size(500,500));
        //Imgproc.resize(hsv, hsv, new Size(500,500));
        //Imgproc.resize(warped, warped, new Size(500,500));

        switch (drawMode){
            case IMAGE:
                return img;      //remove imgCon and use img instead... ?

            case HSV:
                return hsv;


            case WARPED:
                return warped;

            default:
                return null;//return img instead?
        }

    }


    private Mat sortCorners(List<Point> corners) {
        double minSum = Double.MAX_VALUE;
        double maxSum = Double.MIN_VALUE;
        double minDiff = Double.MAX_VALUE;   //double min val || double max val
        double maxDiff = Double.MIN_VALUE;
        int topLeftIndex = 0;
        int topRightIndex = 0;
        int bottomRightIndex = 0;
        int bottomLeftIndex = 0;
        for(int i = 0; i< corners.size();i++) {
            Point corner = corners.get(i);
            double sum = corner.x + corner.y;
            //double diff = corner.y - corner.x;
            double diff = corner.x - corner.y;
            if(sum > maxSum) {
                maxSum = sum;
                bottomRightIndex = i;
            }
            if(sum < minSum) {
                minSum = sum;
                topLeftIndex = i;
            }
            if(diff > maxDiff) {
                maxDiff = diff;
                topRightIndex = i;

            }
            if(diff < minDiff) {
                minDiff = diff;
                bottomLeftIndex = i;
            }
        }
        List<Point> sortedList = new ArrayList<Point>();
        sortedList.add(corners.get(topLeftIndex));
        sortedList.add(corners.get(topRightIndex));
        sortedList.add(corners.get(bottomRightIndex));
        sortedList.add(corners.get(bottomLeftIndex));
        Mat sortedCorners = Converters.vector_Point2f_to_Mat(sortedList);
        return sortedCorners;
    }


}
