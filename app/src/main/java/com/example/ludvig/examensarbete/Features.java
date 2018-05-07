package com.example.ludvig.examensarbete;

import org.opencv.core.Point;

public class Features {
    Point centerPoint;
    FeatureExtractor.COLOR color;
    FeatureExtractor.SHAPE shape;
    FeatureExtractor.SHAPEPOSITION shapePos;
    HDVECTOR hdvShape;
    int shapeCount;     //the number of points that were approximated to the shape


    public HDVECTOR getShapeAndColorVector(){
        if(this.color == null){
            return HDVECTOR.NULL;
        }
        if(this.color == FeatureExtractor.COLOR.BLUE){
            if(this.shape == FeatureExtractor.SHAPE.SQUARE){
                return HDVECTOR.Sb;
            }else if(this.shape == FeatureExtractor.SHAPE.TRIANGLE){
                return HDVECTOR.Tb;
            }else if(this.shape == FeatureExtractor.SHAPE.CIRCLE){
                return HDVECTOR.Cb;
            }
        }else if(this.color == FeatureExtractor.COLOR.RED){
            if(this.shape == FeatureExtractor.SHAPE.SQUARE){
                return HDVECTOR.Sr;
            }else if(this.shape == FeatureExtractor.SHAPE.TRIANGLE){
                return HDVECTOR.Tr;
            }else if(this.shape == FeatureExtractor.SHAPE.CIRCLE){
                return HDVECTOR.Cr;
            }
        }else if(this.color == FeatureExtractor.COLOR.GREEN) {
            if (this.shape == FeatureExtractor.SHAPE.SQUARE) {
                return HDVECTOR.Sg;
            } else if (this.shape == FeatureExtractor.SHAPE.TRIANGLE) {
                return HDVECTOR.Tg;
            } else if (this.shape == FeatureExtractor.SHAPE.CIRCLE) {
                return HDVECTOR.Cg;
            }
        }
            //TODO add yellow...
        return null;
    }
}

