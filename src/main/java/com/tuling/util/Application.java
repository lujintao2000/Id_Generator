package com.tuling.util;

import java.util.Date;

public class Application {

	
	public static void main(String[] args){
		IdGenerator idGenerator = new DistributeIdGenerator(new DefaultIdGenerator(9));
		
		long startTime = new Date().getTime();
		for (int i = 0; i < 1000; i++) {
			System.out.println(idGenerator.getId());
		}
		long endTime = new Date().getTime();
		System.out.println("total time : " + (endTime - startTime) + "ms");
	}
}
