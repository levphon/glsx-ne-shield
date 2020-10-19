package cn.com.glsx.neshield.modules.entity;

import cn.com.glsx.auth.model.SyntheticUser;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "t_role_menu")
@Accessors(chain = true)
public class RoleMenu extends BaseEntity {

    public RoleMenu() {
        super();
    }

    public RoleMenu(boolean addOrUpdate) {
        SyntheticUser user = ShieldContextHolder.getUser();
        if (addOrUpdate) {
            this.setCreatedBy(user.getUserId());
            this.setUpdatedBy(user.getUserId());
            this.setCreatedDate(new Date());
            this.setUpdatedDate(new Date());
        } else {
            this.setUpdatedBy(user.getUserId());
            this.setUpdatedDate(new Date());
        }
    }

    /**
     * 角色id
     */
    @Column(name = "role_id")
    private Long roleId;

    /**
     * 菜单id
     */
    @Column(name = "menu_id")
    private Long menuId;
}