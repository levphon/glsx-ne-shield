package cn.com.glsx.neshield.modules.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginBO {

    @NotBlank
    private String account;

    @NotBlank
    private String password;

    private String captcha;

    private String captchaId;

}
