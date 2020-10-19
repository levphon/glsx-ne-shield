package cn.com.glsx.neshield.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Table;

@Accessors(chain = true)
@Data
@Table(name = "t_device")
public class Device extends BaseEntity {

    /**
     * 硬件名称
     */
    private String name;

    /**
     * 硬件类型
     */
    private Integer type;

    /**
     * 型号id
     */
    @Column(name = "model_id")
    private Integer modelId;

    /**
     * 型号名称
     */
    @Column(name = "model_name")
    private String modelName;

    /**
     * 版本
     */
    private String version;

}