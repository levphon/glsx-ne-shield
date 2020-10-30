package cn.com.glsx.base.modules.mapper;

import cn.com.glsx.base.modules.entity.SysDictType;
import cn.com.glsx.base.modules.model.DictDataSearch;
import cn.com.glsx.base.modules.model.DictTypeDTO;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysDictTypeMapper extends CommonBaseMapper<SysDictType> {

    List<DictTypeDTO> selectList(DictDataSearch search);

    List<SysDictType> selectAllDictType();

    List<DictTypeDTO> selectAllDictType2();

    SysDictType selectById(@Param("id") Long id);

    SysDictType selectByType(@Param("type") String dictType);

    int logicDeleteById(@Param("id") Long id);

}