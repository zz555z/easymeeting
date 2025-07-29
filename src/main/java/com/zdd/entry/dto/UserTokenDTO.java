package com.zdd.entry.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserTokenDTO implements Serializable {


    private static final long serialVersionUID = 1L;
    private String userId;
    private String nickName;
    private String avatar;
    private String token;
    private String currentMeetingId;  //加入的会议id
    private String currentMeetingName;  //会议中的名字
    private String mettingNo;  //自己的会议好
    private Boolean admin;
    private Integer sex;
    private long expireAt;


}
