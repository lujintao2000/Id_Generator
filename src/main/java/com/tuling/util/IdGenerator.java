package com.tuling.util;

/**
 * 该接口定义了获得ID的方法规范
 * @author ljt
 * @date 2020-05-03
 *
 */
public interface IdGenerator {

	/**
	 * 获得一个唯一的ID
	 * 返回值不能为null
	 * @return
	 */
	public String getId();
	
}
