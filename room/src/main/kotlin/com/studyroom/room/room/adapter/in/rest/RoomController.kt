package com.studyroom.room.room.adapter.`in`.rest

import com.studyroom.room.room.adapter.`in`.rest.dto.RoomListResponse
import com.studyroom.room.room.application.service.RoomQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/rooms")
class RoomController(
    private val roomQueryService: RoomQueryService,
) {
    @GetMapping
    fun findAll(): RoomListResponse = roomQueryService.findAll().toResponse()
}
