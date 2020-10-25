package cn.com.glsx.neshield.common.exception;

import com.glsx.plat.exception.SystemMessage;
import lombok.Getter;

/**
 * @author payu
 */
@Getter
public class UserCenterException extends RuntimeException {

    private int errorCode = SystemMessage.FAILURE.getCode();

    public UserCenterException(String message) {
        super(message);
    }

    public UserCenterException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public static UserCenterException of(SystemMessage message) {
        return new UserCenterException(message.getCode(), message.getMsg());
    }

    public UserCenterException(Throwable cause) {
        super(cause);
    }

    public UserCenterException(String message, Throwable cause) {
        super(message, cause);
    }

}
