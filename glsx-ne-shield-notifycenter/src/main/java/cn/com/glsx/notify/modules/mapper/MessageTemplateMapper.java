package cn.com.glsx.notify.modules.mapper;

import cn.com.glsx.notify.modules.entity.MessageTemplate;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MessageTemplateMapper extends CommonBaseMapper<MessageTemplate> {

    MessageTemplate findByTemplate(@Param("template") String template);

    MessageTemplate findByTemplateAndType(@Param("template") String template, @Param("type") String type);

}