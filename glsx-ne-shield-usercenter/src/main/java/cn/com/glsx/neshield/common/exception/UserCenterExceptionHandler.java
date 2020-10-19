package cn.com.glsx.neshield.common.exception;

import com.glsx.plat.core.web.R;
import com.glsx.plat.exception.ExceptionLevel;
import com.glsx.plat.exception.GlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.BindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 *
 * @author payu
 */
@Slf4j
@RestControllerAdvice
public class UserCenterExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(UserCenterException.class)
    public R handleAdminException(UserCenterException e) {
        return R.error(e.getErrorCode(), e.getMessage());
    }

    /**
     * 引入Feign服务的话
     *
     * @param e
     * @return
     */
//    @ExceptionHandler(FeignException.class)
//    public R handleFeignException(FeignException e) {
//        return R.error(e.status(), e.getMessage());
//    }

    /**
     * Mybatis BindingException
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BindingException.class)
    public R handleBindingException(BindingException e) {
        return R.error(e.getMessage());
    }

    @Override
    protected void saveException(Exception e, ExceptionLevel level) {
        // TODO: 2020/10/14 异常入库
    }

}
