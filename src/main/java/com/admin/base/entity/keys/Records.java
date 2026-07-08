package com.admin.base.entity.keys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ZXX
 * @version 1.0
 * @date 2023/4/3 10:08
 * @desc 识别结果记录
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_records")
public class Records extends Model<Records> implements Serializable {
    /**
     * 关键词ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 上传文件名
     */
    private String fileName;
    /**
     * 上传用户名
     */
    private Integer userId;
    /**
     * 关键词列表
     */
    private String keyWordList;
    /**
     * 识别结果
     */
    private String resultPath;

    /**
     * 是否有识别结果
     * 0:未匹配到关键词内容
     * 1：匹配到关键词内容
     */
    private Integer resultState;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
