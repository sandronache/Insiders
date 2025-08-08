package main.java.service;

import org.springframework.transaction.annotation.Transactional;
import main.java.dto.comment.CommentCreateRequestDto;
import main.java.dto.comment.CommentResponseDto;
import main.java.dto.comment.CommentUpdateRequestDto;
import main.java.entity.Comment;
import main.java.entity.Post;
import main.java.entity.User;
import main.java.exceptions.NotFoundException;
import main.java.mapper.CommentMapper;
import main.java.repository.CommentRepository;
import main.java.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserManagementService userManagementService;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, CommentMapper commentMapper, UserManagementService userManagementService, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.userManagementService = userManagementService;
        this.postRepository = postRepository;
    }


    @Transactional(readOnly = true)
    public Comment getCommentById(UUID id){
        return commentRepository.findById(id).orElseThrow(() -> new NotFoundException("Comentariul nu a fost gasit"));
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsForPost(UUID postId, String currentUsername) {
        List<Comment> allComments = commentRepository.findByPostId(postId);

        List<Comment> rootComments = allComments.stream()
                .filter(c -> c.getParentComment() == null)
                .toList();

        return rootComments.stream()
                .map(c -> commentMapper.toDto(c, allComments, currentUsername))
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

        Comment comment = new Comment(post, parent, request.content(), user);
        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toDto(savedComment, List.of(), request.author());
    }


    @Transactional(readOnly = true)
    public CommentResponseDto getCommentWithReplies(UUID commentId, String currentUsername) {
        Comment mainComment = getCommentById(commentId);
        List<Comment> allComments = commentRepository.findByPostId(mainComment.getPost().getId());

        return commentMapper.toDto(mainComment, allComments, currentUsername);
    }

    @Transactional(rollbackFor = Exception.class)
    public CommentResponseDto updateComment(UUID commentId, CommentUpdateRequestDto request, String currentUsername) {
        Comment comment = getCommentById(commentId);

        comment.setContent(request.content());
        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toDto(updatedComment, List.of(), currentUsername);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(UUID commentId) {
        Comment comment = getCommentById(commentId);
        comment.setDeleted(true);
        comment.setContent("[comentariu sters]");
        commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public int countCommentsByPostId(UUID postId) {
        return commentRepository.countByPostId(postId);
    }
}
