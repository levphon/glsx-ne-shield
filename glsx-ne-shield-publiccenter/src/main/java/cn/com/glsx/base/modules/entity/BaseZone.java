package cn.com.glsx.base.modules.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "base_zone")
public class BaseZone implements Serializable {

    @Id
    private Long id;

    @Column(name = "city_code")
    private String cityCode;

    private String code;

    private String name;

    @Column(name = "prov_code")
    private String provCode;

}