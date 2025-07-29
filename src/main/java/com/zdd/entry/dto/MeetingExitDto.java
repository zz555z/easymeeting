package com.zdd.entry.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeetingExitDto implements Serializable {
    private String exitUserId;
    private Integer exitStatus;

    private List<MeetingMemberDto> meetingMemberDtoList;


}