package com.tuling.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的ID生成器，生成的ID格式为:0 + 当前距2020年的年数（8位） + 自增序号(40) + 启动序号(7) + 服务器标识(8)
 * 启动序号是为了解决服务器重启后ID生成重复的问题，每次重启，序号都会自增
 * @author lujintao
 * @date 2020-04-30
 */
public class DefaultIdGenerator implements IdGenerator<Long>{

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIdGenerator.class);

	private static final ScheduledExecutorService  executorService = new ScheduledThreadPoolExecutor(1);

	private static final int DAYS_OF_YEAR = 365;

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
	//计数器序列号占用的标识
	private int sequenceLength;
	//表示要与序列号进行与计算的另一数
	private long sequenceAnother;
	// 序列号需要向左移动的位数
	private int sequenceMove;

	static {
		//初始化并更新启动序号，在原有基础上加1,以此保证重启后生成的ID不会和之前的重复
		START_INDEX.initAndUpdate();
	}

	/**
	 *
	 * @param yearLength 年份占用的位数
	 * @param startIndexLength 启动序号占用的位数
	 * @param serverIdLength  服务器标识占用的位数
	 * @param serverId  服务器标识
	 */
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
		//每到新的一年开始时，就重新计算yeatDistance
		executorService.scheduleAtFixedRate(() -> {
			this.yearDistance = getYearDistance();
		},DAYS_OF_YEAR - Calendar.getInstance().get(Calendar.DAY_OF_YEAR),DAYS_OF_YEAR,TimeUnit.DAYS);
	}



	/**
	 * 获得一个指定长度的ID
	 * 自增序号的获取采用了从counter.getAndIncrement()的值中截取特定长度位的方式获取，是鉴于以下理由：
	 * 在一个整数不断自增的过程中，其尾部特定长度的数字是跟着周期性变化的，例如我们观察一个整数从1000增加到3000，
	 * 会发现当数字从1000增加到2000时，其尾部的数字从000变到999；从2000增加到3000时，尾部数字也从000变到999。
	 * 对于long型整数来说，当整数自增时，其尾部特定长度的二进制位的变化也符合这个规律，无论是普通情况下的自增，还是Long.MAX_VALUE + 1,
	 * 或者 -1 + 1。只要我们保证序列号能表达的最大的数一定比业务系统一年需要的ID数多，那生成的ID就不可能出现重复
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
	 * 譬如为了截取a = 01010000 10000000 00010010 00110000 11000000 11000000
	 * 11000000 01010001的最后7位，可以这么做，让它与b = 00000000 00000000 0000
	 * 0000 00000000 00000000 00000000 00000000 01111111进行与计算即可即 a & b
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
