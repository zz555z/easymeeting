package com.zdd.entry.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeetingJoinDto implements Serializable {
    private MeetingMemberDto meetingMemberDto;

    private List<MeetingMemberDto> meetingMemberDtoList;




}