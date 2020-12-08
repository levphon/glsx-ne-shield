package cn.com.glsx.kafka.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@Data
@Table(name = "t_channel_risk_data")
public class ChannelRiskData implements Serializable {
    @Id
    @Column(
            name = "id",
            unique = true,
            nullable = false
    )
    @GeneratedValue(
            strategy = GenerationType.IDENTITY,
            generator = "SELECT LAST_INSERT_ID()"
    )
    private Long id;

    /**
     * 工单编号
     */
    @Column(name = "workorder_no")
    private String workorderNo;

    /**
     * 车主姓名
     */
    @Column(name = "owner_name")
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
    @Column(name = "active_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date activeDate;

    /**
     * 设备类型
     */
    @Column(name = "dev_type")
    private Integer devType;

    /**
     * gps上点时间
     */
    @Column(name = "gps_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
    @Column(name = "shop_name")
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
    @Column(name = "street_number")
    private String streetNumber;

    /**
     * 渠道分析位置经度
     */
    @Column(name = "center_lat")
    private Double centerLat;

    /**
     * 渠道分析位置纬度
     */
    @Column(name = "center_lng")
    private Double centerLng;
}