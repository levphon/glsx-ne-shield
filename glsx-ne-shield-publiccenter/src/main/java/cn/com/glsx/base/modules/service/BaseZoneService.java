package cn.com.glsx.base.modules.service;

import cn.com.glsx.base.modules.entity.BaseZone;
import cn.com.glsx.base.modules.mapper.BaseZoneMapper;
import com.glsx.plat.redis.utils.RedisUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class BaseZoneService {

    @Resource
    private BaseZoneMapper areaMapper;

    @Autowired
    private RedisUtils redisUtils;

    public List<BaseZone> searchByCityCode(String cityCode) {
        List<BaseZone> list = null;//redisUtils.getListByPrex(ConstantCacheKeys.REDIS_AREA_KEY + cityCode);
//        Collections.sort(list, Comparator.comparing(BaseArea::getCode));
        if (CollectionUtils.isEmpty(list)) list = areaMapper.selectByCityCode(cityCode);
        return list;
    }

}
