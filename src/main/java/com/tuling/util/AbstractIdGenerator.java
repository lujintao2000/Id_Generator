package com.tuling.util;

/**
 * 该类是所有需要装饰的IdGenerator类的父类
 * @author ljt
 * @date 2020-05-03
 *
 */
public abstract class AbstractIdGenerator implements IdGenerator<String>{

	private IdGenerator<String> idGenerator;

	public AbstractIdGenerator(IdGenerator idGenerator){
		this.idGenerator = idGenerator;
	}


    public String getId(){
        return this.getFinalId(idGenerator == null ? "" : this.idGenerator.getId());
    }

    /**
	 * 根据指定的字符串生成最终的ID
	 * @param partialId 部分ID
	 * @return  最终的ID
	 */
	public abstract String getFinalId(String partialId);

}
