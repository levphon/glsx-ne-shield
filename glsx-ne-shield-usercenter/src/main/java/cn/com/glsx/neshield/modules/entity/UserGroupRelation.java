package cn.com.glsx.neshield.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Table;

@Accessors(chain = true)
@Data
@Table(name = "t_user_group_relation")
public class UserGroupRelation extends BaseEntity {

    /**
     * 用户id
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 用户组id
     */
    @Column(name = "user_group_id")
    private Long userGroupId;

}