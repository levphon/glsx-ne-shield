package cn.com.glsx.neshield.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

@Data
@Table(name = "t_permission")
public class Permission extends BaseEntity {

    /**
     * 菜单id
     */
    @Column(name = "permission_tag")
    private Integer permissionTag;

    /**
     * 接口路径
     */
    @Column(name = "interface_url")
    private String interfaceUrl;

}