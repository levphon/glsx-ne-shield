package cn.com.glsx.neshield.modules.service.permissionStrategy;

import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.common.exception.UserCenterException;
import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.entity.User;
import cn.com.glsx.neshield.modules.mapper.DepartmentMapper;
import cn.com.glsx.neshield.modules.mapper.OrganizationMapper;
import cn.com.glsx.neshield.modules.mapper.UserMapper;
import cn.com.glsx.neshield.modules.model.OrgModel;
import cn.com.glsx.neshield.modules.model.OrgSuperiorModel;
import cn.com.glsx.neshield.modules.model.OrgTreeModel;
import cn.com.glsx.neshield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import cn.com.glsx.neshield.modules.service.DepartmentService;
import com.glsx.plat.common.model.TreeModel;
import com.glsx.plat.common.utils.TreeModelUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author taoyr
 */
@Component
public class OneselfStrategy extends PermissionStrategy {

    @Resource
    private UserMapper userMapper;

    @Resource
    private OrganizationMapper organizationMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private DepartmentService departmentService;

    @Override
    public List<Department> permissionDepartments() {
        throw new UserCenterException("权限错误调用，请检查");
    }

    @Override
    public List<User> permissionUsers() {
        List<User> list = Lists.newArrayList();
        User user = userMapper.selectById(ShieldContextHolder.getUserId());
        CollectionUtils.addAll(list, user);
        return list;
    }

    /**
     * 2 self 或 selfDepartment
     * * 2.1 root 找自己根部门
     * * 2.2 非root
     * * 先用rootId找到与自己部门的深度，看是上级还是下级（包括用户本部门）
     * * 2.2.1 上级
     * * 找确定深度的上级部门 先用rootId找到与自己部门的深度，-1得到确定深度，从t_org得到department_id（单个）
     * * 2.2.2 下级（本部门）
     * * 返回空
     * * 封装（false，false）-设置userNum为1（self）或设置userNum为自己部门人数（selfDepartment）-如果是本部门设置hasChild为false，非本部门设置hasChild为true
     *
     * @param rootId
     * @return
     */
    @Override
    public List<DepartmentDTO> orgSimpleList(Long rootId) {

        Long userDeptId = ShieldContextHolder.getDepartmentId();

        List<Department> departmentParamList = Lists.newArrayList();

        Department department = departmentMapper.selectById(userDeptId);

        //只能看他本人的部门
        departmentParamList.add(department);

        List<DepartmentDTO> departmentDTOList = departmentService.getDepartmentAssembled(departmentParamList, false, false);

        departmentDTOList.forEach(dep -> dep.setUserNumber(1));

        departmentDTOList.forEach(dep -> {
            if (!dep.getId().equals(userDeptId)) {
                dep.setHasChildren(true);
            }
        });
        return departmentDTOList;
    }

    @Override
    public List<? extends TreeModel> orgTree(OrgTreeSearch search) {

        Long tenantId = ShieldContextHolder.getTenantId();

        Long deptId = ShieldContextHolder.getDepartmentId();

        search.setTenantId(tenantId);

        search.setOrgId(deptId);

        List<OrgSuperiorModel> superiorModelList = organizationMapper.selectSuperiorIdsByOrg(search);
        Set<Long> ids = getSuperiorIds(superiorModelList);
        search.setOrgIds(ids);

        List<OrgModel> modelList = organizationMapper.selectOrgList(search);

        List<OrgTreeModel> orgTreeModelList = modelList.stream().map(OrgTreeModel::new).collect(Collectors.toList());

        orgTreeModelList.stream().filter(otm -> otm.getId().equals(deptId)).forEach(otm -> {
            otm.setUserNumber(1);
        });

        List<? extends TreeModel> orgTree = TreeModelUtil.fastConvertByDepth(orgTreeModelList, 0);

        return orgTree;
    }
}
