package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.UserPath;
import cn.com.glsx.neshield.modules.model.view.DepartmentUserCount;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPathMapper extends CommonBaseMapper<UserPath> {

    /**
     * 插入根节点路径
     *
     * @param userPath
     * @return
     */
    int insertRootPath(UserPath userPath);

    /**
     * 插入闭包路径
     *
     * @param userPath
     * @return
     */
    int insertUserPath(UserPath userPath);

    /**
     * 找到根路径
     *
     * @param tenantId
     * @return
     */
    UserPath selectRootPath(@Param("tenantId") Long tenantId);

    /**
     * 找到根路径
     *
     * @param subId
     * @return
     */
    UserPath selectRootPathBySubId(@Param("subId") Long subId);

    /**
     * 得到用户所有上级用户（含自己）
     *
     * @param subId
     * @return
     */
    List<UserPath> selectAllSuperiorBySubId(@Param("subId") Long subId);

    /**
     * 得到用户所有下级用户（含自己）
     *
     * @param superiorId
     * @return
     */
    List<UserPath> selectSubordinateBySuperiorId(@Param("superiorId") Long superiorId);

    /**
     * 统计用户下级各部门用户数
     *
     * @param userId
     * @return
     */
    List<DepartmentUserCount> selectSubordinateDepartmentList(Long userId);

}