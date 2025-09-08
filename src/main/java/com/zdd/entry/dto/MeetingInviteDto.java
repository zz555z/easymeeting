package com.zdd.entry.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Data
public class MeetingInviteDto implements Serializable {
    private String meetingName;
    private String inviteUserName;
    private String meetingId;
}