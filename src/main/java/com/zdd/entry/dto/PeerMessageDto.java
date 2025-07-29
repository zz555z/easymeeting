package com.zdd.entry.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class PeerMessageDto implements Serializable {


    private String signalType;
    private String signalData;
}
