package org.insiders.backend.service;

import org.springframework.transaction.annotation.Transactional;
import org.insiders.backend.dto.comment.CommentCreateRequestDto;
import org.insiders.backend.dto.comment.CommentResponseDto;
import org.insiders.backend.dto.comment.CommentUpdateRequestDto;
import org.insiders.backend.entity.Comment;
import org.insiders.backend.entity.Post;
import org.insiders.backend.entity.User;
import org.insiders.backend.exceptions.NotFoundException;
import org.insiders.backend.mapper.CommentMapper;
import org.insiders.backend.repository.CommentRepository;
import org.insiders.backend.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserManagementService userManagementService;
    private final PostRepository postRepository;
    private final VotingService votingService;

    public CommentService(CommentRepository commentRepository,
                          CommentMapper commentMapper,
                          UserManagementService userManagementService,
                          PostRepository postRepository,
                          VotingService votingService) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.userManagementService = userManagementService;
        this.postRepository = postRepository;
        this.votingService = votingService;
    }

    @Transactional(readOnly = true)
    public Comment getCommentById(UUID id){
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comentariul nu a fost gasit"));
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsForPost(UUID postId, String currentUsername) {
        List<Comment> allComments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId);

        Map<UUID,List<Comment>> childrenByParent = allComments.stream()
                .filter(c -> c.getParentComment()!=null)
                .collect(Collectors.groupingBy(c->c.getParentComment().getId()));

        List<Comment> rootComments = allComments.stream()
                .filter(c -> c.getParentComment() == null)
                .sorted(BY_DATE_DESC)
                .toList();

        UUID currentUserId = (currentUsername != null)
                ? userManagementService.findByUsername(currentUsername).getId()
                : null;

        return rootComments.stream()
                .map(c -> buildTreeDto(c, childrenByParent, currentUserId))
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public CommentResponseDto createComment(UUID postId, CommentCreateRequestDto request) {
        User user = userManagementService.findByUsername(request.author());
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Postarea nu a fost gasita"));

        Comment parent = null;
        if (request.parentId() != null) {
            parent = getCommentById(request.parentId());
        }

        Comment savedComment = commentRepository.save(new Comment(post, parent, request.content(), user));

        int up = 0, down = 0;
        String userVote = null;
        return commentMapper.toDto(savedComment, up, down, userVote, List.of());
    }

    @Transactional(readOnly = true)
    public CommentResponseDto getCommentWithReplies(UUID commentId, String currentUsername) {
        Comment mainComment = getCommentById(commentId);

        List<Comment> allComments = commentRepository.findByPostIdOrderByCreatedAtDesc(mainComment.getPost().getId());

        Map<UUID, List<Comment>> childrenByParent = allComments.stream()
                .filter(c -> c.getParentComment() != null)
                .collect(Collectors.groupingBy(c -> c.getParentComment().getId()));

        UUID currentUserId = (currentUsername != null)
                ? userManagementService.findByUsername(currentUsername).getId()
                : null;

        return buildTreeDto(mainComment, childrenByParent, currentUserId);
    }

    @Transactional(rollbackFor = Exception.class)
    public CommentResponseDto updateComment(UUID commentId, CommentUpdateRequestDto request, String currentUsername) {
        Comment comment = getCommentById(commentId);

        comment.setContent(request.content());
        comment.setEdited(true);
        Comment updatedComment = commentRepository.save(comment);

        int up = votingService.countUpvotesForComment(updatedComment.getId());
        int down = votingService.countDownvotesForComment(updatedComment.getId());
        String userVote = null;

        if (currentUsername != null) {
            UUID userId = userManagementService.findByUsername(currentUsername).getId();
            userVote = votingService.getVoteTypeForUser(userId, null, updatedComment.getId());
        }

        return commentMapper.toDto(updatedComment, up, down, userVote, List.of());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(UUID commentId) {
        Comment comment = getCommentById(commentId);
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public int countCommentsByPostId(UUID postId) {
        return commentRepository.countByPostId(postId);
    }

    private CommentResponseDto buildTreeDto(Comment node, Map<UUID,List<Comment>> childrenByParent, UUID currentUserId) {
        int up = votingService.countUpvotesForComment(node.getId());
        int down = votingService.countDownvotesForComment(node.getId());
        String userVote = null;
        if (currentUserId != null) {
            userVote = votingService.getVoteTypeForUser(currentUserId, null, node.getId());
        }

        List<Comment> children = childrenByParent
                .getOrDefault(node.getId(), List.of())
                .stream()
                .sorted(BY_DATE_DESC)
                .toList();

        List<CommentResponseDto> replies = children.stream()
                .map(ch -> buildTreeDto(ch, childrenByParent, currentUserId))
                .toList();

        return commentMapper.toDto(node, up, down, userVote, replies);
    }

    private static final Comparator<Comment> BY_DATE_DESC =
            Comparator.comparing(Comment::getCreatedAt).reversed()
                    .thenComparing(Comment::getId);
}
