package com.nursing.center.controller;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.controller
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  13:12
 * @Description: TODO
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.common.result.Result;
import com.nursing.center.dto.CustomerDTO;
import com.nursing.center.dto.CustomerQueryDTO;
import com.nursing.center.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping("/api/admin/customer")
@RequiredArgsConstructor
@Validated
public class CustomerController {

    private final CustomerService customerService;

   
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<CustomerDTO>> getCustomerPage(CustomerQueryDTO query) {
        IPage<CustomerDTO> page = customerService.getCustomerPage(query);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CustomerDTO> getCustomerById(@PathVariable Long id) {
        CustomerDTO customer = customerService.getCustomerById(id);
        return Result.success(customer);
    }

    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> addCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        Long customerId = customerService.addCustomer(customerDTO);
        return Result.success("客户添加成功", customerId);
    }

   
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        customerService.updateCustomer(customerDTO);
        return Result.success("客户信息修改成功");
    }

    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return Result.success("客户删除成功");
    }

    /**
     * 入住登记接口
     */
    @PostMapping("/{id}/checkin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> checkInCustomer(@PathVariable Long id, @Valid @RequestBody CheckInRequestDTO checkInRequest) {
        customerService.checkInCustomer(id, checkInRequest);
        return Result.success("入住登记成功");
    }

    /**
     * 入住登记请求DTO
     */
    public static class CheckInRequestDTO {
        @javax.validation.constraints.NotNull(message = "楼栋ID不能为空")
        private Long buildingId;
        
        @javax.validation.constraints.NotNull(message = "房间ID不能为空")
        private Long roomId;
        
        @javax.validation.constraints.NotNull(message = "床位ID不能为空")
        private Long bedId;
        
        @javax.validation.constraints.NotBlank(message = "入住时间不能为空")
        private String checkInDate;
        
        @javax.validation.constraints.NotBlank(message = "合同到期时间不能为空")
        private String contractExpireDate;

        // Getters and Setters
        public Long getBuildingId() {
            return buildingId;
        }

        public void setBuildingId(Long buildingId) {
            this.buildingId = buildingId;
        }

        public Long getRoomId() {
            return roomId;
        }

        public void setRoomId(Long roomId) {
            this.roomId = roomId;
        }

        public Long getBedId() {
            return bedId;
        }

        public void setBedId(Long bedId) {
            this.bedId = bedId;
        }

        public String getCheckInDate() {
            return checkInDate;
        }

        public void setCheckInDate(String checkInDate) {
            this.checkInDate = checkInDate;
        }

        public String getContractExpireDate() {
            return contractExpireDate;
        }

        public void setContractExpireDate(String contractExpireDate) {
            this.contractExpireDate = contractExpireDate;
        }
    }
}
