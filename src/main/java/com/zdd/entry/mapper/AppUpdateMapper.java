package com.zdd.entry.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zdd.entry.domain.AppUpdate;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * app发布 Mapper 接口
 * </p>
 *
 * @author zdd
 * @since 2025-03-05
 */
public interface AppUpdateMapper extends BaseMapper<AppUpdate> {

    @Select("select * from app_update where status = #{staus} order by version desc limit 1")
    AppUpdate findOrderByCreateTime(@Param("staus") int status);


    @Select("select *  from app_update  order by version desc limit 1")
    AppUpdate findMacVersion();
}
