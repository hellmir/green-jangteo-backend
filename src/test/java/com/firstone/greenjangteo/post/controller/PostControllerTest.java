package com.firstone.greenjangteo.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstone.greenjangteo.post.domain.image.dto.ImageRequestDto;
import com.firstone.greenjangteo.post.domain.image.testutil.ImageTestObjectFactory;
import com.firstone.greenjangteo.post.domain.view.model.entity.View;
import com.firstone.greenjangteo.post.domain.view.model.service.ViewService;
import com.firstone.greenjangteo.post.dto.PostRequestDto;
import com.firstone.greenjangteo.post.model.entity.Post;
import com.firstone.greenjangteo.post.service.PostService;
import com.firstone.greenjangteo.post.utility.PostTestObjectFactory;
import com.firstone.greenjangteo.user.model.Username;
import com.firstone.greenjangteo.user.model.entity.User;
import com.firstone.greenjangteo.user.security.CustomAuthenticationEntryPoint;
import com.firstone.greenjangteo.user.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.firstone.greenjangteo.order.testutil.OrderTestConstant.BUYER_ID;
import static com.firstone.greenjangteo.order.testutil.OrderTestConstant.SELLER_ID1;
import static com.firstone.greenjangteo.post.utility.PostTestConstant.*;
import static com.firstone.greenjangteo.user.testutil.UserTestConstant.USERNAME1;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = PostController.class)
class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private ViewService viewService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @DisplayName("올바른 게시글 등록 양식을 전송하면 게시글을 등록할 수 있다.")
    @Test
    @WithMockUser(username = BUYER_ID, roles = {"BUYER"})
    void createPost() throws Exception {
        // given
        User user = mock(User.class);
        List<ImageRequestDto> imageRequestDtos = ImageTestObjectFactory.createImageRequestDtos();
        PostRequestDto postRequestDto = PostTestObjectFactory
                .createPostRequestDto(BUYER_ID, SUBJECT, CONTENT, imageRequestDtos);

        Post post = PostTestObjectFactory.createPost(Long.parseLong(POST_ID), SUBJECT, CONTENT, user);

        when(postService.createPost(any(PostRequestDto.class))).thenReturn(post);
        when(user.getId()).thenReturn(Long.parseLong(BUYER_ID));
        when(user.getUsername()).thenReturn(Username.of(USERNAME1));

        // when, then
        mockMvc.perform(post("/posts")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(postRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @DisplayName("게시글 ID와 게시자 ID를 입력해 게시글을 조회할 수 있다.")
    @Test
    @WithMockUser
    void getPost() throws Exception {
        // given
        User user = mock(User.class);
        Post post = PostTestObjectFactory.createPost(Long.parseLong(POST_ID), SUBJECT, CONTENT, user);
        View view = mock(View.class);

        when(postService.getPost(anyLong(), anyLong())).thenReturn(post);
        when(viewService.addAndGetView(anyLong())).thenReturn(view);
        when(user.getId()).thenReturn(Long.parseLong(BUYER_ID));
        when(user.getUsername()).thenReturn(Username.of(USERNAME1));

        // when, then
        mockMvc.perform(get("/posts/{postId}", POST_ID)
                        .queryParam("writerId", SELLER_ID1))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
