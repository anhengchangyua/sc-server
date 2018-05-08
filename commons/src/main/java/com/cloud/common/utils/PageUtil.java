package com.cloud.common.utils;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.util.CollectionUtils;

/**
 * 分页参数处理工具
 * 
 *  @author
 *
 */
public class PageUtil {

	/**
	 * 分页参数，起始位置，从0开始
	 */
	public static final String START = "start";
	/**
	 * 分页参数，每页数据条数
	 */
	public static final String LENGTH = "length";

	/**
	 * 转换分页参数<br>
	 * mybatis中limit #{start, JdbcType=INTEGER}, #{length,
	 * JdbcType=INTEGER}里的类型转换貌似失效<br>
	 * 我们这里先把他转成Integer的类型
	 * 
	 * @param params
	 */
	public static void pageParamConver(Map<String, Object> params) {
		if (!CollectionUtils.isEmpty(params)) {
			if (params.containsKey(START)) {
				params.put(START, MapUtils.getInteger(params, START));
			}

			if (params.containsKey(LENGTH)) {
				params.put(LENGTH, MapUtils.getInteger(params, LENGTH));
			}
		}
	}
}
