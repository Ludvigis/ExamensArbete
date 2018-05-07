package com.example.ludvig.examensarbete;



import java.util.Random;


public class Sign {
	private HDVECTOR top_right;
	private HDVECTOR top_left;
	private HDVECTOR bottom_left;
	private HDVECTOR bottom_right;
	
	public Sign(){
	}
	
	private HDVECTOR getRandomShape(){
		Random rnd = new Random();
		int temp = rnd.nextInt(9)+1;
		switch(temp){
		case 1: 
			return HDVECTOR.Cb;
		case 2: 
			return HDVECTOR.Cr;
		case 3:
			return HDVECTOR.Cg;
		case 4:
			return HDVECTOR.Sb;
		case 5:
			return HDVECTOR.Sr;
		case 6:
			return HDVECTOR.Sg;
		case 7:
			return HDVECTOR.Tb;
		case 8:
			return HDVECTOR.Tr;
		case 9:
			return HDVECTOR.Tg;
			
		}
		return HDVECTOR.Tg;
		
	}
	
	public HDVECTOR getTopRightShape() {
		return top_right;
	}

	public HDVECTOR getTopLeftShape() {
		return top_left;
	}

	public HDVECTOR getBottomLeftShape() {
		return bottom_left;
	}

	public HDVECTOR getBottomRightShape() {
		return bottom_right;
	}

	public void setTopRightShape(HDVECTOR top_right) {
		this.top_right = top_right;
	}

	public void setTopLeftShape(HDVECTOR top_left) {
		this.top_left = top_left;
	}

	public void setBottomLeftShape(HDVECTOR bottom_left) {
		this.bottom_left = bottom_left;
	}

	public void setBottomRightShape(HDVECTOR bottom_right) {
		this.bottom_right = bottom_right;
	}

	@Override
	public String toString() {
		String s = "\ntop left " + top_left.toString() + "\ntopRight " + top_right.toString() + "\nbottomRight " + bottom_right.toString() + "\nbottomleft " + bottom_left.toString();
		return s;
	}
}
