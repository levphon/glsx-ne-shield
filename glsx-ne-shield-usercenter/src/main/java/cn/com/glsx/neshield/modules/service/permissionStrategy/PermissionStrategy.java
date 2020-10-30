package cn.com.glsx.neshield.modules.service.permissionStrategy;

import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.entity.User;
import cn.com.glsx.neshield.modules.model.OrgSuperiorModel;
import cn.com.glsx.neshield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import com.glsx.plat.common.model.TreeModel;
import com.glsx.plat.common.utils.StringUtils;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author taoyr
 */
public abstract class PermissionStrategy {

    /**
     * 根据角色数据权限范围获取对应的部门（组织）
     *
     * @return
     */
    public abstract List<Department> permissionDepartments();

    /**
     * 根据角色数据权限范围获取对应的人
     *
     * @return
     */
    public abstract List<User> permissionUsers();

    /**
     * 获取组织机构列表，需求特殊处理（比如本人角色数据权限为本人，但是要求能看的他自己的（包含自己建和非自己建）上级组织结构数据）
     *
     * @param rootId
     * @return
     */
    public abstract List<DepartmentDTO> orgSimpleList(Long rootId);

    /**
     * 获取组织树
     *
     * @param search
     * @return
     */
    public abstract List<? extends TreeModel> orgTree(OrgTreeSearch search);

    /**
     * 获取上级组织id
     *
     * @param superiorModelList
     * @return
     */
    public Set<Long> getSuperiorIds(List<OrgSuperiorModel> superiorModelList) {
        Set<Long> superiorIds = Sets.newHashSet();
        superiorModelList.forEach(osm -> {
            if (StringUtils.isNotEmpty(osm.getSuperiorIds())) {
                String[] ids = osm.getSuperiorIds().split(",");
                for (String id : ids) {
                    superiorIds.add(Long.valueOf(id));
                }
            }
        });
        return superiorIds;
    }

    public Set<Long> getSuperiorIds(Long deptId, Map<Long, String> idsStrMap) {
        String idsStr = idsStrMap.get(deptId);
        Set<Long> superiorIds = Sets.newHashSet();
        if (StringUtils.isNotEmpty(idsStr)) {
            String[] ids = idsStr.split(",");
            for (String id : ids) {
                superiorIds.add(Long.valueOf(id));
            }
        }
        return superiorIds;
    }

}
