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
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的ID生成器，生成的ID格式为:年月日 + (服务器启动次数 - 1) % 100 + long型计数器从尾部截取指定长度,如20201120 + 01 + 102282
 *
 * @author lujintao
 * @date 2020-04-30
 */
public class DefaultIdGenerator implements IdGenerator{

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIdGenerator.class);

	private static final int MIN_LENGTH = 15;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyyMMdd");
	private static final StartIndex START_INDEX = new StartIndex(0);
	private static final String PATH = DefaultIdGenerator.class.getResource("/").getPath()
			+ File.separator + "startIndex";
	private AtomicLong counter = new AtomicLong();
	//生成ID时，从long型自增器尾部截取的数字长度
	private int length;

	static {
		initStartIndex();
		updateStartIndex();
	}


	public DefaultIdGenerator(int length){
		this.length = length;
	}

	/**
	 * 初始化START_INDEX的值,并将新值写入文件START_INDEX中
	 */
	private static void initStartIndex() {
		// 从文件中读取START_INDEX的值
		File file = new File(PATH);
		// 记录从文件START_INDEX 中读取到的值
		int index = 0;
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				LOGGER.error("创建START_INDEX文件失败",e);
			}
		} else {
			try {
				BufferedReader in = new BufferedReader(new FileReader(file));
				try {
					String content = in.readLine();
					if (content != null && content.trim().length() > 0) {
						index = Integer.parseInt(content);
					}
				} catch (IOException e) {
					LOGGER.error("从START_INDEX文件读取内容异常",e);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							LOGGER.error("关闭START_INDEX文件异常",e);
						}
					}
				}

			} catch (FileNotFoundException e) {
				LOGGER.error("START_INDEX文件未找到",e);
			}
		}
		START_INDEX.setIndex(index);

	}

	/**
	 * 将原有的startIndex值加1写入文件START_INDEX
	 */
	private static void updateStartIndex() {
		BufferedWriter in = null;
		try {
			in = new BufferedWriter(new FileWriter(new File(PATH)));
			in.write((START_INDEX.getIndex() + 1)+ "");
		}catch (FileNotFoundException e) {
			LOGGER.error("startIndex文件未找到");
		} catch (IOException e) {
			LOGGER.error("更新startIndex异常",e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOGGER.error("更新startIndex文件时关闭异常",e);
				}
			}

		}
		//锁定startIndex文件，以防被误删
		FileChannel fileChannel = null;
		try {
			fileChannel = new FileOutputStream(PATH,true).getChannel();
			// 对文件加锁
			try {
				FileLock lock = fileChannel.lock();
			} catch (IOException e) {
				LOGGER.error("startIndex文件锁定异常",e);
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("锁定startIndex时，startIndex文件未找到",e);
		}
	}

	/**
	 * 获得一个指定长度的ID
	 * @return
	 */
	public String getId() {
		String result = "";
		result = DATE_FORMAT.format(new Date()) + START_INDEX
				+ this.getSubStr(counter.getAndIncrement(), length);
		return result;
	}

	/**
	 * 在指定整数中，从末尾截取指定长度的字符串;当number的长度不足length时，需要在前面补0,直到字符串长度达到length
	 *
	 * @param number
	 * @param length
	 * @return 长度为length的子串
	 */
	private static String getSubStr(Long number, int length) {
		String result = "";
		int numLen = number.toString().length();
		if (numLen < length) {
			result = number.toString();
			for (int i = 0; i < length - numLen; i++) {
				result = "0" + result;
			}
		} else {
			result = number.toString().substring(numLen - length);
		}

		return result;
	}
}
