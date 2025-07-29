package com.zdd.entry.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class MeetingInviteDto {
    private String meetingName;
    private String inviteUserName;
    private String meetingId;
}