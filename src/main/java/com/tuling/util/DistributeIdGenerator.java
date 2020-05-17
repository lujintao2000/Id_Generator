package com.tuling.util;

/**
 * 集群部署下的ID生成实现
 * @author ljt
 * @date 2020-05-03
 *
 */
public class DistributeIdGenerator extends AbstractIdGenerator{
	private IdGenerator idGenerator;
	private String serverId;

	/**
	 *
	 * @param idGenerator
	 * @param serverId   服务器标识
	 */
	public DistributeIdGenerator(IdGenerator idGenerator,String serverId){
		super(idGenerator);
	}


	@Override
	public String getFinalId(String partialId){
		return  partialId == null ? this.serverId : partialId + this.serverId;
	}
}
