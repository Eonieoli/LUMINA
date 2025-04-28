package com.lumina.backend.post.service.impl;

import com.lumina.backend.category.model.entity.Category;
import com.lumina.backend.category.repository.CategoryRepository;
import com.lumina.backend.category.repository.UserCategoryRepository;
import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.post.model.entity.*;
import com.lumina.backend.post.model.request.UploadCommentRequest;
import com.lumina.backend.post.model.request.UploadPostRequest;
import com.lumina.backend.post.model.response.GetChildCommentResponse;
import com.lumina.backend.post.model.response.GetCommentResponse;
import com.lumina.backend.post.model.response.GetPostResponse;
import com.lumina.backend.post.repository.*;
import com.lumina.backend.post.service.PostService;
import com.lumina.backend.post.service.S3Service;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.repository.FollowRepository;
import com.lumina.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final HashtagRepository hashtagRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final FollowRepository followRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserCategoryRepository userCategoryRepository;

    private final S3Service s3Service;


    /**
     * 게시물을 업로드하는 메서드
     *
     * @param userId 게시물을 업로드한 사용자
     * @param request UploadPostRequest 요청 바디
     */
    @Override
    @Transactional
    public void uploadPost(
            Long userId, UploadPostRequest request) throws IOException {

        if (request.getCategoryName() == null || request.getCategoryName().trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "카테고리은 필수 입력값입니다.");
        }

        if (request.getPostContent() == null || request.getPostContent().trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "게시물 내용은 필수 입력값입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음: " + userId ));

        Category category = categoryRepository.findByCategoryName(request.getCategoryName())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없음: " + request.getCategoryName()));

        Post post;
        if (request.getPostImageFile() != null && !request.getPostImageFile().isEmpty()) {
            String postImage = s3Service.uploadImageFile(request.getPostImageFile(), "post/");
            post = new Post(user, category, postImage, request.getPostContent(), 0);
        } else {
            post = new Post(user, category, request.getPostContent(), 0);
        }
        postRepository.save(post);

        // 해시태그 처리
        if (request.getHashtag() != null && !request.getHashtag().isEmpty()) {
            for (String hashtagName : request.getHashtag()) {
                // 1. 해시태그 존재 여부 확인
                Hashtag hashtag = hashtagRepository.findByHashtagName(hashtagName)
                        .orElseGet(() -> hashtagRepository.save(new Hashtag(hashtagName))); // 없으면 저장

                // 2. PostHashtag 연결 저장
                PostHashtag postHashtag = new PostHashtag(post, hashtag);
                postHashtagRepository.save(postHashtag);
            }
        }
    }


    @Override
    public Map<String, Object> getPosts(Long myId, Long userId, String categoryName, int pageNum) {

        if (pageNum < 1) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "페이지 번호는 1 이상의 값이어야 합니다.");
        }

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage;

        if (userId != null) {
            // 특정 유저 게시물 조회
            postPage = postRepository.findByUserId(userId, pageRequest);

        } else if (categoryName != null) {
            // 특정 카테고리 게시물 조회
            Long categoryId = categoryRepository.findIdByCategoryName(categoryName);
            postPage = postRepository.findByCategoryId(categoryId, pageRequest);

        } else {
            // 팔로우한 사람의 게시물만 조회
            List<Long> followingIds = followRepository.findFollowingIdsByFollowerId(myId);
            postPage = postRepository.findByUserIdIn(followingIds, pageRequest);
        }

        List<GetPostResponse> posts = postPage.getContent().stream()
                .map(post -> {
                    User user = post.getUser();
                    Category category = post.getCategory();
                    List<String> hashtagList = postHashtagRepository.findHashtagNamesByPostId(post.getId());
                    int likeCnt = postLikeRepository.countByPostId(post.getId());
                    int commentCnt = commentRepository.countByPostId(post.getId());
                    Boolean isLike = postLikeRepository.existsByUserIdAndPostId(myId, post.getId());
                    post.plusViews(1);
                    postRepository.save(post);

                    return new GetPostResponse(
                            post.getId(), user.getId(), user.getNickname(), user.getProfileImage(),
                            post.getPostImage(), post.getPostContent(), post.getPostViews(),
                            category.getCategoryName(), hashtagList, likeCnt, commentCnt, isLike
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", postPage.getTotalPages());
        result.put("currentPage", pageNum);
        result.put("posts", posts);

        return result;
    }


    /**
     * 특정 게시물을 삭제하는 메서드
     *
     * @param postId 삭제할 게시물의 ID
     * @param userId 삭제 요청을 한 사용자의 ID
     */
    @Override
    @Transactional
    public void deletePost(Long userId, String role, Long postId) {

        // 게시물 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "게시물을 찾을수 없음: " + postId ));

        // 요청한 사용자 ID가 게시물 소유자가 아닐 경우 에러 반환
        if (role.equals("ROLE_USER") && !post.getUser().getId().equals(userId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "사진 삭제 권한이 없습니다.");
        }

        // mySQL에서 삭제
        postRepository.delete(post);
        // S3에서 삭제
        if (post.getPostImage() != null) {
            s3Service.deleteImageFile(post.getPostImage(), "post/");
        }
    }


    /**
     * 게시물에 대한 좋아요 토글 메서드
     *
     * @param userId  사용자 ID
     * @param postId 게시물 ID
     * @return 좋아요 상태 (true: 좋아요, false: 좋아요 취소)
     */
    @Override
    @Transactional
    public Boolean toggleLike(Long userId, Long postId) {

        if (postId == null || postId <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 게시물 ID입니다.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 게시물을 찾을 수 없습니다. 게시물 ID: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다. 사용자 ID: " + userId));

        PostLike existPostLike = postLikeRepository.findByUserIdAndPostId(userId, postId)
                .orElse(null);

        if (existPostLike != null) {
            // 기존 좋아요 관계가 있으면 좋아요 취소
            postLikeRepository.delete(existPostLike);
            return false;
        } else {
            PostLike postLike = new PostLike(user, post);
            postLikeRepository.save(postLike);
        }
        return true;
    }


    @Override
    @Transactional
    public void uploadComment(Long userId, Long postId, UploadCommentRequest request) {

        if (request.getCommentContent() == null || request.getCommentContent().trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "댓글 내용을 입력해주세요.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다. 사용자 ID: " + userId));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 게시물을 찾을 수 없습니다. 게시물 ID: " + postId));

        Comment comment;
        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다. 댓글 ID: " + request.getParentCommentId()));
            if (!parentComment.getPost().getId().equals(postId)) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "부모 댓글의 게시물 불일치");
            }

            comment = new Comment(user, post, parentComment, request.getCommentContent());
        } else {
            comment = new Comment(user, post, request.getCommentContent());
        }
        commentRepository.save(comment);
    }


    @Override
    public Map<String, Object> getComment(Long userId, Long postId, int pageNum) {

        if (pageNum < 1) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "페이지 번호는 1 이상의 값이어야 합니다.");
        }

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findByPostIdAndParentCommentIsNull(postId, pageRequest);

        List<GetCommentResponse> comments = commentPage.getContent().stream()
                .map(comment -> {
                    User user = comment.getUser();
                    int likeCnt = commentLikeRepository.countByCommentId(comment.getId());
                    int childCommentCnt = commentRepository.countByParentCommentId(comment.getId());
                    Boolean isLike = commentLikeRepository.existsByUserIdAndCommentId(userId, comment.getId());

                    return new GetCommentResponse(
                            comment.getId(), user.getId(), user.getNickname(), user.getProfileImage(),
                            comment.getCommentContent(), likeCnt, childCommentCnt, isLike
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", commentPage.getTotalPages());
        result.put("currentPage", pageNum);
        result.put("comments", comments);

        return result;
    }


    @Override
    public Map<String, Object> getChildComment(
            Long userId, Long postId, Long ParentCommentId,  int pageNum) {

        if (pageNum < 1) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "페이지 번호는 1 이상의 값이어야 합니다.");
        }

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findByPostIdAndParentCommentId(postId, ParentCommentId, pageRequest);

        List<GetChildCommentResponse> comments = commentPage.getContent().stream()
                .map(comment -> {
                    User user = comment.getUser();
                    int likeCnt = commentLikeRepository.countByCommentId(comment.getId());
                    Boolean isLike = commentLikeRepository.existsByUserIdAndCommentId(userId, comment.getId());

                    return new GetChildCommentResponse(
                            comment.getId(), user.getId(), user.getNickname(), user.getProfileImage(),
                            comment.getCommentContent(), likeCnt, isLike
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", commentPage.getTotalPages());
        result.put("currentPage", pageNum);
        result.put("comments", comments);

        return result;
    }


    /**
     * 특정 댓글을 삭제하는 메서드
     *
     * @param postId 댓글 게시물의 ID
     * @param commentId 삭제할 댓글의 ID
     * @param userId 삭제 요청을 한 사용자의 ID
     */
    @Override
    @Transactional
    public void deleteComment(
            Long userId, String role, Long postId, Long commentId) {

        // 게시물 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "게시물을 찾을수 없음: " + postId ));

        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "댓글을 찾을수 없음: " + commentId ));

        // 해당 댓글이 해당 게시물의 댓글이 아닌 경우 에러 반환
        if (!comment.getPost().equals(post)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "해당 게시글의 댓글이 아닙니다");
        }

        // 요청한 사용자 ID가 게시물 소유자가 아닐 경우 에러 반환
        if (role.equals("ROLE_USER") && !comment.getUser().getId().equals(userId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "댓글 삭제 권한이 없습니다.");
        }

        // mySQL에서 삭제
        commentRepository.delete(comment);
    }


    /**
     * 댓글 좋아요를 토글하는 메서드
     *
     * @param postId     댓글 게시물의 ID
     * @param commentId  좋아요 할 댓글의 ID
     * @return 좋아요 상태 (true: 좋아요, false: 좋아요 취소)
     */
    @Override
    @Transactional
    public Boolean toggleCommentLike(
            Long userId, Long postId, Long commentId) {

        if (postId == null || postId <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 게시물 ID입니다.");
        }

        if (commentId == null || commentId <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 댓글 ID입니다.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 게시물을 찾을 수 없습니다. 게시물 ID: " + postId));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다. 댓글 ID: " + commentId));

        // 해당 댓글이 해당 게시물의 댓글이 아닌 경우 에러 반환
        if (!comment.getPost().equals(post)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "해당 게시글의 댓글이 아닙니다");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다. 사용자 ID: " + userId));

        CommentLike existCommentLike = commentLikeRepository.findByUserIdAndCommentId(userId, commentId)
                .orElse(null);

        if (existCommentLike != null) {
            // 기존 좋아요 관계가 있으면 좋아요 취소
            commentLikeRepository.delete(existCommentLike);
            return false;
        } else {
            CommentLike commentLike = new CommentLike(user, comment);
            commentLikeRepository.save(commentLike);
        }
        return true;
    }


    @Override
    public Map<String, Object> getSubscribePost(Long userId, int pageNum) {

        if (pageNum < 1) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "페이지 번호는 1 이상의 값이어야 합니다.");
        }

        List<Long> subscribedCategoryIds = userCategoryRepository.findCategoryIdsByUserId(userId);

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = postRepository.findByCategoryIdIn(subscribedCategoryIds, pageRequest);

        List<GetPostResponse> posts = postPage.getContent().stream()
                .map(post -> {
                    User user = post.getUser();
                    Category category = post.getCategory();
                    List<String> hashtagList = postHashtagRepository.findHashtagNamesByPostId(post.getId());
                    int likeCnt = postLikeRepository.countByPostId(post.getId());
                    int commentCnt = commentRepository.countByPostId(post.getId());
                    Boolean isLike = postLikeRepository.existsByUserIdAndPostId(userId, post.getId());
                    post.plusViews(1);
                    postRepository.save(post);

                    return new GetPostResponse(
                            post.getId(), user.getId(), user.getNickname(), user.getProfileImage(),
                            post.getPostImage(), post.getPostContent(), post.getPostViews(),
                            category.getCategoryName(), hashtagList, likeCnt, commentCnt, isLike
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", postPage.getTotalPages());
        result.put("currentPage", pageNum);
        result.put("posts", posts);

        return result;
    }


    /**
     * 게시물 검색 기능을 제공하는 메서드
     *
     * @param keyword 검색할 게시물 텍스트
     * @return ResponseEntity<BaseResponse<Map<String, Object>>> 검색된 게시물 목록을 포함한 응답
     */
    @Override
    public Map<String, Object> searchPost(
            Long userId, String keyword, int pageNum) {

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = postRepository.findPostsByHashtagName(keyword, pageRequest);

        List<GetPostResponse> posts = postPage.getContent().stream()
                .map(post -> {
                    User user = post.getUser();
                    Category category = post.getCategory();
                    List<String> hashtagList = postHashtagRepository.findHashtagNamesByPostId(post.getId());
                    int likeCnt = postLikeRepository.countByPostId(post.getId());
                    int commentCnt = commentRepository.countByPostId(post.getId());
                    Boolean isLike = postLikeRepository.existsByUserIdAndPostId(userId, post.getId());
                    post.plusViews(1);
                    postRepository.save(post);

                    return new GetPostResponse(
                            post.getId(), user.getId(), user.getNickname(), user.getProfileImage(),
                            post.getPostImage(), post.getPostContent(), post.getPostViews(),
                            category.getCategoryName(), hashtagList, likeCnt, commentCnt, isLike
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", postPage.getTotalPages());
        result.put("currentPage", pageNum);
        result.put("posts", posts);

        return result;
    }
}
