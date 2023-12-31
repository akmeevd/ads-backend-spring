package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.skypro.homework.dto.CommentDto;
import ru.skypro.homework.dto.ResponseWrapperCommentDto;
import ru.skypro.homework.model.Comment;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    static LocalDateTime toLocalDate(Long millis) {
        if (millis == null) {
            return null;
        }
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    static Long localDateTimeToMillis(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }

    @Mapping(source = "id", target = "pk")
    @Mapping(source = "author.id", target = "author")
    @Mapping(source = "author.firstName", target = "authorFirstName")
    @Mapping(expression = "java(getUrlToAvatar(comment))", target = "authorImage")
    CommentDto commentToCommentDto(Comment comment);


    @Mapping(target = "id", source = "pk")
    @Mapping(source = "author", target = "author.id")
    @Mapping(source = "authorFirstName", target = "author.firstName")
    Comment commentDtoToComment(CommentDto commentDto);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "author", ignore = true)
    void updateComment(CommentDto commentDto, @MappingTarget Comment comment);

    List<CommentDto> commentListToCommentDtoList(List<Comment> comments);

    default ResponseWrapperCommentDto listToRespWrapperCommentDto(List<Comment> comments) {
        ResponseWrapperCommentDto result = new ResponseWrapperCommentDto();
        result.setCount(comments.size());
        result.setResults(commentListToCommentDtoList(comments));
        return result;
    }

    default String getUrlToAvatar(Comment comment) {
        if (comment.getAuthor().getAvatar() == null) {
            return null;
        }
        return "/users/" + comment.getAuthor().getId() + "/image";
    }
}
