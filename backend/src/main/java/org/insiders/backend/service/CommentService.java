package org.insiders.backend.service;

import org.insiders.backend.dto.comment.CommentCreateRequestDto;
import org.insiders.backend.dto.comment.CommentResponseDto;
import org.insiders.backend.dto.comment.CommentUpdateRequestDto;
import org.insiders.backend.entity.Comment;
import org.insiders.backend.entity.Post;
import org.insiders.backend.entity.User;
import org.insiders.backend.exceptions.NotFoundException;
import org.insiders.backend.logger.AsyncLogManager;
import org.insiders.backend.mapper.CommentMapper;
import org.insiders.backend.repository.CommentRepository;
import org.insiders.backend.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final AsyncLogManager logger = AsyncLogManager.getInstance();

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
        logger.log("INFO", "Fetching comment with ID: " + id);
        try {
            Comment comment = commentRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.log("WARN", "Comment not found with ID: " + id);
                        return new NotFoundException("Comentariul nu a fost gasit");
                    });
            logger.log("INFO", "Retrieved comment with ID: " + id);
            return comment;
        } catch (Exception e) {
            if (!(e instanceof NotFoundException)) {
                logger.log("ERROR", "Error retrieving comment with ID " + id + ": " + e.getMessage());
            }
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsForPost(UUID postId, String currentUsername) {
        logger.log("INFO", "Fetching comments for post with ID: " + postId + ", current user: " + currentUsername);
        try {
            List<Comment> allComments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
            logger.log("INFO", "Found " + allComments.size() + " comments for post ID: " + postId);

            Map<UUID,List<Comment>> childrenByParent = allComments.stream()
                    .filter(c -> c.getParentComment()!=null)
                    .collect(Collectors.groupingBy(c->c.getParentComment().getId()));

            List<Comment> rootComments = allComments.stream()
                    .filter(c -> c.getParentComment() == null)
                    .sorted(BY_DATE_DESC)
                    .toList();
            logger.log("INFO", "Filtered " + rootComments.size() + " root comments for post ID: " + postId);

            // Fix: Make currentUserId effectively final by initializing it with its final value
            final UUID currentUserId = currentUsername != null
                    ? userManagementService.findByUsername(currentUsername).getId()
                    : null;

            if (currentUserId != null) {
                logger.log("INFO", "Current user ID resolved: " + currentUserId);
            }

            List<CommentResponseDto> result = rootComments.stream()
                    .map(c -> buildTreeDto(c, childrenByParent, currentUserId))
                    .toList();
            logger.log("INFO", "Successfully built comment tree with " + result.size() + " root comments");
            return result;
        } catch (Exception e) {
            logger.log("ERROR", "Error fetching comments for post " + postId + ": " + e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public CommentResponseDto createComment(UUID postId, CommentCreateRequestDto request) {
        logger.log("INFO", "Creating comment for post ID: " + postId + " by user: " + request.author());
        try {
            User user = userManagementService.findByUsername(request.author());
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> {
                        logger.log("WARN", "Post not found with ID: " + postId);
                        return new NotFoundException("Postarea nu a fost gasita");
                    });

            Comment parent = null;
            if (request.parentId() != null) {
                logger.log("INFO", "Comment has parent ID: " + request.parentId());
                parent = getCommentById(request.parentId());
            }

            Comment savedComment = commentRepository.save(new Comment(post, parent, request.content(), user));
            logger.log("INFO", "Comment created successfully with ID: " + savedComment.getId());

            int up = 0, down = 0;
            String userVote = null;
            return commentMapper.toDto(savedComment, up, down, userVote, List.of());
        } catch (Exception e) {
            logger.log("ERROR", "Error creating comment for post " + postId + ": " + e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public CommentResponseDto getCommentWithReplies(UUID commentId, String currentUsername) {
        logger.log("INFO", "Fetching comment tree for comment ID: " + commentId);
        try {
            Comment mainComment = getCommentById(commentId);

            List<Comment> allComments = commentRepository.findByPostIdOrderByCreatedAtDesc(mainComment.getPost().getId());
            logger.log("INFO", "Found " + allComments.size() + " comments in post for building comment tree");

            Map<UUID, List<Comment>> childrenByParent = allComments.stream()
                    .filter(c -> c.getParentComment() != null)
                    .collect(Collectors.groupingBy(c -> c.getParentComment().getId()));

            UUID currentUserId = null;
            if (currentUsername != null) {
                currentUserId = userManagementService.findByUsername(currentUsername).getId();
                logger.log("INFO", "Current user ID resolved: " + currentUserId);
            }

            CommentResponseDto result = buildTreeDto(mainComment, childrenByParent, currentUserId);
            logger.log("INFO", "Successfully built comment tree for comment ID: " + commentId);
            return result;
        } catch (Exception e) {
            logger.log("ERROR", "Error fetching comment tree for " + commentId + ": " + e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public CommentResponseDto updateComment(UUID commentId, CommentUpdateRequestDto request, String currentUsername) {
        logger.log("INFO", "Updating comment with ID: " + commentId + ", user: " + currentUsername);
        try {
            Comment comment = getCommentById(commentId);

            comment.setContent(request.content());
            comment.setEdited(true);
            Comment updatedComment = commentRepository.save(comment);
            logger.log("INFO", "Comment updated successfully, ID: " + updatedComment.getId());

            int up = votingService.countUpvotesForComment(updatedComment.getId());
            int down = votingService.countDownvotesForComment(updatedComment.getId());
            String userVote = null;

            if (currentUsername != null) {
                UUID userId = userManagementService.findByUsername(currentUsername).getId();
                userVote = votingService.getVoteTypeForUser(userId, null, updatedComment.getId());
            }

            return commentMapper.toDto(updatedComment, up, down, userVote, List.of());
        } catch (Exception e) {
            logger.log("ERROR", "Error updating comment " + commentId + ": " + e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(UUID commentId) {
        logger.log("INFO", "Deleting comment with ID: " + commentId);
        try {
            Comment comment = getCommentById(commentId);
            commentRepository.delete(comment);
            logger.log("INFO", "Comment deleted successfully, ID: " + commentId);
        } catch (Exception e) {
            logger.log("ERROR", "Error deleting comment " + commentId + ": " + e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public int countCommentsByPostId(UUID postId) {
        logger.log("INFO", "Counting comments for post ID: " + postId);
        try {
            int count = commentRepository.countByPostId(postId);
            logger.log("INFO", "Post ID: " + postId + " has " + count + " comments");
            return count;
        } catch (Exception e) {
            logger.log("ERROR", "Error counting comments for post " + postId + ": " + e.getMessage());
            throw e;
        }
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