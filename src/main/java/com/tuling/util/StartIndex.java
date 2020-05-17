package com.tuling.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * index 为0(包含)到100(不包含)之间的数
 * @author lujintao
 * @date 2020-05-01
 *
 */
public class StartIndex {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIdGenerator.class);
	private static final String PATH = DefaultIdGenerator.class.getResource("/").getPath()
			+ File.separator + "startIndex";

	private long index;

	public StartIndex(long index){
		this.index = index;
	}
	
	public long getIndex() {
		return index;
	}

	public void setIndex(long index) {
		this.index = index;
	}

	/**
	 * 初始化START_INDEX的值,并将新值写入文件START_INDEX中
	 */
	public  void init() {
		// 从文件中读取START_INDEX的值
		File file = new File(PATH);

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
	}

	/**
	 * 将原有的startIndex值加1写入文件START_INDEX
	 */
	public void update() {
		BufferedWriter in = null;
		try {
			in = new BufferedWriter(new FileWriter(new File(PATH)));
			in.write((++index)+ "");
			in.flush();
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
	 * 从startIndex文件中初始化index的值，并且更新文件中index的值，在原有值基础上加1
	 */
	public void initAndUpdate(){
		init();
		update();
	}

}
