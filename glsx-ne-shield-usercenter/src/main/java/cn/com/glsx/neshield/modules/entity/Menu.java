package cn.com.glsx.neshield.modules.entity;

import cn.com.glsx.auth.model.SyntheticUser;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "t_menu")
@Data
@Accessors(chain = true)
public class Menu extends BaseEntity {

    public Menu() {
        super();
    }

    public Menu(boolean isAdd) {
        if (isAdd) {
            SyntheticUser user = ShieldContextHolder.getUser();
            this.setCreatedBy(user.getUserId());
            this.setCreatedDate(new Date());
        }
    }

    /**
     * 菜单no
     */
    @Column(name = "menu_no")
    private String menuNo;

    /**
     * 菜单名
     */
    @Column(name = "menu_name")
    private String menuName;

    /**
     * 前端路由
     */
    @Column(name = "front_route")
    private String frontRoute;

    /**
     * 权限名称
     */
    @Column(name = "permission_tag")
    private String permissionTag;

    /**
     * 类型 1目录，2菜单，3按钮
     */
    private Integer type;

    /**
     * 图标
     */
    private String icon;

    /**
     * 父菜单id
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 排序
     */
    @Column(name = "order_num")
    private Integer orderNum;

    /**
     * 状态 0=不显示 1=显示
     */
    @Column(name = "enable_status")
    private Integer enableStatus;

}