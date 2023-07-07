package ru.skypro.homework.service;

import liquibase.pro.packaged.M;
import liquibase.pro.packaged.O;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import ru.skypro.homework.component.AuthenticationComponent;
import ru.skypro.homework.dto.CommentDto;
import ru.skypro.homework.dto.ResponseWrapperAdsDto;
import ru.skypro.homework.dto.ResponseWrapperCommentDto;
import ru.skypro.homework.exception.ActionForbiddenException;
import ru.skypro.homework.exception.CommentNotFoundException;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.model.Advert;
import ru.skypro.homework.model.Comment;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.AdvertRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    @InjectMocks
    private CommentService commentService;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AdvertRepository advertRepository;
    @Mock
    private AuthenticationComponent auth;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private Authentication authentication;
    private Comment comment;

    @BeforeEach
    public void setup() {
        User user = new User();
        user.setId(1);
        user.setUsername("user@gmail.com");
        Advert advert = new Advert();
        advert.setId(1);
        comment = new Comment();
        comment.setId(1);
        comment.setAuthor(user);
        comment.setText("comment");
        comment.setAdvert(advert);
    }

    @Test
    public void create() {
        CommentDto expectedCommentDto = new CommentDto();
        expectedCommentDto.setPk(comment.getId());
        doReturn(Optional.of(comment.getAdvert())).when(advertRepository).findById(anyInt());
        doReturn(comment.getAuthor()).when(userRepository).findByUsername(any());
        doReturn(authentication).when(auth).getAuth();
        doReturn(comment).when(commentMapper).commentDtoToComment(any());
        doReturn(expectedCommentDto).when(commentMapper).commentToCommentDto(any());
        CommentDto actualCommentDto = commentService
                .create(comment.getAdvert().getId(), expectedCommentDto);
        assertNotNull(actualCommentDto);
        assertEquals(expectedCommentDto, actualCommentDto);
        verify(commentRepository, times(1)).save(any());
    }

    @Test
    public void delete() {
        doReturn(Optional.of(comment.getAdvert())).when(advertRepository).findById(anyInt());
        doReturn(Optional.of(comment)).when(commentRepository).findById(any());
        commentService.delete(comment.getAdvert().getId(), comment.getId());
        verify(commentRepository, times(1)).delete(any());
    }

    @Test
    public void update() {
        CommentDto expectedCommentDto = new CommentDto();
        expectedCommentDto.setPk(1);
        doReturn(Optional.of(comment)).when(commentRepository).findById(anyInt());
        doReturn(Optional.of(comment.getAdvert())).when(advertRepository).findById(anyInt());
        doNothing().when(commentMapper).updateComment(isA(CommentDto.class), isA(Comment.class));
        doReturn(expectedCommentDto).when(commentMapper).commentToCommentDto(any());
        CommentDto actualCommentDto = commentService
                .update(comment.getAdvert().getId(), comment.getId(), expectedCommentDto);
        assertNotNull(actualCommentDto);
        assertEquals(expectedCommentDto, actualCommentDto);
        verify(commentRepository, times(1)).save(any());
    }

    @Test
    public void findAll() {
        CommentDto commentDto = new CommentDto();
        commentDto.setPk(comment.getId());
        ResponseWrapperCommentDto expectedResponseWrapperCommentDto = new ResponseWrapperCommentDto();
        expectedResponseWrapperCommentDto.setCount(1);
        expectedResponseWrapperCommentDto.setResults(List.of(commentDto));
        doReturn(List.of(comment)).when(commentRepository).findAllByAdvertId(anyInt());
        doReturn(expectedResponseWrapperCommentDto).when(commentMapper).listToRespWrapperCommentDto(any());
        ResponseWrapperCommentDto actualResponseWrapperCommentDto = commentService
                .findAll(comment.getAdvert().getId());
        assertNotNull(actualResponseWrapperCommentDto);
        assertEquals(expectedResponseWrapperCommentDto, actualResponseWrapperCommentDto);
    }

    @Test
    public void findAdvert() throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        doReturn(Optional.of(comment.getAdvert())).when(advertRepository).findById(anyInt());
        Class[] parameters = new Class[1];
        parameters[0] = int.class;
        Method method = commentService
                .getClass()
                .getDeclaredMethod("findAdvert", parameters);
        method.setAccessible(true);
        Object[] arguments = new Object[1];
        arguments[0] = comment.getAdvert().getId();
        Advert actualAdvert = (Advert) method.invoke(commentService, arguments);
        Advert expectedAdvert = comment.getAdvert();
        assertNotNull(actualAdvert);
        assertEquals(expectedAdvert, actualAdvert);
    }

    @Test
    public void findCommentWithAuth() throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        doReturn(Optional.of(comment)).when(commentRepository).findById(anyInt());
        Class[] parameters = new Class[2];
        parameters[0] = Advert.class;
        parameters[1] = int.class;
        Method method = commentService.getClass()
                .getDeclaredMethod("findCommentWithAuth", parameters);
        method.setAccessible(true);
        Object[] arguments = new Object[2];
        arguments[0] = comment.getAdvert();
        arguments[1] = comment.getId();
        Comment actualComment = (Comment) method.invoke(commentService, arguments);
        assertNotNull(actualComment);
        assertEquals(comment,actualComment);
    }

    @Test
    public void doesThrowCommentNotFoundExceptionWhenDeleteOrUpdateWhereCommentIsNull() {
        Advert advert = new Advert();
        advert.setId(1);
        CommentDto commentDto = new CommentDto();
        commentDto.setPk(11);
        doReturn(Optional.empty()).when(commentRepository).findById(anyInt());
        doReturn(Optional.of(advert)).when(advertRepository).findById(anyInt());
        assertThrows(CommentNotFoundException.class,
                () -> commentService.delete(advert.getId(), comment.getId()));
        assertThrows(CommentNotFoundException.class,
                () -> commentService.update(advert.getId(), comment.getId(), commentDto));
    }

    @Test
    public void doesThrowCommentNotFoundExceptionWhenDeleteOrUpdateWhereAdvertOfCommentIdIsNotEqualsAdvertId() {
        Advert advert = new Advert();
        advert.setId(101);
        CommentDto commentDto = new CommentDto();
        commentDto.setPk(11);
        doReturn(Optional.of(comment)).when(commentRepository).findById(anyInt());
        doReturn(Optional.of(advert)).when(advertRepository).findById(anyInt());
        assertThrows(CommentNotFoundException.class,
                () -> commentService.delete(advert.getId(), comment.getId()));
        assertThrows(CommentNotFoundException.class,
                () -> commentService.update(advert.getId(), comment.getId(),commentDto));
    }

    @Test
    public void doesThrowActionForbiddenExceptionWhenDeleteOrUpdate() {
        boolean isAuthenticationNull = true;
        CommentDto commentDto = new CommentDto();
        commentDto.setPk(11);
        doReturn(Optional.of(comment)).when(commentRepository).findById(anyInt());
        doReturn(Optional.of(comment.getAdvert())).when(advertRepository).findById(anyInt());
        doReturn(isAuthenticationNull).when(auth).check(any());
        assertThrows(ActionForbiddenException.class,
                () -> commentService.delete(comment.getAdvert().getId(), comment.getId()));
        assertThrows(ActionForbiddenException.class,
                () -> commentService.update(comment.getAdvert().getId(), comment.getId(), commentDto));
    }
}
