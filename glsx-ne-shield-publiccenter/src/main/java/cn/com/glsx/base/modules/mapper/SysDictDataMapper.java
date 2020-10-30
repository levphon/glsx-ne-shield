package cn.com.glsx.base.modules.mapper;

import cn.com.glsx.base.modules.entity.SysDictData;
import cn.com.glsx.base.modules.entity.SysDictType;
import cn.com.glsx.base.modules.model.DictDataDTO;
import cn.com.glsx.base.modules.model.DictDataSearch;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysDictDataMapper extends CommonBaseMapper<SysDictData> {

    List<DictDataDTO> selectList(DictDataSearch search);

    List<SysDictData> selectByType(@Param("type") String type);

    List<DictDataDTO> selectByType2(@Param("type") String type);

    List<DictDataDTO> selectByTypeWithDeleted(@Param("type") String type);

    SysDictData selectById(@Param("id") Long id);

    int logicDeleteById(@Param("id") Long id);

}