package cn.com.glsx.base.modules.service;

import cn.com.glsx.base.modules.entity.SysDictData;
import cn.com.glsx.base.modules.entity.SysDictType;
import cn.com.glsx.base.modules.mapper.SysDictDataMapper;
import cn.com.glsx.base.modules.mapper.SysDictTypeMapper;
import cn.com.glsx.base.modules.model.*;
import cn.com.glsx.base.modules.model.export.DictDataExport;
import cn.com.glsx.base.modules.model.export.DictTypeExport;
import cn.com.glsx.base.modules.utils.DictUtils;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictDataService implements InitializingBean {

    @Autowired
    private SysDictTypeMapper typeMapper;

    @Autowired
    private SysDictDataMapper dataMapper;

    @Autowired
    private DictUtils dictUtils;

    /**
     * 项目启动时，初始化字典到缓存
     */
    public void init() {
        List<DictTypeDTO> dictTypeList = this.getAllDictType2();
        for (DictTypeDTO dictType : dictTypeList) {
            List<DictDataDTO> dictDatas = this.getDictDataByType2(dictType.getDictType());
            dictUtils.setDictCache(dictType.getDictType(), dictDatas);
        }
    }

    public PageInfo<DictTypeDTO> searchType(DictDataSearch search) {
        List<DictTypeDTO> list = typeMapper.selectList(search);
        return new PageInfo<>(list);
    }

    public PageInfo<DictDataDTO> searchData(DictDataSearch search) {
        List<DictDataDTO> list = dataMapper.selectList(search);
        return new PageInfo<>(list);
    }

    public List<SysDictType> getAllDictType() {
        return typeMapper.selectAllDictType();
    }

    public List<DictTypeDTO> getAllDictType2() {
        return typeMapper.selectAllDictType2();
    }

    public List<SysDictData> getDictDataByType(String type) {
        return dataMapper.selectByType(type);
    }

    public List<DictDataDTO> getDictDataByType2(String type) {
        return dataMapper.selectByType2(type);
    }

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    public List<DictDataDTO> getDictDataByTypeWithCached(String dictType) {
        List<DictDataDTO> dictDatas = dictUtils.getDictCache(dictType);
        if (CollectionUtils.isNotEmpty(dictDatas)) {
            return dictDatas;
        }
        dictDatas = this.getDictDataByType2(dictType);
        if (CollectionUtils.isNotEmpty(dictDatas)) {
            dictUtils.setDictCache(dictType, dictDatas);
            return dictDatas;
        }
        return null;
    }

    public void addType(SysDictType type) {
        typeMapper.insert(type);
    }

    public void addData(SysDictData data) {
        dataMapper.insert(data);
    }

    public void editType(SysDictType type) {
        typeMapper.updateByPrimaryKeySelective(type);
    }

    public void editData(SysDictData data) {
        dataMapper.updateByPrimaryKeySelective(data);
    }

    public SysDictType getDictTypeById(Long id) {
        return typeMapper.selectByPrimaryKey(id);
    }

    public SysDictData getDictDataById(Long id) {
        return dataMapper.selectByPrimaryKey(id);
    }

    public void logicDeleteTypeById(Long id) {
        typeMapper.logicDeleteById(id);
    }

    public void logicDeleteDataById(Long id) {
        dataMapper.logicDeleteById(id);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        init();
    }

    public List<DictTypeExport> exportType(DictDataSearch search) {
        return null;
    }

    public List<DictDataExport> exportData(DictDataSearch search) {
        return null;
    }

}
