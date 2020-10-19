package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.Organization;
import cn.com.glsx.neshield.modules.model.OrgModel;
import cn.com.glsx.neshield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.model.param.OrganizationBO;
import cn.com.glsx.neshield.modules.model.param.OrganizationSearch;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrganizationMapper extends CommonBaseMapper<Organization> {

    /**
     * 插入闭包路径
     *
     * @param organization
     * @return
     */
    int insertOrganizationPath(Organization organization);

    /**
     * 删除闭包路径
     *
     * @param nodeId
     * @return
     */
    int deleteOrganizationPath(Long nodeId);

    /**
     * 获取组织机构
     *
     * @param search
     * @return
     */
    List<OrgModel> selectOrgList(OrgTreeSearch search);

    /**
     * 根据id获取组织机构
     *
     * @param search
     * @return
     */
    List<OrgModel> selectOrgTree(OrgTreeSearch search);

    /**
     * 获取父级id（含自己）
     *
     * @param name
     * @return
     */
    List<String> selectSuperiorIdsByName(@Param("name") String name);

    /**
     * 插入根节点路径
     *
     * @param organization
     * @return
     */
    int insertRootPath(Organization organization);

    /**
     * 找到所有子节点
     *
     * @param organizationId
     * @return
     */
    List<Organization> selectByRootId(Long organizationId);

    /**
     * 找到根路径
     *
     * @param organizationId
     * @return
     */
    Organization selectRootPath(Long organizationId);

    /**
     * 找到根节点list
     *
     * @param subIdList
     * @return
     */
    List<Organization> selectRootIdList(@Param("subIdList") List<Long> subIdList);

    /**
     * 得到上级节点
     *
     * @param organizationId
     * @return
     */
    Organization selectSuperiorOrganization(Long organizationId);

    /**
     * 删除所有
     *
     * @param organizationId
     * @return
     */
    int logicDeleteAllSubOrganization(Long organizationId);


    /**
     * 查找子节点列表
     *
     * @param organizationSearch
     * @return
     */
    List<Department> selectChildrenList(OrganizationSearch organizationSearch);

    /**
     * 查找确定深度的子节点列表
     *
     * @param departmentIds
     * @return
     */
    List<Organization> selectSubList(List<Long> departmentIds, Integer depth);

    /**
     * 根据条件筛选符合的路径
     *
     * @param organizationBO
     * @return
     */
    List<Organization> selectList(OrganizationBO organizationBO);
}