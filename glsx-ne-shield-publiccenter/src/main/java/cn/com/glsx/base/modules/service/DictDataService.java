package cn.com.glsx.base.modules.service;

import cn.com.glsx.base.modules.entity.SysDictData;
import cn.com.glsx.base.modules.entity.SysDictType;
import cn.com.glsx.base.modules.mapper.SysDictDataMapper;
import cn.com.glsx.base.modules.mapper.SysDictTypeMapper;
import cn.com.glsx.base.modules.model.DictDataDTO;
import cn.com.glsx.base.modules.model.DictDataSearch;
import cn.com.glsx.base.modules.model.DictTypeDTO;
import cn.com.glsx.base.modules.model.export.DictDataExport;
import cn.com.glsx.base.modules.model.export.DictTypeExport;
import cn.com.glsx.base.modules.utils.DictUtils;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.core.enums.SysConstants;
import com.glsx.plat.exception.BusinessException;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        // TODO: 2020/11/2 使用缓存
        List<DictDataDTO> dictDatas = Lists.newArrayList();//dictUtils.getDictCache(dictType);
        if (CollectionUtils.isNotEmpty(dictDatas)) {
            return dictDatas;
        }
        SysDictType type = typeMapper.selectByTypeWithDeleted(dictType);
        Optional<SysDictType> typeOpt = Optional.ofNullable(type);
        if (typeOpt.isPresent()) {
            dictDatas = dataMapper.selectByTypeWithDeleted(dictType);
            if (CollectionUtils.isNotEmpty(dictDatas)) {
                boolean typeDisabled = !SysConstants.EnableStatus.enable.getCode().equals(type.getEnableStatus());
                boolean typeDeleted = SysConstants.DeleteStatus.delete.getCode().equals(type.getDelFlag());
                dictDatas.forEach(dd -> {
                    boolean disabled = !SysConstants.EnableStatus.enable.getCode().equals(dd.getEnableStatus());
                    boolean deleted = SysConstants.DeleteStatus.delete.getCode().equals(dd.getDelFlag());
                    //字典数据停用
                    dd.setDisabled(disabled);
                    //字典数据删除
                    dd.setDeleted(deleted);

                    //字典类型停用
                    if (typeDisabled) {
                        dd.setTypeDisabled(typeDisabled);
                        dd.setDisabled(typeDisabled);
                    }
                    //字典类型删除
                    if (typeDeleted) {
                        dd.setTypeDeleted(typeDeleted);
                        dd.setDeleted(typeDeleted);
                    }
                });
                dictUtils.setDictCache(dictType, dictDatas);
                return dictDatas;
            }
        }
        return Lists.newArrayList();
    }

    public void addType(SysDictType type) {
        checkAddType(type);
        // TODO: 2020/11/2 更新缓存
        typeMapper.insert(type);
    }

    public void editType(SysDictType type) {
        checkEditType(type);
        // TODO: 2020/11/2 更新缓存
        typeMapper.updateByPrimaryKeySelective(type);
    }

    public void addData(SysDictData data) {
        checkAddData(data);
        // TODO: 2020/11/2 更新缓存
        dataMapper.insert(data);
    }

    public void editData(SysDictData data) {
        checkEditData(data);
        // TODO: 2020/11/2 更新缓存
        dataMapper.updateByPrimaryKeySelective(data);
    }

    private void checkAddType(SysDictType type) {
        SysDictType dbType1 = typeMapper.selectByNameWithDeleted(type.getDictName());
        if (dbType1 != null) {
            boolean delFlag = SysConstants.DeleteStatus.delete.getCode().equals(dbType1.getDelFlag());
            throw BusinessException.create("字典名称已存在" + (delFlag ? "，但处于删除状态" : ""));
        }
        SysDictType dbType2 = typeMapper.selectByTypeWithDeleted(type.getDictType());
        if (dbType2 != null) {
            boolean delFlag = SysConstants.DeleteStatus.delete.getCode().equals(dbType2.getDelFlag());
            throw BusinessException.create("字典类型已存在" + (delFlag ? "，但处于删除状态" : ""));
        }
    }

    private void checkEditType(SysDictType type) {
        SysDictType dbType1 = typeMapper.selectByNameWithDeleted(type.getDictName());
        if (dbType1 != null && !dbType1.getId().equals(type.getId())) {
            boolean delFlag = SysConstants.DeleteStatus.delete.getCode().equals(dbType1.getDelFlag());
            throw BusinessException.create("字典名称已存在" + (delFlag ? "，但处于删除状态" : ""));
        }
        SysDictType dbType2 = typeMapper.selectByTypeWithDeleted(type.getDictType());
        if (dbType2 != null && !dbType2.getId().equals(type.getId())) {
            boolean delFlag = SysConstants.DeleteStatus.delete.getCode().equals(dbType2.getDelFlag());
            throw BusinessException.create("字典类型已存在" + (delFlag ? "，但处于删除状态" : ""));
        }
    }

    private void checkAddData(SysDictData data) {
        SysDictData condition = new SysDictData()
                .setDictType(data.getDictType())
                .setDictValue(data.getDictValue());
        condition.setDelFlag(null);
        SysDictData dbData = dataMapper.selectOne(condition);
        if (dbData != null) {
            boolean delFlag = SysConstants.DeleteStatus.delete.getCode().equals(dbData.getDelFlag());
            throw BusinessException.create("字典数据键值已存在" + (delFlag ? "，但处于删除状态" : ""));
        }
    }

    private void checkEditData(SysDictData data) {
        SysDictData condition = new SysDictData()
                .setDictType(data.getDictType())
                .setDictValue(data.getDictValue());
        condition.setDelFlag(null);
        SysDictData dbData = dataMapper.selectOne(condition);
        if (dbData != null && !dbData.getId().equals(data.getId())) {
            boolean delFlag = SysConstants.DeleteStatus.delete.getCode().equals(dbData.getDelFlag());
            throw BusinessException.create("字典数据键值已存在" + (delFlag ? "，但处于删除状态" : ""));
        }
    }

    public SysDictType getDictTypeById(Long id) {
        return typeMapper.selectById(id);
    }

    public SysDictData getDictDataById(Long id) {
        return dataMapper.selectById(id);
    }

    public void logicDeleteTypeById(Long id) {
        // TODO: 2020/11/2 更新缓存
        typeMapper.logicDeleteById(id);
    }

    public void logicDeleteDataById(Long id) {
        // TODO: 2020/11/2 更新缓存
        dataMapper.logicDeleteById(id);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        init();
    }

    public List<DictTypeExport> exportType(DictDataSearch search) {
        return Lists.newArrayList();
    }

    public List<DictDataExport> exportData(DictDataSearch search) {
        return Lists.newArrayList();
    }

}
