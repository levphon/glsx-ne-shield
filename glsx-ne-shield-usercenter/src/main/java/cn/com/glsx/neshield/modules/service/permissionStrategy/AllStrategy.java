package cn.com.glsx.neshield.modules.service.permissionStrategy;

import cn.com.glsx.admin.common.constant.Constants;
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
import cn.com.glsx.neshield.modules.model.param.OrganizationSearch;
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import cn.com.glsx.neshield.modules.service.DepartmentService;
import com.glsx.plat.common.model.TreeModel;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.common.utils.TreeModelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author taoyr
 */
@Slf4j
@Component
public class AllStrategy extends PermissionStrategy {

    @Resource
    private OrganizationMapper organizationMapper;

    @Resource
    private UserMapper userMapper;

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
        throw new UserCenterException("权限错误调用，请检查");
    }

    /**
     * 1 all
     * * 1.1 root 找所有根部门
     * * 1.2 非root 根据rootId找子部门list
     * * 封装-不过滤-返回
     *
     * @param rootId
     * @return
     */
    @Override
    public List<DepartmentDTO> orgSimpleList(Long rootId) {
        List<Department> departmentParamList;
        if (rootId == null) {
            departmentParamList = departmentMapper.selectDepartmentList(new Department().setIsRoot(Constants.IS_ROOT_DEPARTMENT));
        } else {
            departmentParamList = organizationMapper.selectChildrenList(new OrganizationSearch().setRootId(rootId));
        }
        return departmentService.getDepartmentAssembled(departmentParamList, true, true);
    }

    /**
     * 1.查询符合条件的部门nameDepartmentList
     * 2.找出idList所有向上的路径经过的所有部门allDepartmentIdList
     * 3.找出所有组织链organizationList
     * 4.找出根节点rootList
     * 5.封装-调用TreeModelUtil组装树
     *
     * @param search
     * @return
     */
    @Override
    public List<? extends TreeModel> orgTree(OrgTreeSearch search) {
        if (StringUtils.isNotEmpty(search.getOrgName())) {
            List<OrgSuperiorModel> superiorModelList = organizationMapper.selectSuperiorIdsByOrg(search);
            Set<Long> ids = getSuperiorIds(superiorModelList);
            search.setOrgIds(ids);
        }

        List<OrgModel> modelList = organizationMapper.selectOrgList(search);

        List<Long> departmentIdList = modelList.stream().map(OrgModel::getOrgId).collect(Collectors.toList());
        //计算用户数
        Map<Long, Integer> recursiveDepartmentUserMap = departmentService.countRecursiveDepartmentUser(departmentIdList);

        List<OrgTreeModel> orgTreeModelList = modelList.stream().map(OrgTreeModel::new).collect(Collectors.toList());

        orgTreeModelList.forEach(otm -> {
            Integer number = recursiveDepartmentUserMap.get(otm.getId());
            otm.setUserNumber(number == null ? 0 : number);
        });

        List<? extends TreeModel> orgTree = TreeModelUtil.fastConvertByDepth(orgTreeModelList, 0);

        return orgTree;
    }

}
