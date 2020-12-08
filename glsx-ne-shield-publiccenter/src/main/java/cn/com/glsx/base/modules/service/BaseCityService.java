package cn.com.glsx.base.modules.service;

import cn.com.glsx.base.modules.entity.BaseCity;
import cn.com.glsx.base.modules.mapper.BaseCityMapper;
import com.glsx.plat.redis.utils.RedisUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class BaseCityService {

    @Resource
    private BaseCityMapper cityMapper;

    @Autowired
    private RedisUtils redisUtils;

    public List<BaseCity> list(String provCode) {
        List<BaseCity> list = null;//redisUtils.getListByPrex(ConstantCacheKeys.REDIS_CITY_KEY + provinceCode);
//        Collections.sort(list, Comparator.comparing(BaseCity::getCode));
        if (CollectionUtils.isEmpty(list)) list = cityMapper.selectByProvCode(provCode);
        return list;
    }
}
