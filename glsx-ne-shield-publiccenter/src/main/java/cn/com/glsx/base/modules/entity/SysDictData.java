package cn.com.glsx.base.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

@Data
@Table(name = "t_dict_data")
public class SysDictData extends BaseEntity {

    /**
     * 字典排序
     */
    @Column(name = "dict_sort")
    private Integer dictSort;

    /**
     * 字典标签
     */
    @Column(name = "dict_label")
    private String dictLabel;

    /**
     * 字典键值
     */
    @Column(name = "dict_value")
    private String dictValue;

    /**
     * 字典类型
     */
    @Column(name = "dict_type")
    private String dictType;

    /**
     * 是否默认（Y是 N否）
     */
    @Column(name = "is_default")
    private String isDefault;

    @Column(name = "enable_status")
    private Integer enableStatus;

    /**
     * 备注
     */
    private String remark;

}