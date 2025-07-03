package com.nursing.center.controller;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.controller
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  15:30
 * @Description: Room management controller
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.common.result.Result;
import com.nursing.center.dto.RoomDTO;
import com.nursing.center.dto.RoomQueryDTO;
import com.nursing.center.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/page")
    public Result<IPage<RoomDTO>> getRoomPage(@RequestBody @Valid RoomQueryDTO query) {
        IPage<RoomDTO> page = roomService.getRoomPage(query);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<RoomDTO> getRoomById(@PathVariable Long id) {
        RoomDTO room = roomService.getRoomById(id);
        return Result.success(room);
    }

    @PostMapping
    public Result<Long> addRoom(@RequestBody @Valid RoomDTO roomDTO) {
        Long id = roomService.addRoom(roomDTO);
        return Result.success("房间添加成功", id);
    }

    @PutMapping
    public Result<Void> updateRoom(@RequestBody @Valid RoomDTO roomDTO) {
        roomService.updateRoom(roomDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return Result.success();
    }

    @GetMapping("/building/{buildingId}")
    public Result<List<RoomDTO>> getRoomsByBuildingId(@PathVariable Long buildingId) {
        List<RoomDTO> rooms = roomService.getRoomsByBuildingId(buildingId);
        return Result.success(rooms);
    }

    @GetMapping("/enabled")
    public Result<List<RoomDTO>> getEnabledRooms() {
        List<RoomDTO> rooms = roomService.getEnabledRooms();
        return Result.success(rooms);
    }

    @GetMapping("/grouped/building/{buildingId}")
    public Result<List<RoomDTO>> getRoomsGroupedByFloor(@PathVariable Long buildingId) {
        List<RoomDTO> rooms = roomService.getRoomsGroupedByFloor(buildingId);
        return Result.success(rooms);
    }
}
