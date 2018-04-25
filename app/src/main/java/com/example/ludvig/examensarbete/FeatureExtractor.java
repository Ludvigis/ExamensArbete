package com.example.ludvig.examensarbete;

import android.content.Context;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class FeatureExtractor {
    private Mat hsv;
    private Mat warped;
    Memory mem;

    public enum COLOR {RED,GREEN,BLUE,YELLOW}
    public enum SHAPE {SQUARE,CIRCLE,TRIANGLE}
    public enum QUADRANT {TOPLEFT,TOPRIGHT,BOTTOMRIGHT,BOTTOMLEFT}


    private static final String TAG = "featureExtractor";
    public FeatureExtractor() throws IOException, ClassNotFoundException {
        hsv = new Mat();
        warped = new Mat();
        mem = Memory.getInstance();

    }

    public Mat extractFeatures(Mat img, Scalar lowerbound, Scalar upperbound,int cannyLow, int cannyHigh, double epsilon,  DrawMode drawmode){
        Mat[] signMat = findSign(img,lowerbound,upperbound, drawmode);
        Mat warped = signMat[2];
        Vector<Features> shapes = detectShape(warped,cannyLow,cannyHigh,epsilon);
        detectColors(warped,shapes);
        FeatureSign featureSign = new FeatureSign();
        for(int i = 0; i<shapes.size();i++){
            Features shape = shapes.get(i);
            shape.quadrant = findQuadrants(warped,shape.centerPoint);
            Log.i(TAG,String.valueOf(shapes.size()));
            Log.i(TAG,shape.color + " " + shape.shape + " in " + shape.quadrant);
            String s = "";
            switch (shape.color){
                case RED:
                    s = "R";
                    break;
                case GREEN:
                    s="G";
                    break;
                case BLUE:
                    s="B";
                    break;
                case YELLOW:
                    s="Y";
                    break;
            }
            switch(shape.quadrant){
                case TOPLEFT:
                    featureSign.topLeft = shape;
                    break;
                case TOPRIGHT:
                    featureSign.topRight = shape;
                    break;
                case BOTTOMRIGHT:
                    featureSign.bottomRight = shape;
                    break;
                case BOTTOMLEFT:
                    featureSign.bottomLeft = shape;
                    break;

            }

            Imgproc.putText(warped, s + String.valueOf(shape.shapeCount),shape.centerPoint,Core.FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(0, 0, 255), 5);
        }

        Sign signLeft = new Sign(mem);
        Sign signRight = new Sign(mem);
        getSameDifferent(signLeft,signRight,featureSign);
        getAboveBelow(signLeft,signRight,featureSign);
        getShapeVector(signLeft,signRight,featureSign);

        Node node = new Node(signLeft,signRight,"Root");
        Node node2 = new Node(signLeft,signRight,"left");
        Node node3 = new Node(signLeft,signRight,"right");
        node.setLeftPath(node2);
        node.setRightPath(node3);

        try {
            Node best = node.checkForBestRewardMatch();
            if(best!=null){
                Log.d("ResultFeature",best.getName());

                Imgproc.putText(signMat[0],best.getName(),new Point(signMat[0].width()/2,signMat[0].height() - 100),Core.FONT_HERSHEY_SIMPLEX,2.0,new Scalar(0,255,0),5);
            }


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("signTest",signLeft.toString() + "  " + signRight.toString());


        switch (drawmode) {
            case IMAGE:
                return signMat[0];

            case HSV:
                return hsv;

            case WARPED:
                Imgproc.resize(warped,warped,img.size());
                return warped;


            default:
                return img;
        }



    }

    private void getShapeVector(Sign signLeft,Sign signRight, FeatureSign featureSign){

        if(featureSign.topLeft != null  && featureSign.bottomLeft != null){
            signLeft.setFirstObj(getShapeAndColorVector(featureSign.topLeft));
            signLeft.setSecObj(getShapeAndColorVector(featureSign.bottomLeft));
        }

        if(featureSign.topRight != null  && featureSign.bottomRight != null) {
            signRight.setFirstObj(getShapeAndColorVector(featureSign.topRight));
            signRight.setSecObj(getShapeAndColorVector(featureSign.bottomRight));
        }
    }
    private HDVECTOR getShapeAndColorVector(Features feature){
        if(feature.color == COLOR.BLUE){
            if(feature.shape == SHAPE.SQUARE){
                return HDVECTOR.Sb;
            }else if(feature.shape == SHAPE.TRIANGLE){
                return HDVECTOR.Tb;
            }else if(feature.shape == SHAPE.CIRCLE){
                return HDVECTOR.Cb;
            }
        }else if(feature.color == COLOR.RED){
            if(feature.shape == SHAPE.SQUARE){
                return HDVECTOR.Sr;
            }else if(feature.shape == SHAPE.TRIANGLE){
                return HDVECTOR.Tr;
            }else if(feature.shape == SHAPE.CIRCLE){
                return HDVECTOR.Cr;
            }
        }//TODO add green and yellow...
        return null;
    }

    private void getAboveBelow(Sign signLeft,Sign signRight, FeatureSign featureSign){
        if(featureSign.topLeft != null  && featureSign.bottomLeft != null){
            signLeft.setRelation(HDVECTOR.aboveBelow);
        }
        if(featureSign.topRight != null  && featureSign.bottomRight != null) {
            signRight.setRelation(HDVECTOR.aboveBelow);
        }
    }

    private void getSameDifferent(Sign signLeft,Sign signRight, FeatureSign featureSign){
        if(featureSign.topLeft != null  && featureSign.bottomLeft != null){
            if(featureSign.topLeft.shape == featureSign.bottomLeft.shape){
                signLeft.setSameDifferent(HDVECTOR.same);
            }else{
                signLeft.setSameDifferent(HDVECTOR.different);
            }
        }
        if(featureSign.topRight != null  && featureSign.bottomRight != null) {
            if (featureSign.topRight.shape == featureSign.bottomRight.shape) {
                signRight.setSameDifferent(HDVECTOR.same);
            } else {
                signRight.setSameDifferent(HDVECTOR.different);
            }
        }
    }


    private QUADRANT findQuadrants(Mat img,Point centerPoint){
        int width = img.cols();
        int height = img.rows();

        if(centerPoint.x < width / 2 && centerPoint.y < height/2){  //top left quadrant
            return QUADRANT.TOPLEFT;
        }
        else if(centerPoint.x > width / 2 && centerPoint.y < height/2){  //top right quadrant
            return QUADRANT.TOPRIGHT;
        }
        else if(centerPoint.x < width / 2 && centerPoint.y > height/2){     //bottom left quadrant
            return QUADRANT.BOTTOMLEFT;
        }
        else{                                                               //bottom right quadrant
            return QUADRANT.BOTTOMRIGHT;
        }
    }

    private void detectColors(Mat img, Vector<Features> features){
        if(img.channels() < 3){     //img is empty
            return ;
        }
        Scalar lower_blue = new Scalar(100,50,30);         //Scalar lower_blue = new Scalar(80,115,180);
        Scalar upper_blue = new Scalar(255,255,255);

        Scalar lower_red = new Scalar(0,50,30);
        Scalar upper_red = new Scalar(20,255,255);

        Scalar lower_yellow = new Scalar(20,50,30);
        Scalar upper_yellow = new Scalar(40,255,255);

        Scalar lower_green = new Scalar(40,50,30);
        Scalar upper_green = new Scalar(90,255,255);

        Mat hsv = new Mat();
        Imgproc.cvtColor(img,hsv,Imgproc.COLOR_RGB2HSV);
        Mat blueMask = new Mat();
        Mat redMask = new Mat();
        Mat greenMask = new Mat();
        Mat yellowMask = new Mat();
        Core.inRange(hsv,lower_blue,upper_blue,blueMask);
        Core.inRange(hsv,lower_red,upper_red,redMask);
        Core.inRange(hsv,lower_yellow,upper_yellow,yellowMask);
        Core.inRange(hsv,lower_green,upper_green,greenMask);



        for(int i = features.size()-1; i>=0; i--){
            Features f = features.get(i);
            Point centerPoint = f.centerPoint;
            int x = (int)centerPoint.y; //TODO swap back x and y and change places in .get(x,y) to (y,x)
            int y = (int)centerPoint.x;

            Log.i("colorDetP",centerPoint.toString());
            if(blueMask.get(x,y)!= null)
                Log.d("colorDet", i + "b" + String.valueOf(blueMask.get(x,y)[0]));
            if(redMask.get(x,y)!= null)
                Log.d("colorDet",i + "r" + String.valueOf(redMask.get(x,y)[0]));

            if(blueMask.get(x,y)!= null && blueMask.get(x,y)[0] > 0){
                f.color = COLOR.BLUE;
                Log.d("colorDet", "Blue detected!");
            }else if(redMask.get(x,y)!= null && redMask.get(x,y)[0] > 0){
                f.color = COLOR.RED;
                Log.d("colorDet", "Red detected!");
            }else if (yellowMask.get(x,y)!= null && yellowMask.get(x,y)[0] > 0){
                f.color = COLOR.YELLOW;
            }else if(greenMask.get(x,y)!= null && greenMask.get(x,y)[0] > 0){
                f.color = COLOR.YELLOW;
            }else{
                features.remove(i);     //TODO loop backwards and remove...
            }

        }

    }


    public Vector<Features> detectShape(Mat img, int cannyLow, int cannyHigh, double epsilon) {
        Vector<Features> resultVector = new Vector<Features>();

        Mat edges = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.Canny(img, edges, cannyLow, cannyHigh);     //100, 300 cannyLow and High
        //Imgproc.blur(edges, edges, new Size(2, 2));
        Imgproc.findContours(edges, contours, hierarchy,
                Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);        //TODO Retr external?
        //third argument -1 = draw all contours...

        //Imgproc.drawContours(img, contours, -1, new Scalar(255,0,0),2);

        MatOfPoint2f approxCurve = new MatOfPoint2f();
        MatOfPoint2f curve = new MatOfPoint2f();
        for (int i = 0; i < contours.size(); i++) {
            Rect rect = Imgproc.boundingRect(contours.get(i));
            int centerX = rect.x + rect.width/2;
            int centerY = rect.y + rect.height/2;

            MatOfPoint c = contours.get(i);
            curve.fromList(c.toList());

            Imgproc.approxPolyDP(curve, approxCurve, epsilon*
                    Imgproc.arcLength(curve, true), true);

            /*if(approxCurve.total()>=3){
                Imgproc.circle(img, new Point(centerX,centerY),1,new Scalar(0,255,0),5);
            }*/
            if(approxCurve.total() == 3) {
                Features f = new Features();
                f.shape = SHAPE.TRIANGLE;
                f.shapeCount = 3;
                f.centerPoint = new Point(centerX,centerY);
                resultVector.add(f);

            }else if(approxCurve.total() == 4) {
                Features f = new Features();
                f.shape = SHAPE.SQUARE;
                f.centerPoint = new Point(centerX,centerY);
                f.shapeCount = 4;

                resultVector.add(f);

            }else if(approxCurve.total() > 7) {
                Features f = new Features();
                f.shape = SHAPE.CIRCLE;
                f.shapeCount = (int)approxCurve.total();
                f.centerPoint = new Point(centerX,centerY);

                resultVector.add(f);
            }
        }
        return resultVector;
    }

    public Mat[] findSign(Mat img , Scalar lowerBound, Scalar upperBound, DrawMode drawMode) {
        //Mat warped = new Mat();

        Imgproc.cvtColor(img, hsv, Imgproc.COLOR_RGB2HSV);  //BGR OR RGB??
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
            //new result of size width and height

            int width = calcWidth(corners);
            int height = calcHeight(corners);
            //int width = img.width();
            //int height = img.height();


            Point topLeft = new Point(0,0);
            Point topRight = new Point(width,0);
            Point bottomRight = new Point(width,height);
            Point bottomLeft = new Point(0,height);
            List<Point> dimensionList = new ArrayList<Point>();
            dimensionList.add(topLeft);
            dimensionList.add(topRight);
            dimensionList.add(bottomRight);
            dimensionList.add(bottomLeft);
            Mat dimensions = Converters.vector_Point2f_to_Mat(dimensionList);
            Mat perspectiveTransform = Imgproc.getPerspectiveTransform(corners,dimensions);


            Imgproc.warpPerspective(img, warped, perspectiveTransform, new Size(width,height),Imgproc.INTER_CUBIC);

        }

        return new Mat[]{img,hsv,warped};
    }

    private int calcWidth(Mat corners){
        double[] tl = corners.get(0,0);
        double[] tr = corners.get(1,0);
        double[] br = corners.get(2,0);
        double[] bl = corners.get(3,0);

        int w1 = (int)Math.sqrt(Math.pow(br[0]-bl[0],2) + Math.pow(br[1]-bl[1],2));
        int w2 = (int)Math.sqrt(Math.pow(tr[0]-tl[0],2) + Math.pow(tr[1]-tl[1],2));

        if(w1 < w2){
            return (w1>0)?w1 :1;
        }else{
            return (w2>0)?w2 :1;
        }
    }

    private int calcHeight(Mat corners){
        double[] tl = corners.get(0,0);
        double[] tr = corners.get(1,0);
        double[] br = corners.get(2,0);
        double[] bl = corners.get(3,0);

        int h1 = (int)Math.sqrt(Math.pow(tr[0]-br[0],2) + Math.pow(tr[1]-br[1],2));
        int h2 = (int)Math.sqrt(Math.pow(tl[0]-bl[0],2) + Math.pow(tl[1]-bl[1],2));

        if(h1 < h2){
            return (h1>0)?h1:1;
        }else{
            return (h2>0)?h2:1;
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
