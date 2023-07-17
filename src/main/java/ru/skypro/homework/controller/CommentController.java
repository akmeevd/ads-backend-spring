package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.CommentDto;
import ru.skypro.homework.dto.ResponseWrapperCommentDto;
import ru.skypro.homework.service.CommentService;

@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("/ads")
@Tag(name = "Comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add comment to advert", responses = {
            @ApiResponse(responseCode = "201", content = {@Content(schema = @Schema(
                    implementation = CommentDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<CommentDto> create(@PathVariable("id") Integer id,
                                             @RequestBody CommentDto text) {
        return new ResponseEntity<>(commentService.create(id, text), HttpStatus.CREATED);
    }

    @DeleteMapping("/{adId}/comments/{commentId}")
    @Operation(summary = "Delete comment", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<?> delete(@PathVariable("adId") Integer adId,
                                    @PathVariable("commentId") Integer commentId) {
        commentService.delete(adId, commentId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{adId}/comments/{commentId}")
    @Operation(summary = "Update comment", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(
                    implementation = CommentDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<CommentDto> update(@PathVariable("adId") Integer adId,
                                             @PathVariable("commentId") Integer commentId,
                                             @RequestBody CommentDto comment) {
        return ResponseEntity.ok(commentService.update(adId, commentId, comment));
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Get comments for advert", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(
                    implementation = ResponseWrapperCommentDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<ResponseWrapperCommentDto> findAllByAdvert(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(commentService.findAll(id));
    }
}
