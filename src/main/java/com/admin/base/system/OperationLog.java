package com.admin.base.system;

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
 * @date 2021/9/22 10:03 下午
 * @desc 操作日志记录表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tb_sys_operation_log")
public class OperationLog extends Model<OperationLog> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 日志主键
     */
//    @Excel(name = "操作序号", cellType = ColumnType.NUMERIC)
    @TableId( type = IdType.AUTO)
    private Integer operationId;

    /**
     * 操作模块
     */
//    @Excel(name = "操作模块")
    private String title;

    /**
     * 业务类型（0其它 1新增 2修改 3删除）
     */
//    @Excel(name = "业务类型", readConverterExp = "0=其它,1=新增,2=修改,3=删除,4=授权,5=导出,6=导入,7=强退,8=生成代码,9=清空数据")
    private Integer businessType;


    /**
     * 请求方法
     */
//    @Excel(name = "请求方法")
    private String method;

    /**
     * 请求方式
     */
//    @Excel(name = "请求方式")
    private String requestMethod;


    /**
     * 操作人员
     */
//    @Excel(name = "操作人员")
    private String operationName;


    /**
     * 请求url
     */
//    @Excel(name = "请求地址")
    private String operationUrl;

    /**
     * 操作地址
     */
//    @Excel(name = "操作地址")
    private String operationIp;


    /**
     * 请求参数
     */
//    @Excel(name = "请求参数")
    private String operationParam;

    /**
     * 返回参数
     */
//    @Excel(name = "返回参数")
    private String jsonResult;

    /**
     * 操作状态（0正常 1异常）
     */
//    @Excel(name = "状态", readConverterExp = "0=正常,1=异常")
    private Integer status;

    /**
     * 错误消息
     */
//    @Excel(name = "错误消息")
    private String errorMsg;

//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

}
