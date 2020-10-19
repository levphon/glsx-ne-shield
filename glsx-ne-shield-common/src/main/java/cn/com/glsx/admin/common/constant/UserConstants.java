package cn.com.glsx.admin.common.constant;

import com.glsx.plat.core.enums.SysConstants;

/**
 * @author: taoyr
 **/
public interface UserConstants extends SysConstants {

    Long adminRoleId = 1L;

    /**
     * 角色权限传播类型
     */
    enum RolePermitCastType implements SysConstants {
        oneself {
            @Override
            public Integer getCode() {
                return 0;
            }

            @Override
            public String getValue() {
                return "本人";
            }

            public String getBeanName(){
                return "oneselfStrategy";
            }
        },
        subordinate {
            @Override
            public Integer getCode() {
                return 1;
            }

            @Override
            public String getValue() {
                return "本人及下属";
            }

            public String getBeanName(){
                return "subOrdinateStrategy";
            }
        },
        selfDepartment {
            @Override
            public Integer getCode() {
                return 2;
            }

            @Override
            public String getValue() {
                return "本部门";
            }

            public String getBeanName(){
                return "selfDepartmentStrategy";
            }
        },
        subDepartment {
            @Override
            public Integer getCode() {
                return 3;
            }

            @Override
            public String getValue() {
                return "本部门及下属部门";
            }

            public String getBeanName(){
                return "subDepartmentStrategy";
            }
        },
        all {
            @Override
            public Integer getCode() {
                return 4;
            }

            @Override
            public String getValue() {
                return "全部";
            }

            public String getBeanName(){
                return "allStrategy";
            }
        },
        ;

        public abstract String getBeanName();

        public static String getValueByCode(Integer code) {
            for (RolePermitCastType status : RolePermitCastType.values()) {
                if (status.getCode().equals(code)) {
                    return status.getValue();
                }
            }
            return null;
        }

        public static String getBeanNameByCode(Integer code) {
            for (RolePermitCastType status : RolePermitCastType.values()) {
                if (status.getCode().equals(code)) {
                    return status.getBeanName();
                }
            }
            return null;
        }
    }

    /**
     * 角色可见度（0=共享，1=系统管理员，2=指定租户）
     */
    enum roleVisibility implements SysConstants {
        share {
            @Override
            public Integer getCode() {
                return 0;
            }

            @Override
            public String getValue() {
                return "共享";
            }
        },
        onlyAdmin {
            @Override
            public Integer getCode() {
                return 1;
            }

            @Override
            public String getValue() {
                return "超级管理员";
            }
        },
        specifyTenants {
            @Override
            public Integer getCode() {
                return 2;
            }

            @Override
            public String getValue() {
                return "指定租户";
            }
        },
        ;

        public static String getValueByCode(Integer code) {
            for (EnableStatus status : EnableStatus.values()) {
                if (status.getCode().equals(code)) {
                    return status.getValue();
                }
            }
            return null;
        }
    }
}
