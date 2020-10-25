package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.Menu;
import cn.com.glsx.neshield.modules.model.MenuModel;
import cn.com.glsx.neshield.modules.model.param.MenuSearch;
import cn.com.glsx.neshield.modules.model.view.MenuDTO;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper extends CommonBaseMapper<Menu> {

    List<MenuDTO> selectList(MenuSearch search);

    /**
     * 根据条件获得菜单树-全部菜单
     *
     * @return
     */
    List<MenuModel> selectMenuFullTree();

    /**
     * 根据条件获得菜单树-授权菜单
     *
     * @param roleIds
     * @return
     */
    List<MenuModel> selectMenuPermTree(@Param("roleIds") List<Long> roleIds, @Param("menuTypes") List<Integer> menuTypes);

    List<MenuModel> selectMenuTreeByParentId(@Param("parentId") Long parentId, List<Long> roleIds);

    /**
     * 根据条件获得菜单
     *
     * @param roleIds
     * @return
     */
    List<Menu> selectMenuList(@Param("roleIds") List<Long> roleIds);

    /**
     * 根据父菜单id获得子菜单列表
     *
     * @return
     */
    List<Menu> selectMenuListByParentId(Long parentId);

    /**
     * 根据父菜单no获得子菜单列表
     *
     * @param parentMenuNo
     * @return
     */
    List<Menu> selectMenuListByLikeParentNo(Long parentMenuNo);

    /**
     * 逻辑删除
     *
     * @param id
     * @return
     */
    int logicDeleteById(@Param("id") Long id);

}