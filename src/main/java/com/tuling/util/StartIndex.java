package com.tuling.util;

/**
 * index 为0(包含)到100(不包含)之间的数
 * @author lujintao
 * @date 2020-05-01
 *
 */
public class StartIndex {
	private int index;

	public StartIndex(int index){
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index % 100;
	}
	
	@Override
	public String toString(){
		if(index < 10){
			return "0" + index;
		}else{
			return index + "";
		}
	}
}
