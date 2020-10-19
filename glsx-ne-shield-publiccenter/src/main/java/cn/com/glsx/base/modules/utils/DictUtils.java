package cn.com.glsx.base.modules.utils;

import cn.com.glsx.base.modules.model.DictDataDTO;
import com.glsx.plat.redis.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 字典工具类
 */
@Component
public class DictUtils {

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 设置字典缓存
     *
     * @param key       参数键
     * @param dictDatas 字典数据列表
     */
    public void setDictCache(String key, List<DictDataDTO> dictDatas) {
        String fullKey = getCacheKey(key);
        redisUtils.set(fullKey, dictDatas);
    }

    /**
     * 获取字典缓存
     *
     * @param key 参数键
     * @return dictDatas 字典数据列表
     */
    public List<DictDataDTO> getDictCache(String key) {
        Object cacheObj = redisUtils.get(getCacheKey(key));
        if (Objects.isNull(cacheObj)) {
            return null;
        }
        return cast(cacheObj);
    }

    /**
     * 清空字典缓存
     */
    public void clearDictCache() {
        Collection<String> keys = redisUtils.keys(SYS_DICT_KEY + "*");
        redisUtils.del(keys);
    }

    /**
     * 设置cache key
     *
     * @param configKey 参数键
     * @return 缓存键key
     */
    public static String getCacheKey(String configKey) {
        return SYS_DICT_KEY + configKey;
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

}
