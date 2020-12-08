package cn.com.glsx.kafka.model;

import lombok.Data;
import java.util.Date;

@Data
public class ChannerRiskBo {
    /**
     * 工单编号
     */
    private String workorderNo;

    /**
     * 车主姓名
     */
    private String ownerName;

    /**
     * 车架号
     */
    private String vin;

    /**
     * 设备sn
     */
    private String sn;

    /**
     * 激活时间
     */
    private Date activeDate;

    /**
     * 设备类型
     */
    private Integer devType;

    /**
     * gps上点时间
     */
    private Date gpsTime;

    /**
     * 经度
     */
    private Double lng;

    /**
     * 纬度
     */
    private Double lat;

    /**
     * 省份
     */
    private String prov;

    /**
     * 城市
     */
    private String city;

    /**
     * 县区
     */
    private String zone;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 渠道名称
     */
    private String shopName;

    /**
     * 标签标识
     */
    private String labels;

    /**
     * 数据类型:0正常数据,1疑似风险
     */
    private Integer type;

    /**
     * 渠道位置分析半径
     */
    private Integer radius;

    /**
     * 渠道地址县区
     */
    private String district;

    /**
     * 渠道地址街道
     */
    private String street;

    /**
     * 渠道地址街道号
     */
    private String streetNumber;

    /**
     * 渠道分析位置经度
     */
    private Double centerLat;

    /**
     * 渠道分析位置纬度
     */
    private Double centerLng;
}
