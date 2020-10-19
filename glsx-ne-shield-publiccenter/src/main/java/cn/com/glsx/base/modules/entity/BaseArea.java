package cn.com.glsx.base.modules.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "base_areas")
public class BaseArea implements Serializable {

    @Id
    private Long id;

    @Column(name = "city_code")
    private String cityCode;

    private String code;

    private String name;

    @Column(name = "province_code")
    private String provCode;

}