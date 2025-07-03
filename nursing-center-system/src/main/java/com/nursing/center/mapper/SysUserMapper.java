package com.nursing.center.mapper;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.mapper
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  13:09
 * @Description: TODO
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nursing.center.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    
    /**
     * 获取在线管家详细信息列表
     */
    List<Map<String, Object>> getOnlineManagerDetails();
}