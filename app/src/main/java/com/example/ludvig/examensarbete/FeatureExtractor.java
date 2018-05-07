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
    AEM entityMem;
    EM expMem;
    EB encodingBlock;
    PU procUnit;

    public enum COLOR {RED,GREEN,BLUE}
    public enum SHAPE {SQUARE,CIRCLE,TRIANGLE}
    public enum SHAPEPOSITION {LEFTSIGN_TOPLEFT,LEFTSIGN_TOPRIGHT,LEFTSIGN_BOTTOMRIGHT,LEFTSIGN_BOTTOMLEFT,RIGHTSIGN_TOPLEFT,RIGHTSIGN_TOPRIGHT,RIGHTSIGN_BOTTOMRIGHT,RIGHTSIGN_BOTTOMLEFT}
    private Context context;

    private static final String TAG = "featureExtractor";
    public FeatureExtractor(Context context) throws IOException, ClassNotFoundException {

        entityMem = new AEM(false,context);

        expMem = new EM(false,context);
        encodingBlock = new EB();
        procUnit = new PU(entityMem,expMem);

        //expMem.savePersistent();
        //entityMem.savePersistent();


    }
    public void init(){
        hsv = new Mat();
        warped = new Mat();
        Log.i("feMEM",entityMem.keysToString());
        try {
            expMem.loadPersistent();
            entityMem.loadPersistent();
        } catch (IOException e) {
            Log.e(TAG, "IO: "+ e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Class not found: "+ e.getMessage());
        }

        Log.i("feMEM",entityMem.keysToString());

    }

    public MatSignTuple extractFeatures(Mat img, Scalar lowerbound, Scalar upperbound,int cannyLow, int cannyHigh, double epsilon,  DrawMode drawmode){
        Mat[] signMat = findSign(img,lowerbound,upperbound, drawmode);
        Mat warped = signMat[2];
        Vector<Features> shapes = detectShape(warped,cannyLow,cannyHigh,epsilon);
        detectColors(warped,shapes);
        FeatureSign featureSignLeft = new FeatureSign();
        FeatureSign featureSignRight = new FeatureSign();
        Log.d(TAG,String.valueOf(shapes.size()));
        for(int i = 0; i<shapes.size();i++){
            Features shape = shapes.get(i);
            shape.shapePos = findShapePos(warped,shape.centerPoint);

            Log.d(TAG,shape.color + " " + shape.shape + " in " + shape.shapePos);
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
                /*case YELLOW:
                    s="Y";
                    break;*/
            }
            switch(shape.shapePos){
                case LEFTSIGN_TOPLEFT:
                    featureSignLeft.topLeft = shape;
                    break;
                case LEFTSIGN_TOPRIGHT:
                    featureSignLeft.topRight = shape;
                    break;
                case LEFTSIGN_BOTTOMRIGHT:
                    featureSignLeft.bottomRight = shape;
                    break;
                case LEFTSIGN_BOTTOMLEFT:
                    featureSignLeft.bottomLeft = shape;
                    break;
                case RIGHTSIGN_TOPLEFT:
                    featureSignRight.topLeft = shape;
                    break;
                case RIGHTSIGN_TOPRIGHT:
                    featureSignRight.topRight = shape;
                    break;
                case RIGHTSIGN_BOTTOMRIGHT:
                    featureSignRight.bottomRight = shape;
                    break;
                case RIGHTSIGN_BOTTOMLEFT:
                    featureSignRight.bottomLeft = shape;
                    break;
            }




            String str = "";
            str = shape.shapePos.toString();
            Imgproc.putText(warped, s + String.valueOf(shape.shapeCount),shape.centerPoint,Core.FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(0, 0, 255), 5);
        }
        Sign signLeft = encodingBlock.createSignFromFeatureSign(featureSignLeft);
        Sign signRight = encodingBlock.createSignFromFeatureSign(featureSignRight);
        Log.i("FeatureExtractorSign",signLeft.toString());
        Log.i("FeatureExtractorSign",signRight.toString());
        Mat returnMat = img;
        switch (drawmode) {
            case IMAGE:
                returnMat = signMat[0];
                break;
                //return signMat[0];

            case HSV:
                returnMat = hsv;
                break;

            case WARPED:
                Imgproc.resize(warped,warped,img.size());
                returnMat = warped;
                break;

        }

        return new MatSignTuple(returnMat,signLeft,signRight);

    }

    public DIR checkForBest(Sign left,Sign right){
        try {
            return procUnit.checkForBestMatch(left, right);
        } catch (IOException e) {
            Log.e(TAG,"could not find best match",e);
        }
        return null;
    }

    public void train(Sign sign,DIR direction){

        try {
            procUnit.SaveEncodingAndDirectionToExp(sign,direction);
        } catch (IOException e) {
            Log.e(TAG,"unable to save exp",e);
        }
    }

    public void saveExp(){
        try {
            expMem.savePersistent();
        } catch (IOException e) {
            Log.e(TAG,"Unable to save exp",e);
        }
    }


    private SHAPEPOSITION findShapePos(Mat img,Point centerPoint){
        int width = img.cols();
        int height = img.rows();
        /*
        if(centerPoint.x < width / 2 && centerPoint.y < height/2){  //top left quadrant
            return SHAPEPOSITION.TOPLEFT;
        }
        else if(centerPoint.x > width / 2 && centerPoint.y < height/2){  //top right quadrant
            return SHAPEPOSITION.TOPRIGHT;
        }
        else if(centerPoint.x < width / 2 && centerPoint.y > height/2){     //bottom left quadrant
            return SHAPEPOSITION.BOTTOMLEFT;
        }
        else{                                                               //bottom right quadrant
            return SHAPEPOSITION.BOTTOMRIGHT;
        }
        */

        // LEFT SIGN
        if(centerPoint.x <= width/2){
            if( centerPoint.x <= width / 4 && centerPoint.y <= height/ 2 ){
                return SHAPEPOSITION.LEFTSIGN_TOPLEFT;
            }
            else if(centerPoint.x > width / 4 && centerPoint.y <= height / 2){
                return SHAPEPOSITION.LEFTSIGN_TOPRIGHT;
            }
            else if(centerPoint.x > width / 4 && centerPoint.y > height /2 ){
                return SHAPEPOSITION.LEFTSIGN_BOTTOMRIGHT;
            }
            else if(centerPoint.x <= width/4 && centerPoint.y > height/2){
                return SHAPEPOSITION.LEFTSIGN_BOTTOMLEFT;
            }else{
                Log.e(TAG,"position not found: left sign at " + centerPoint.x+":"+centerPoint.y);
                return null;

            }
        }else{
            // RIGHT SIGN
            if(centerPoint.x <= 3*(width / 4) && centerPoint.y <= height/2){
                return SHAPEPOSITION.RIGHTSIGN_TOPLEFT;
            }
            else if(centerPoint.x > 3*(width / 4) && centerPoint.y <= height/2){
                return SHAPEPOSITION.RIGHTSIGN_TOPRIGHT;
            }
            else if(centerPoint.x >= 3*(width / 4) && centerPoint.y > height/2){
                return SHAPEPOSITION.RIGHTSIGN_BOTTOMRIGHT;
            }else if(centerPoint.x < 3*(width / 4) && centerPoint.y > height/2){
                return SHAPEPOSITION.RIGHTSIGN_BOTTOMLEFT;
            }else{
                Log.e(TAG,"position not found: right sign at " + centerPoint.x+":"+centerPoint.y);
                return null;
            }
        }






    }

    private void detectColors(Mat img, Vector<Features> features){
        if(img.channels() < 3){     //img is empty
            return ;
        }
        Scalar lower_blue = new Scalar(95,50,30);         //Scalar lower_blue = new Scalar(80,115,180);
        Scalar upper_blue = new Scalar(130,255,255);

        Scalar lower_red = new Scalar(0,50,30);
        Scalar upper_red = new Scalar(10,255,255);
        Scalar lower_red2 = new Scalar(170,50,30);
        Scalar upper_red2 = new Scalar(180,255,255);


        Scalar lower_yellow = new Scalar(20,50,30);         //TODO improve yellow range
        Scalar upper_yellow = new Scalar(30,255,255);

        Scalar lower_green = new Scalar(40,50,30);
        Scalar upper_green = new Scalar(70,255,255);

        Mat hsv = new Mat();
        Imgproc.cvtColor(img,hsv,Imgproc.COLOR_RGB2HSV);
        Mat blueMask = new Mat();
        Mat redMask = new Mat();
        Mat redMask2 = new Mat();
        Mat greenMask = new Mat();
        //Mat yellowMask = new Mat();

        //handle the hue wrap of red.
        Core.inRange(hsv,lower_red,upper_red,redMask);
        Core.inRange(hsv,lower_red2,upper_red2,redMask2);
        Core.bitwise_or(redMask,redMask2,redMask);

        Core.inRange(hsv,lower_blue,upper_blue,blueMask);


        //Core.inRange(hsv,lower_yellow,upper_yellow,yellowMask);
        Core.inRange(hsv,lower_green,upper_green,greenMask);



        for(int i = features.size()-1; i>=0; i--){
            Features f = features.get(i);
            Point centerPoint = f.centerPoint;
            int x = (int)centerPoint.x;
            int y = (int)centerPoint.y;

            Log.i("colorDetP",centerPoint.toString());
            if(blueMask.get(y,x)!= null)
                Log.d("colorDet", i + "b" + String.valueOf(blueMask.get(y,x)[0]));
            if(redMask.get(y,x)!= null)
                Log.d("colorDet",i + "r" + String.valueOf(redMask.get(y,x)[0]));

            if(blueMask.get(y,x)!= null && blueMask.get(y,x)[0] > 0){
                f.color = COLOR.BLUE;
                Log.d("colorDet", "Blue detected!");
            }else if(redMask.get(y,x)!= null && redMask.get(y,x)[0] > 0){
                f.color = COLOR.RED;
                Log.d("colorDet", "Red detected!");
            }/*else if (yellowMask.get(y,x)!= null && yellowMask.get(y,x)[0] > 0){
                f.color = COLOR.YELLOW;
            }*/
            else if(greenMask.get(y,x)!= null && greenMask.get(y,x)[0] > 0){
                f.color = COLOR.GREEN;
            }else{
                features.remove(i);
            }

        }

    }

    public Vector<Features> detectShape(Mat img, int cannyLow, int cannyHigh, double epsilon) {
        Vector<Features> resultVector = new Vector<Features>();

        Mat edges = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.Canny(img, edges, cannyLow, cannyHigh);
        //Imgproc.blur(edges, edges, new Size(2, 2));
        Imgproc.findContours(edges, contours, hierarchy,
                Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

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

            }else if(approxCurve.total() >= 7) {
                Features f = new Features();
                f.shape = SHAPE.CIRCLE;
                f.shapeCount = (int)approxCurve.total();
                f.centerPoint = new Point(centerX,centerY);

                resultVector.add(f);
            }
        }
        return resultVector;
    }

    public Mat[] findSign(Mat img , Scalar lowerBound, Scalar upperBound, DrawMode drawMode) {  //TODO fix error with drawing on same mat...
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
            /*if(drawMode == DrawMode.IMAGE) {
                Imgproc.putText(img, "Top L", new Point(corners.get(0, 0)),
                        Core.FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(0, 0, 255), 5);
                Imgproc.putText(img, "Top R", new Point(corners.get(1, 0)),
                        Core.FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(0, 0, 255), 5);
                Imgproc.putText(img, "Bot R", new Point(corners.get(2, 0)),
                        Core.FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(0, 0, 255), 5);
                Imgproc.putText(img, "Bot L", new Point(corners.get(3, 0)),
                        Core.FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(0, 0, 255), 5);
            }*/
            //new result of size width and height

            int width = calcWidth(corners);
            int height = calcHeight(corners);

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
