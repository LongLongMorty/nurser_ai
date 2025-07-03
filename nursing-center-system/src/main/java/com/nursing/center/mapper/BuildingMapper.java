package com.nursing.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nursing.center.dto.BuildingDTO;
import com.nursing.center.dto.BuildingQueryDTO;
import com.nursing.center.entity.Building;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BuildingMapper extends BaseMapper<Building> {

    /**
     * 分页查询楼栋信息
     */
    IPage<BuildingDTO> selectBuildingPage(Page<BuildingDTO> page, @Param("query") BuildingQueryDTO query);

    /**
     * 根据ID查询楼栋详情
     */
    BuildingDTO selectBuildingById(@Param("id") Long id);

    /**
     * 获取所有启用的楼栋
     */
    List<BuildingDTO> selectEnabledBuildings();
}
