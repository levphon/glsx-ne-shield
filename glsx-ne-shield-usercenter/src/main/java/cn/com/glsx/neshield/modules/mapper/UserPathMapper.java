package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.UserPath;
import cn.com.glsx.neshield.modules.model.view.DepartmentUserCount;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPathMapper extends CommonBaseMapper<UserPath> {

    List<DepartmentUserCount> selectSubordinateDepartmentList(Long userId);

    List<UserPath> selectUserSubordinateList(Long userId);

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

}