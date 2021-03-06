package cn.com.glsx.loggin.modules.controller;

import cn.com.glsx.loggin.modules.model.SysLogSearch;
import cn.com.glsx.loggin.modules.service.LogginService;
import com.glsx.plat.common.utils.DateUtils;
import com.glsx.plat.core.web.R;
import com.glsx.plat.loggin.entity.SysLogEntity;
import com.glsx.plat.office.excel.EasyExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("loggin")
public class LogginController {

    @Autowired
    private LogginService logginService;

    //只需要加上下面这段即可，注意不能忘记注解
    @InitBinder
    public void initBinder(WebDataBinder binder, WebRequest request) {
        //转换日期
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));// CustomDateEditor为自定义日期编辑器
    }

    @GetMapping("/search")
    public R search(SysLogSearch search) {
        Page<SysLogEntity> pageInfo = logginService.search(search);
        return R.ok().putPageData(pageInfo);
    }

    @GetMapping(value = "/export")
    public void export(HttpServletResponse response, SysLogSearch search) throws Exception {
        List<SysLogEntity> list = logginService.export(search);
        EasyExcelUtils.writeExcel(response, list, "操作日志_" + DateUtils.formatSerial(new Date()), "Sheet1", SysLogEntity.class);
    }

}
