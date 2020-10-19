package cn.com.glsx.base.modules.service;

import cn.com.glsx.base.modules.entity.BaseProvince;
import cn.com.glsx.base.modules.mapper.BaseProvinceMapper;
import com.glsx.plat.redis.utils.RedisUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class BaseProvincesService {

    @Resource
    private BaseProvinceMapper provinceMapper;

    @Autowired
    private RedisUtils redisUtils;

    public List<BaseProvince> list() {
        List<BaseProvince> list = null;//redisUtils.getListByPrex(ConstantCacheKeys.REDIS_PROVINCES_KEY);
        //排序
//        Collections.sort(list, Comparator.comparing(BaseProvince::getCode));
        if (CollectionUtils.isEmpty(list)) list = provinceMapper.selectAll();
        return list;
    }
}
