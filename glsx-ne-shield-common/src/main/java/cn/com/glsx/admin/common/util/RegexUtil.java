package cn.com.glsx.admin.common.util;

/**
 * @author taoyr
 */
public class RegexUtil {

    //public static final String pwdRegex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,18}$";
    public static final String pwdRegex = "^[0-9A-Za-z]{6,18}$";

    public static final String mobileRegex = "^1(3|4|5|7|8|9)\\d{9}$";

    public static boolean regexPwd(String pwd) {
        return pwd.matches(pwdRegex);
    }

}
