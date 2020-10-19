package cn.com.glsx.base.modules.controller;

import cn.com.glsx.base.modules.converter.DictTypeConverter;
import cn.com.glsx.base.modules.entity.SysDictType;
import cn.com.glsx.base.modules.model.DictDataSearch;
import cn.com.glsx.base.modules.model.DictTypeBO;
import cn.com.glsx.base.modules.model.DictTypeDTO;
import cn.com.glsx.base.modules.model.export.DictTypeExport;
import cn.com.glsx.base.modules.service.DictDataService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.common.annotation.SysLog;
import com.glsx.plat.common.enums.OperateType;
import com.glsx.plat.common.utils.DateUtils;
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
@RequestMapping("dict/type")
public class DictTypeController {

    private final static String MODULE = "字典管理";

    @Autowired
    private DictDataService dictDataService;

    @GetMapping("/search")
    public R search(DictDataSearch search) {
        PageHelper.startPage(search.getPageNumber(), search.getPageSize());
        PageInfo<DictTypeDTO> pageInfo = dictDataService.searchType(search);
        return R.ok().putPageData(pageInfo);
    }

    @GetMapping(value = "/export")
    public void export(HttpServletResponse response, DictDataSearch search) throws Exception {
        List<DictTypeExport> list = dictDataService.exportType(search);
        EasyExcelUtils.writeExcel(response, list, "字典类型_" + DateUtils.formatSerial(new Date()), "Sheet1", DictTypeExport.class);
    }

    @SysLog(module = MODULE, action = OperateType.ADD)
    @PostMapping("/add")
    public R add(@RequestBody @Valid DictTypeBO typeBO) {
        SysDictType type = DictTypeConverter.INSTANCE.bo2do(typeBO);
        dictDataService.addType(type);
        return R.ok();
    }

    @SysLog(module = MODULE, action = OperateType.EDIT)
    @PostMapping("/edit")
    public R edit(@RequestBody @Valid DictTypeBO typeBO) {
        AssertUtils.isNull(typeBO.getId(), "ID不能为空");
        SysDictType type = DictTypeConverter.INSTANCE.bo2do(typeBO);
        dictDataService.editType(type);
        return R.ok();
    }

    @GetMapping("/info")
    public R info(@RequestParam("typeId") Long id) {
        SysDictType type = dictDataService.getDictTypeById(id);
        DictTypeDTO typeDTO = DictTypeConverter.INSTANCE.do2dto(type);
        return R.ok().data(typeDTO);
    }

    @GetMapping("/delete")
    public R delete(@RequestParam("typeId") Long id) {
        dictDataService.logicDeleteTypeById(id);
        return R.ok();
    }

    @GetMapping("/options")
    public R options() {
        List<DictTypeDTO> list = dictDataService.getAllDictType2();
        return R.ok().data(list);
    }

}
