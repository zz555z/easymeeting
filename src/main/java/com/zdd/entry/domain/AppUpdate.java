package com.zdd.entry.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * app发布
 * </p>
 *
 * @author zdd
 * @since 2025-03-05
 */
@Getter
@Setter
@TableName("app_update")
public class AppUpdate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 版本号
     */
    private String version;

    /**
     * 更新描述
     */
    private String updateDesc;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 0:未发布 1:灰度发布 2:全网发布
     */
    private Integer status;

    /**
     * 灰度uid
     */
    private String grayscaleUid;

    /**
     * 文件类型 0:本地文件 1:外链
     */
    private Integer fileType;

    /**
     * 外链地址
     */
    private String outerLink;
}
