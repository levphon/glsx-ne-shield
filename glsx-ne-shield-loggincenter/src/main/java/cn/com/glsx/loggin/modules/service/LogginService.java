package cn.com.glsx.loggin.modules.service;

import cn.com.glsx.admin.common.constant.UserConstants;
import cn.com.glsx.auth.api.AuthFeignClient;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.loggin.modules.model.SysLogSearch;
import com.alibaba.fastjson.JSONArray;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.core.web.R;
import com.glsx.plat.loggin.LogginStrategyFactory;
import com.glsx.plat.loggin.entity.SysLogEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogginService {

    @Autowired
    private LogginStrategyFactory logginStrategyFactory;

    @Autowired
    private AuthFeignClient authFeignClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Page<SysLogEntity> search(SysLogSearch search) {

        Query query = new Query();

        Criteria criteria = buildCriteria(search);

        query.addCriteria(criteria);

        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");
        //注意：分页索引从0开始
        Pageable pageable = PageRequest.of(search.getPageNumber() - 1, search.getPageSize(), sort);

        List<SysLogEntity> list = mongoTemplate.find(query.with(pageable), SysLogEntity.class);

        long total = mongoTemplate.count(query, SysLogEntity.class);

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
            R result = authFeignClient.getRelationAuthUserIds();
            if (result.isSuccess()) {
                List<Long> userIdList = ((JSONArray) result.getData()).toJavaList(Long.class);
                if (CollectionUtils.isNotEmpty(userIdList)) {
                    criteria = Criteria.where("operator").in(userIdList);
                } else {
                    // 2020/10/13 直接无法查看日志数据
                    criteria = Criteria.where("operator").is(0L);
                }
            }
        }

        if (StringUtils.isNotEmpty(search.getSDate()) && StringUtils.isNotEmpty(search.getEDate())) {
            criteria.andOperator(
                    Criteria.where("createdDate").gte(search.getSDate()),
                    Criteria.where("createdDate").lt(search.getEDate())
            );
        }
        return criteria;
    }

}
