package com.zdd.entry.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class PeerConnectionDataDto implements Serializable {

    private String token;
    private String sendUserId;
    private String receiveUserId;
    private String signalType;
    private String signalData;
}
