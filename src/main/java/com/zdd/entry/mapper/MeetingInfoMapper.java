package com.zdd.entry.mapper;

import com.zdd.entry.domain.MeetingInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author zdd
* @description 针对表【meeting_info】的数据库操作Mapper
* @createDate 2025-06-30 16:43:00
* @Entity com.zdd.entry.domain.MeetingInfo
*/
public interface MeetingInfoMapper extends BaseMapper<MeetingInfo> {

    @Select("select * from meeting_info where status = 1 and start_time >= #{startTime} and start_time <= #{endTime}")
    List<MeetingInfo> loadTodayMeeting();
}




