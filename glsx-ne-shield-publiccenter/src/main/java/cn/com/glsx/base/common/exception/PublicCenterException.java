package cn.com.glsx.base.common.exception;

import com.glsx.plat.exception.SystemMessage;
import lombok.Getter;

/**
 * @author payu
 */
@Getter
public class PublicCenterException extends RuntimeException {

    private int errorCode = SystemMessage.FAILURE.getCode();

    public PublicCenterException(String message) {
        super(message);
    }

    public PublicCenterException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public static PublicCenterException of(SystemMessage message) {
        return new PublicCenterException(message.getCode(), message.getMsg());
    }

    public PublicCenterException(Throwable cause) {
        super(cause);
    }

    public PublicCenterException(String message, Throwable cause) {
        super(message, cause);
    }

}
