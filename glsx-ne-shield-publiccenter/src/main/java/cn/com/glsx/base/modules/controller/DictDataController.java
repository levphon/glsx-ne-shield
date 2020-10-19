package cn.com.glsx.base.modules.controller;

import cn.com.glsx.base.modules.converter.DictDataConverter;
import cn.com.glsx.base.modules.entity.SysDictData;
import cn.com.glsx.base.modules.model.DictDataBO;
import cn.com.glsx.base.modules.model.DictDataDTO;
import cn.com.glsx.base.modules.model.export.DictDataExport;
import cn.com.glsx.base.modules.model.DictDataSearch;
import cn.com.glsx.base.modules.service.DictDataService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.common.annotation.SysLog;
import com.glsx.plat.common.enums.OperateType;
import com.glsx.plat.common.utils.DateUtils;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.context.utils.validator.AssertUtils;
import com.glsx.plat.core.web.R;
import com.glsx.plat.office.excel.EasyExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;

/**
 * @author payu
 */
@RestController
@RequestMapping("dict/data")
public class DictDataController {

    private final static String MODULE = "字典管理";

    @Autowired
    private DictDataService dictDataService;

    @GetMapping("/search")
    public R search(DictDataSearch search) {
        PageHelper.startPage(search.getPageNumber(), search.getPageSize());
        PageInfo<DictDataDTO> pageInfo = dictDataService.searchData(search);
        return R.ok().putPageData(pageInfo);
    }

    @GetMapping(value = "/export")
    public void export(HttpServletResponse response, DictDataSearch search) throws Exception {
        List<DictDataExport> list = dictDataService.exportData(search);
        EasyExcelUtils.writeExcel(response, list, "字典数据_" + DateUtils.formatSerial(new Date()), "Sheet1", DictDataExport.class);
    }

    /**
     * 根据字典类型查询字典数据信息
     */
    @GetMapping(value = "/type/{dictType}")
    public R dictType(@PathVariable("dictType") String dictType) {
        List<DictDataDTO> list = dictDataService.getDictDataByTypeWithCached(dictType);
        return R.ok().data(list);
    }

    @SysLog(module = MODULE, action = OperateType.ADD)
    @PostMapping("/add")
    public R add(@RequestBody @Valid DictDataBO dataBO) {
        SysDictData data = DictDataConverter.INSTANCE.bo2do(dataBO);
        if (StringUtils.isNullOrEmpty(data.getIsDefault())) data.setIsDefault("N");

        // TODO: 2020/10/14 更新缓存
        dictDataService.addData(data);
        return R.ok();
    }

    @SysLog(module = MODULE, action = OperateType.EDIT)
    @PostMapping("/edit")
    public R edit(@RequestBody @Valid DictDataBO dataBO) {
        AssertUtils.isNull(dataBO.getId(), "ID不能为空");
        SysDictData data = DictDataConverter.INSTANCE.bo2do(dataBO);

        // TODO: 2020/10/14 更新缓存
        dictDataService.editData(data);
        return R.ok();
    }

    @GetMapping("/info")
    public R info(@RequestParam("dataId") Long id) {
        SysDictData data = dictDataService.getDictDataById(id);
        DictDataDTO dataDTO = DictDataConverter.INSTANCE.do2dto(data);
        return R.ok().data(dataDTO);
    }

    @GetMapping("/delete")
    public R delete(@RequestParam("dataId") Long id) {
        dictDataService.logicDeleteDataById(id);
        return R.ok();
    }

}
