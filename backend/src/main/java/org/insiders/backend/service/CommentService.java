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

import java.util.List;
import java.util.UUID;

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
        List<Comment> allComments = commentRepository.findByPostId(postId);
        List<Comment> rootComments = allComments.stream()
                .filter(c -> c.getParentComment() == null && !c.isDeleted())
                .toList();

        UUID currentUserId = (currentUsername != null)
                ? userManagementService.findByUsername(currentUsername).getId()
                : null;

        return rootComments.stream()
                .map(c -> buildTreeDto(c, allComments, currentUserId))
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
            if(parent.isDeleted()){
                throw new NotFoundException("Nu poti raspunde unui comnetariu sters!");
            }
        }

        Comment savedComment = commentRepository.saveAndFlush(new Comment(post, parent, request.content(), user));

        int up = 0, down = 0;
        String userVote = null;
        return commentMapper.toDto(savedComment, up, down, userVote, List.of());
    }

    @Transactional(readOnly = true)
    public CommentResponseDto getCommentWithReplies(UUID commentId, String currentUsername) {
        Comment mainComment = getCommentById(commentId);

        if(mainComment.isDeleted()){
            throw new NotFoundException("Comentariul nu a fost gasit");
        }

        List<Comment> allComments = commentRepository.findByPostId(mainComment.getPost().getId());

        UUID currentUserId = (currentUsername != null)
                ? userManagementService.findByUsername(currentUsername).getId()
                : null;

        return buildTreeDto(mainComment, allComments, currentUserId);
    }

    @Transactional(rollbackFor = Exception.class)
    public CommentResponseDto updateComment(UUID commentId, CommentUpdateRequestDto request, String currentUsername) {
        Comment comment = getCommentById(commentId);

        if(comment.isDeleted()){
            throw new NotFoundException("Comentariul nu a fost gasit");
        }

        comment.setContent(request.content());
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
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public int countCommentsByPostId(UUID postId) {
        return commentRepository.countByPostId(postId);
    }

    private CommentResponseDto buildTreeDto(Comment node, List<Comment> all, UUID currentUserId) {
        if (node.isDeleted()) {
            return null;
        }

        int up = votingService.countUpvotesForComment(node.getId());
        int down = votingService.countDownvotesForComment(node.getId());
        String userVote = null;
        if (currentUserId != null) {
            userVote = votingService.getVoteTypeForUser(currentUserId, null, node.getId());
        }

        List<Comment> children = all.stream()
                 .filter(c -> c.getParentComment() != null
                        && node.getId().equals(c.getParentComment().getId()))
                .toList();

        List<CommentResponseDto> replies = children.stream()
                .map(ch -> buildTreeDto(ch, all, currentUserId))
                .filter(dto -> dto != null)
                .toList();

        return commentMapper.toDto(node, up, down, userVote, replies);
    }
}
