package com.tuling.util;

import java.util.Calendar;
import java.util.Date;

public class Application {

	public static void main(String[] args){
		System.out.println(1900834 - 1835298);

		IdGenerator<Long> idGenerator =  new DefaultIdGenerator(8, 8, 8, 34);

		long startTime = new Date().getTime();
		long result = 0L;
		for (int i = 0; i < 20000000; i++) {
			idGenerator.getId();
		}
		long endTime = new Date().getTime();
		System.out.println("total time : " + (endTime - startTime) + "ms");
		System.out.println("result: " + result);
	}
}
