package cn.com.glsx.neshield.modules.controller;

import cn.com.glsx.admin.common.util.RegexUtil;
import cn.com.glsx.neshield.modules.model.UserDTO;
import cn.com.glsx.neshield.modules.model.param.UserSearch;
import cn.com.glsx.neshield.modules.service.UserService;
import cn.com.glsx.neshield.modules.BaseController;
import cn.com.glsx.neshield.modules.model.param.UserBO;
import cn.com.glsx.neshield.modules.model.export.UserExport;
import com.glsx.plat.common.annotation.SysLog;
import com.glsx.plat.common.enums.OperateType;
import com.glsx.plat.common.utils.DateUtils;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.context.utils.validator.AssertUtils;
import com.glsx.plat.core.constant.ResultConstants;
import com.glsx.plat.core.web.R;
import com.glsx.plat.office.excel.EasyExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * 单体应用Controller处理统一返回体
 *
 * @author payu
 */
@Slf4j
@RestController
@RequestMapping(value = "/user")
public class UserController extends BaseController {

    private final static String MODULE = "用户管理";

    @Resource
    private UserService userService;

    @GetMapping("/search")
    public R search(UserSearch search) {
        return userService.search(search);
    }

    @GetMapping(value = "/export")
    public void export(HttpServletResponse response, UserSearch search) throws Exception {
        List<UserExport> list = userService.export(search);
        EasyExcelUtils.writeExcel(response, list, "用户_" + DateUtils.formatSerial(new Date()), "Sheet1", UserExport.class);
    }

    @SysLog(module = MODULE, action = OperateType.ADD)
    @PostMapping(value = "/add")
    public R add(@RequestBody @Validated UserBO userBO) {
//        User user = UserConverter.INSTANCE.bo2do(userBO);
        String password = userBO.getPassword();
        if (StringUtils.isBlank(password) || !RegexUtil.regexPwd(password)) {
            return R.error(ResultConstants.ARGS_ERROR.getCode(), "密码格式错误");
        }

        return userService.addUser(userBO);
    }

    @SysLog(module = MODULE, action = OperateType.EDIT)
    @PostMapping(value = "/edit")
    public R edit(@RequestBody @Validated UserBO userBO) {
        AssertUtils.isNull(userBO.getId(), "ID不能为空");
//        User user = UserConverter.INSTANCE.bo2do(userBO);
        return userService.editUser(userBO);
    }

    @GetMapping(value = "/info")
    public R info(Long id) {
        UserDTO user = userService.userInfo(id);
        return R.ok().data(user);
    }

    @GetMapping(value = "/delete")
    public R delete(Long id) {
        userService.logicDeleteById(id);
        return R.ok();
    }

    @GetMapping("/suitableSuperUsers")
    public R suitableSuperUsers(Long departmentId){
        if (departmentId == null){
            return R.ok();
        }
        return userService.suitableSuperUsers(departmentId);
    }

}
