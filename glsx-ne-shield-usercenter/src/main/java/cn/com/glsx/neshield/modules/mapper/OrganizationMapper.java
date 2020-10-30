package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.entity.Organization;
import cn.com.glsx.neshield.modules.model.OrgModel;
import cn.com.glsx.neshield.modules.model.OrgSuperiorModel;
import cn.com.glsx.neshield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.neshield.modules.model.param.OrganizationBO;
import cn.com.glsx.neshield.modules.model.param.OrganizationSearch;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrganizationMapper extends CommonBaseMapper<Organization> {

    /**
     * 插入根节点路径
     *
     * @param organization
     * @return
     */
    int insertRootPath(Organization organization);

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
     * 获取父级id（含自己）
     *
     * @param search
     * @return
     */
    List<OrgSuperiorModel> selectSuperiorIdsByOrg(OrgTreeSearch search);

    /**
     * 获取组织机构
     *
     * @param search
     * @return
     */
    List<OrgModel> selectOrgList(OrgTreeSearch search);

    /**
     * 找到所有子节点
     *
     * @param organizationId
     * @return
     */
    List<Organization> selectByRootId(Long organizationId);

    /**
     * 找到根节点list
     *
     * @param subIds
     * @return
     */
    List<Organization> selectRootIdList(@Param("subIds") List<Long> subIds);

    /**
     * 找到根路径
     *
     * @param tenantId
     * @return
     */
    Organization selectRootPath(@Param("tenantId") Long tenantId);

    /**
     * 找到根路径
     *
     * @param subId
     * @return
     */
    Organization selectRootPathBySubId(@Param("subId") Long subId);

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
    List<Organization> selectSubList(@Param("departmentIds") List<Long> departmentIds, @Param("depth") Integer depth);

    /**
     * 根据条件筛选符合的路径
     *
     * @param organizationBO
     * @return
     */
    List<Organization> selectList(OrganizationBO organizationBO);

    /**
     * @param rootId
     * @param subId
     * @return
     */
    Organization selectRootByRootIdAndSubId(@Param("rootId") Long rootId, @Param("subId") Long subId);

    List<Organization> selectAllSuperiorBySubId(@Param("subId") Long subId);

}