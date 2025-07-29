package com.zdd.entry.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingDto implements Serializable {

    private Integer maxImageSize = 2;
    private Integer maxVideoSize = 5;
    private Integer maxFileSize = 5;

   }