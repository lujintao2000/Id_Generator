package com.tuling.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的ID生成器，生成的ID格式为:距2020年的年数（8位） + 自增序号(40) + 启动序号(7) + 服务器标识(8)
 *
 * @author lujintao
 * @date 2020-04-30
 */
public class DefaultIdGenerator implements IdGenerator<Long>{

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIdGenerator.class);

	private static final ScheduledExecutorService  executorService = new ScheduledThreadPoolExecutor(1);

	private static final int DAYS_OF_YEAR = 365;

	private static final int MIN_LENGTH = 15;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyyMMdd");
	private static final StartIndex START_INDEX = new StartIndex(0);


	private static final int BASE_YEAR = 2020;
	private AtomicLong counter = new AtomicLong();

	//年份（以2020年为起点，距离2020年的年份）占用的位数
	private int yearLength;
	//记录当前年份与基准年份（2020）年相隔的年数
	private int yearDistance;
	//表示要与年份进行与计算的另一数
	private long yearAnother;
	// 年份需要向左移动的位数
	private int yearMove;
	//启动序号占用的位数
	private int startIndexLength;
	//表示要与启动序号进行与计算的另一数
	private long startIndexAnother;
	// 启动序号需要向左移动的位数
	private int startIndexMove;
	//服务器ID
	private long serverId;
	//服务器标识占用的位数
	private int serverIdLength;
//	// 服务器标识需要向左移动的位数
//	private int serverIdMove;
	//计数器序列号占用的标识
	private int sequenceLength;
	//表示要与序列号进行与计算的另一数
	private long sequenceAnother;
	// 序列号需要向左移动的位数
	private int sequenceMove;

	static {
		START_INDEX.initAndUpdate();
	}

	public DefaultIdGenerator(int yearLength, int startIndexLength, int serverIdLength,long serverId){
		this.yearLength = yearLength;
		this.yearDistance = getYearDistance();
		this.startIndexLength = startIndexLength;
		this.serverIdLength = serverIdLength;
		this.sequenceLength = 63 - yearLength - startIndexLength - serverIdLength;
		this.yearAnother = getAnotherForAnd(yearLength);
		this.startIndexAnother = getAnotherForAnd(startIndexLength);
		this.sequenceAnother = getAnotherForAnd(sequenceLength);
		this.yearMove = 63 - yearLength;
		this.sequenceMove = startIndexLength + serverIdLength;
		this.startIndexMove = serverIdLength;
		this.serverId = serverId;
		executorService.scheduleAtFixedRate(() -> {
			this.yearDistance = getYearDistance();
		},0,DAYS_OF_YEAR,TimeUnit.DAYS);
	}



	/**
	 * 获得一个指定长度的ID
	 * @return
	 */
	public Long getId() {
		long result = 0L;
		result = (((long)yearDistance & yearAnother) << yearMove) |
				((counter.getAndIncrement() & sequenceAnother) << sequenceMove) |
				((START_INDEX.getIndex() & startIndexAnother) << startIndexMove) |
				serverId;
		return result;
	}


	private int getYearDistance(){
		return Calendar.getInstance().get(Calendar.YEAR) - BASE_YEAR;
	}

	/**
	 * 当一个整数需要截取低位length个位时，为了完成这一操作，需要与之进行与操作的另一个数
	 * @param length
	 * @return
	 */
	private long getAnotherForAnd(int length){
		long result = 1;
		for(int i = 0; i < length; i++){
			result *= 2;
		}

		return result - 1;
	}
}
