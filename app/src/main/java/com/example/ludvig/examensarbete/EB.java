package com.example.ludvig.examensarbete;

public class EB {

	AEM aem;
	
	public EB(){
		
	}
	
	public Sign createBasicSign(HDVECTOR shape_top_left, HDVECTOR shape_top_right, HDVECTOR shape_bottom_left, HDVECTOR shape_bottom_right){
		Sign s = new Sign();
		s.setTopLeftShape(shape_top_left);
		s.setTopRightShape(shape_top_right);
		s.setBottomLeftShape(shape_bottom_left);
		s.setBottomRightShape(shape_bottom_right);
		return s;
	}


	public Sign createSignFromFeatureSign(FeatureSign featureSign){
		Sign sign = new Sign();
		if(featureSign.topLeft == null){
			sign.setTopLeftShape(HDVECTOR.NULL);
		}else{
			sign.setTopLeftShape(featureSign.topLeft.getShapeAndColorVector());
		}
		if(featureSign.topRight == null){
			sign.setTopRightShape(HDVECTOR.NULL);
		}else{
			sign.setTopRightShape(featureSign.topRight.getShapeAndColorVector());
		}
		if(featureSign.bottomRight == null){
			sign.setBottomRightShape(HDVECTOR.NULL);
		}else{
			sign.setBottomRightShape(featureSign.bottomRight.getShapeAndColorVector());
		}
		if(featureSign.bottomLeft == null){
			sign.setBottomLeftShape(HDVECTOR.NULL);
		}else{
			sign.setBottomLeftShape(featureSign.bottomLeft.getShapeAndColorVector());
		}




		return sign;
	}
	
}
