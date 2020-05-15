package com.tuling.util;

/**
 * 集群部署下的ID生成实现
 * @author ljt
 * @date 2020-05-03
 *
 */
public class DistributeIdGenerator extends AbstractIdGenerator{
	private static final String DEFAULT_DISTRIBUTE_ID = "01";
	private IdGenerator idGenerator;
	
	public DistributeIdGenerator(IdGenerator idGenerator){
		super(idGenerator);
	}
	

	/**
	 * 获得分布式部署ID
	 * @return
	 */
	private String getDistributeId(){
		String distributeId = System.getProperty("distributeId");
		if(distributeId != null && distributeId.trim().length() > 0){
			return distributeId.trim();
		}else{
			return DEFAULT_DISTRIBUTE_ID;
		}
	}
	
	@Override
	public String getFinalId(String partialId){
		return  partialId == null ? this.getDistributeId() : partialId + this.getDistributeId();
	}
}
