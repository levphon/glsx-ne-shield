package cn.com.glsx.auth.utils;

import cn.com.glsx.auth.model.*;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author payu
 */
@NoArgsConstructor
public class ShieldContextHolder {

    private static final Long adminRoleId = 1L;

    private static final ThreadLocal<SyntheticUser> USER_THREAD_LOCAL = new ThreadLocal<>();

    // 设置当前用户
    public static void setUser(SyntheticUser user) {
        USER_THREAD_LOCAL.set(user);
    }

    // 获取当前用户
    public static SyntheticUser getUser() {
        return USER_THREAD_LOCAL.get();
    }

    // 移除当前用户
    public static void removeUser() {
        USER_THREAD_LOCAL.remove();
    }

    public static boolean isSuperAdmin() {
        Long userId = getUserId();
        return userId != null && 1 == userId;
    }

    public static boolean isRoleAdmin(){
        Long roleId = getRoleId();
        return adminRoleId.equals(roleId);
    }

    public static Long getUserId() {
        SyntheticUser user = getUser();
        if (user != null) {
            return user.getUserId();
        }
        return null;
    }

    public static String getUsername() {
        SyntheticUser user = getUser();
        if (user != null) {
            return user.getUsername();
        }
        return null;
    }

    public static UserGroup getUserGroup() {
        SyntheticUser user = getUser();
        if (user != null) {
            return user.getUserGroup();
        }
        return null;
    }

    public static Department getDepartment() {
        SyntheticUser user = getUser();
        if (user != null) {
            return user.getDepartment();
        }
        return null;
    }

    public static Long getRootDepartmentId(){
        return null;
    }

    public static List<Long> getRoleIds() {
        SyntheticUser user = getUser();
        if (user != null) {
            if (CollectionUtils.isNotEmpty(user.getRoleList())) {
                return user.getRoleList().stream().map(Role::getRoleId).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    public static Long getRoleId(){
        return getRoleIds().get(0);
    }

    public static List<Permission> getRolePermissions(Long roleId) {
        //TODO
        return Lists.newArrayList();
    }

    /**
     * 获取当前用户角色可见的创建人id
     * @return
     */
    public static List<Long> getCreatorIds(){
        //TODO
        return Lists.newArrayList();
    }

    public static List<Menu> getRoleMenus(Long roleId){
        return getUser().getMenuList();
    }

    public static List<Permission> getUserPermissions() {
        List<Long> roleIds = getRoleIds();
        //TODO 根据roleIds组装permissions
        return Lists.newArrayList();
    }

    public static Integer getRolePermissionType() {
        return getRole().getRolePermissionType();
    }

    public static Role getRole(){
        return getUser().getRoleList().get(0);
    }

    public static Integer getRoleVisibility(){
        return getRole().getRoleVisibility();
    }
}
