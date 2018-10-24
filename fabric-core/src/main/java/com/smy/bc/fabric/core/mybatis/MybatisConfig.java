package com.smy.bc.fabric.core.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.stereotype.Component;

/**
 * <p>Title: MybatisConfig</p>
 * <p>Description: </p>
 *
 * @author zhaofeng
 * @version 1.0
 * @date 18-9-28
 */
@MapperScan("com.smy.bc.fabric.core.mybatis.mapper")
@Component
public class MybatisConfig {

}
