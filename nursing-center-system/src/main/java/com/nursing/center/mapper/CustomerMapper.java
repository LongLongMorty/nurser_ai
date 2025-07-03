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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nursing.center.dto.CustomerDTO;
import com.nursing.center.dto.CustomerQueryDTO;
import com.nursing.center.dto.OutingCustomerDTO;
import com.nursing.center.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {    /**
     * 分页查询客户信息（包含关联信息）
     */
    IPage<CustomerDTO> selectCustomerPage(Page<CustomerDTO> page, @Param("query") CustomerQueryDTO query);

    /**
     * 根据ID查询客户详细信息
     */
    CustomerDTO selectCustomerById(@Param("id") Long id);

    /**
     * 分页查询外出客户（床位状态为OUT的客户）
     */
    IPage<OutingCustomerDTO> selectOutingCustomerPage(Page<OutingCustomerDTO> page, @Param("query") CustomerQueryDTO query);
    
    /**
     * 获取在住客户详细信息列表
     */
    List<Map<String, Object>> getCustomerDetails();
}
