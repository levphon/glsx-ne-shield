package cn.com.glsx.loggin.modules.service;

import cn.com.glsx.admin.common.constant.UserConstants;
import cn.com.glsx.auth.api.AuthFeignClient;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.loggin.modules.model.SysLogSearch;
import com.glsx.plat.loggin.entity.SysLogEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class LogginService {

    @Autowired
    private AuthFeignClient authFeignClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Page<SysLogEntity> search(SysLogSearch search) {

        Query query = new Query();

        Criteria criteria = buildCriteria(search);

        query.addCriteria(criteria);

        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");

        long total = mongoTemplate.count(query, SysLogEntity.class);

        //注意：分页索引从0开始
        Pageable pageable = PageRequest.of(search.getPageNumber() - 1, search.getPageSize(), sort);

        List<SysLogEntity> list = mongoTemplate.find(query.with(pageable), SysLogEntity.class);

        return new PageImpl(list, pageable, total);
    }

    public List<SysLogEntity> export(SysLogSearch search) {

        Query query = new Query();

        Criteria criteria = buildCriteria(search);

        query.addCriteria(criteria);

        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");

        List<SysLogEntity> list = mongoTemplate.find(query.with(sort), SysLogEntity.class);

        return list;
    }

    /**
     * 构建查询条件
     *
     * @param search
     * @return
     */
    private Criteria buildCriteria(SysLogSearch search) {
        Criteria criteria = new Criteria();

        Integer rolePermissionType = ShieldContextHolder.getRolePermissionType();
        //非管理员 并且 数据权限不是全部 的需要加上用户权限涉及的全部用户id
        if (!ShieldContextHolder.isSuperAdmin() && !UserConstants.RolePermitCastType.all.getCode().equals(rolePermissionType)) {
            Set<Long> userIdList = ShieldContextHolder.getVisibleCreatorIds();
            if (CollectionUtils.isNotEmpty(userIdList)) {
                criteria = Criteria.where("operator").in(ShieldContextHolder.getVisibleCreatorIds());
            } else {
                // 2020/10/13 直接无法查看日志数据
                criteria = Criteria.where("operator").is(0L);
            }
        }

        Date sDate = search.getStartDate();
        Date eDate = search.getEndDate(true);
        if (sDate != null && eDate != null) {
            criteria.andOperator(
                    Criteria.where("createdDate").gte(Objects.requireNonNull(dateToISODate(sDate))),
                    Criteria.where("createdDate").lt(Objects.requireNonNull(dateToISODate(eDate)))
            );
        }
        return criteria;
    }

    /**
     * mongo 日期查询isodate
     *
     * @param originDate
     * @return
     */
    public static Date dateToISODate(Date originDate) {
        //T代表后面跟着时间，Z代表UTC统一时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setCalendar(new GregorianCalendar(new SimpleTimeZone(0, "GMT")));
        try {
            String isoDate = format.format(originDate);
            return format.parse(isoDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
