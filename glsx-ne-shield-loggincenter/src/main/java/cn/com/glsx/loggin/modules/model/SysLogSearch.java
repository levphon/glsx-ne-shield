package cn.com.glsx.loggin.modules.model;

import cn.hutool.db.Page;
import com.glsx.plat.common.utils.DateUtils;
import lombok.Data;

import java.util.Date;

@Data
public class SysLogSearch extends Page {

    private String sDate;
    private String eDate;

    public Date getStartDate() {
        if (sDate == null) {
            return null;
        }
        return DateUtils.parse(sDate);
    }

    public Date getEndDate(boolean withTime) {
        if (eDate == null) {
            return null;
        }
        if (withTime) {
            Date date = DateUtils.parse(eDate);
            //如果不带时间，日期加一天，相当于取当天最大时分秒 59:59:59
            if (date.getHours() == 0 && date.getMinutes() == 0 && date.getSeconds() == 0) {
                date = DateUtils.addDateDays(date, 1);
            }
            return date;
        }
        return DateUtils.parse(eDate);
    }

}
