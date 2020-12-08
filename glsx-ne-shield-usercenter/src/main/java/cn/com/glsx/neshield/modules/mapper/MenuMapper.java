package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.Menu;
import cn.com.glsx.neshield.modules.model.MenuModel;
import cn.com.glsx.neshield.modules.model.param.MenuSearch;
import cn.com.glsx.neshield.modules.model.param.MenuTreeSearch;
import cn.com.glsx.neshield.modules.model.view.MenuDTO;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper extends CommonBaseMapper<Menu> {

    Menu selectById(Long id);

    Menu selectByMenuNo(@Param("menuNo") Long menuNo);

    List<MenuDTO> selectDTOList(MenuSearch search);

    /**
     * 根据条件获得菜单树-全部菜单
     *
     * @return
     */
    List<MenuModel> selectMenuFullTree();

    /**
     * 根据条件获得菜单树-授权菜单
     *
     * @param search
     * @return
     */
    List<MenuModel> selectMenuPermTree(MenuTreeSearch search);

    /**
     * 获取角色菜单树
     *
     * @param parentNo
     * @param roleIds
     * @return
     */
    List<MenuModel> selectMenuTreeByParentId(@Param("parentNo") Long parentNo, List<Long> roleIds);

    /**
     * 根据条件获得菜单
     *
     * @param roleIds
     * @return
     */
    List<Menu> selectList(@Param("roleIds") List<Long> roleIds);

    /**
     * 根据父菜单id获得子菜单列表
     *
     * @param parentNo
     * @return
     */
    List<Menu> selectByParentId(@Param("parentNo") Long parentNo);

    /**
     * 根据父菜单no获得子菜单列表
     *
     * @param parentNo
     * @return
     */
    List<Menu> selectByLikeParentNo(@Param("parentNo") Long parentNo);

    /**
     * 获取子菜单数量
     *
     * @param parentNo
     * @return
     */
    int selectChildrenCntByParentId(@Param("parentNo") Long parentNo);

    /**
     * 逻辑删除
     *
     * @param id
     * @return
     */
    @Deprecated
    int logicDeleteById(@Param("id") Long id);

}