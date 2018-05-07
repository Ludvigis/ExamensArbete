package com.example.ludvig.examensarbete;

import org.opencv.core.Mat;

public class MatSignTuple {
    Mat img;
    Sign leftSign;
    Sign rightSign;

    public MatSignTuple(Mat img,Sign leftSign,Sign rightSign){
        this.img = img;
        this.leftSign  = leftSign;
        this.rightSign = rightSign;
    }
}
