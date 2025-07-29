package com.zdd.entry.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeetingMemberDto implements Serializable {
    private String userId;
    private String nickName;
    private String avatar;
    private Long joinTime;
    private Integer memberType;
    private Integer status;
    private Boolean videoOpen;
    private Integer sex;




}