package cn.com.glsx.base.common.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.glsx.plat.core.web.R;
import com.glsx.plat.exception.ExceptionLevel;
import com.glsx.plat.exception.GlobalExceptionHandler;
import com.glsx.plat.exception.SystemMessage;
import com.glsx.plat.loggin.LogginConstants;
import com.glsx.plat.loggin.LogginStrategyFactory;
import feign.FeignException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.BindingException;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 *
 * @author payu
 */
@Slf4j
@RestControllerAdvice
public class PublicCenterExceptionHandler extends GlobalExceptionHandler {

    @Autowired
    private LogginStrategyFactory logginStrategyFactory;

    @ExceptionHandler(PublicCenterException.class)
    public R handleAdminException(PublicCenterException e) {
        this.saveException(e, ExceptionLevel.fatal);
        return R.error(e.getErrorCode(), e.getMessage());
    }

    /**
     * 引入Feign服务的话
     *
     * @param e
     * @return
     */
    @ExceptionHandler(FeignException.class)
    public R handleFeignException(FeignException e) {
        this.saveException(e, ExceptionLevel.fatal);
        return R.error(e.status(), e.getMessage());
    }

    /**
     * Mybatis BindingException
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BindingException.class)
    public R handleBindingException(BindingException e) {
        this.saveException(e, ExceptionLevel.fatal);
        return R.error(e.getMessage());
    }

    @ExceptionHandler(value = MyBatisSystemException.class)
    public R myBatisSystemException(MyBatisSystemException e) {
        log.error("数据存储出错", e);
        this.saveException(e, ExceptionLevel.fatal);
        return SystemMessage.DATA_HANDLE_ERROR.result();
    }

    @ExceptionHandler(value = UnrecognizedPropertyException.class)
    public R unrecognizedPropertyException(UnrecognizedPropertyException e) {
        log.error("不能识别的字段", e);
        this.saveException(e, ExceptionLevel.fatal);
        return SystemMessage.ARGS_ERROR.result();
    }

    @SneakyThrows
    @Async
    @Override
    protected void saveException(Exception e, ExceptionLevel level) {
        // TODO: 2020/10/14 异常入库,对此方法进行aop处理
        String logId = MDC.get(LogginConstants.MDC_LOG_DB_ID);
        log.error("操作异常/失败ID:{}", MDC.get(LogginConstants.MDC_LOG_DB_ID));
        logginStrategyFactory.getStrategy().updateLogStatus(logId, "失败");
        MDC.clear();
    }

}
