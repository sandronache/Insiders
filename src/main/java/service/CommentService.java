package main.java.service;

import main.java.dto.comment.CommentCreateRequestDto;
import main.java.dto.comment.CommentResponseDto;
import main.java.dto.comment.CommentUpdateRequestDto;
import main.java.dto.comment.VoteResponseDto;
import main.java.entity.Comment;
import main.java.entity.Post;
import main.java.entity.User;
import main.java.entity.Vote;
import main.java.exceptions.InvalidVoteTypeException;
import main.java.exceptions.NotFoundException;
import main.java.logger.LoggerFacade;
import main.java.mapper.CommentMapper;
import main.java.repository.CommentRepository;
import main.java.repository.VoteRepository;
import main.java.util.Helper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class CommentService {
    private final VotingService votingService;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PostManagementService postManagementService;
    private final UserManagementService userManagementService;

    public  CommentService(VotingService votingService, CommentRepository commentRepository, CommentMapper commentMapper, PostManagementService postManagementService, UserManagementService userManagementService) {
        this.votingService = votingService;
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.postManagementService = postManagementService;
        this.userManagementService = userManagementService;
    }

    //de pastrat la refactor
    public List<CommentResponseDto> getCommentsForPost(UUID postId, String currentUsername) {
        List<Comment> allComments = commentRepository.findByPostId(postId);

        List<Comment> rootComments = allComments.stream()
                .filter(c -> c.getParentComment() == null)
                .toList();

        return rootComments.stream()
                .map(c -> commentMapper.toDto(c, allComments, currentUsername))
                .toList();
    }


    //de pastrat la refactor
    public CommentResponseDto createComment(UUID postId, CommentCreateRequestDto request){
        Post post = postManagementService.getPostById(postId);
        User user =userManagementService.findByUsername(request.author());

        Comment parent = null;
        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new NotFoundException("Comentariul parinte nu a fost gasit"));
        }

        Comment comment = new Comment(post,parent, request.content(),user);
        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toDto(savedComment,List.of(), request.author());
    }


    //de pastrat la refactor
    public CommentResponseDto getCommentWithReplies(UUID commentId,String currentUsername) {
        Comment mainComment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comentariul nu a fost gasit"));
        List<Comment> allComments = commentRepository.findByPostId(mainComment.getPost().getId());

        return commentMapper.toDto(mainComment,allComments,currentUsername);
    }


    //de pastrat la refactor
    public CommentResponseDto updateComment(UUID commentId, CommentUpdateRequestDto request,String currentUsername) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comentariul nu a fost gasit"));

        comment.setContent(request.content());
        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toDto(updatedComment,List.of(),currentUsername);
    }

    //de pastrat la refactor
    public void deleteComment(UUID commentId){
        Comment comment = commentRepository.findById(commentId).orElseThrow(()-> new NotFoundException("Comentariul nu a fost gasit"));
        comment.setDeleted(true);
        comment.setContent("[comentariu sters]");
        commentRepository.save(comment);
    }


    //de pastrat la refactor
    public VoteResponseDto voteComment(UUID commentId, String voteType, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comentariul nu a fost gasit"));

        User user = userManagementService.findByUsername(username);

        switch (voteType.toLowerCase()) {
            case "up" -> votingService.createVote(user.getId(), null, comment.getId(), true);
            case "down" -> votingService.createVote(user.getId(), null, comment.getId(), false);
            case "none" -> votingService.deleteVoteForComment(comment, user);
            default -> throw new InvalidVoteTypeException("Tipul de vot este invalid: " + voteType);
        }

        int upvotes = votingService.countUpvotesForComment(comment.getId());
        int downvotes = votingService.countDownvotesForComment(comment.getId());
        int score = upvotes - downvotes;
        String userVote = votingService.getVoteTypeForUser(user.getId(),null,commentId);

        return new VoteResponseDto(upvotes, downvotes, score, userVote);
    }

    //de pastrat la refactor
    public Comment findById(UUID commentId){
        return commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comentariul nu a fost gasit"));
    }

    public int countCommentsByPostId(UUID postId) {
        return commentRepository.countByPostId(postId);
    }



//    public Comment createComment(String content, String username) {
//        LoggerFacade.debug("Creating new comment by user: " + username);
//
//        return new Comment(content, username, votingService.createVote());
//    }
//
//    public boolean addReply(Comment comment, String id, String content, String username) {
//        TreeMap<Integer, Comment> replies = comment.getReplies();
//        if (id.isEmpty()) {
//            if (comment.isDeleted()) {
//                LoggerFacade.warning("Cannot add reply to a deleted comment by user: " + username);
//                return false;
//            }
//
//            Comment reply = createComment(content, username);
//
//            Integer newId = comment.getIdNextReply();
//            comment.setIdNextReply(newId + 1);
//
//            replies.put(newId, reply);
//
//            LoggerFacade.info("Direct reply added to comment by user: " + username);
//            return true;
//        }
//
//        int idx = Helper.extractFirstLevel(id);
//        if (!replies.containsKey(idx)) {
//            LoggerFacade.warning("Failed to add reply with invalid ID: " + id);
//            return false;
//        }
//        LoggerFacade.debug("Adding nested reply to comment path: " + id);
//
//        String remaining_id = Helper.extractRemainingLevels(id);
//        return addReply(replies.get(idx), remaining_id, content, username);
//    }
//
//    public void deleteComment(Comment comment, String id) {
//        if (id.isEmpty()) {
//            comment.setIsDeleted(true);
//
//            LoggerFacade.info("Comment marked as deleted");
//            return;
//        }
//        TreeMap<Integer,Comment> replies = comment.getReplies();
//
//        int idx = Helper.extractFirstLevel(id);
//        if (!replies.containsKey(idx)) {
//            LoggerFacade.warning("Failed to delete comment with invalid ID: " + id);
//            return;
//        }
//        LoggerFacade.debug("Deleting nested comment at path: " + id);
//
//        String remaining_id = Helper.extractRemainingLevels(id);
//        deleteComment(replies.get(idx), remaining_id);
//    }
//
//    public void addUpvote(Comment comment, String id, String username) {
//        if (id.isEmpty()) {
//            if (comment.isDeleted()) {
//                LoggerFacade.warning("Cannot upvote a deleted comment by user: " + username);
//                return;
//            }
//
//            // Add upvote in memory
//            votingService.addUpvote(comment.getVote(), username);
//
//            // Also save to database if comment has database ID
//            Integer commentDatabaseId = comment.getDatabaseId();
//            if (commentDatabaseId != null) {
//                VoteRepository voteRepository = new VoteRepository();
//                voteRepository.addCommentUpvote(commentDatabaseId, username);
//
//                // Check and save emoji status to database
//                votingService.checkEmojiForComment(comment.getVote(), commentDatabaseId);
//
//                LoggerFacade.info("Comment upvote saved to database for comment ID: " + commentDatabaseId + " by user: " + username);
//            }
//
//            LoggerFacade.info("Upvote added to comment by user: " + username);
//            return;
//        }
//        TreeMap<Integer, Comment> replies = comment.getReplies();
//
//        int idx = Helper.extractFirstLevel(id);
//        if (!replies.containsKey(idx)) {
//            LoggerFacade.warning("Failed to upvote comment with invalid ID: " + id);
//            return;
//        }
//        LoggerFacade.debug("Adding upvote to nested comment at path: " + id);
//
//        String remaining_id = Helper.extractRemainingLevels(id);
//        addUpvote(replies.get(idx), remaining_id, username);
//    }
//
//    public void addDownvote(Comment comment, String id, String username) {
//        if (id.isEmpty()) {
//            if (comment.isDeleted()) {
//                LoggerFacade.warning("Cannot downvote a deleted comment by user: " + username);
//                return;
//            }
//
//            // Add downvote in memory
//            votingService.addDownvote(comment.getVote(), username);
//
//            // Also save to database if comment has database ID
//            Integer commentDatabaseId = comment.getDatabaseId();
//            if (commentDatabaseId != null) {
//                VoteRepository voteRepository = new VoteRepository();
//                voteRepository.addCommentDownvote(commentDatabaseId, username);
//
//                // Check and save emoji status to database
//                votingService.checkEmojiForComment(comment.getVote(), commentDatabaseId);
//
//                LoggerFacade.info("Comment downvote saved to database for comment ID: " + commentDatabaseId + " by user: " + username);
//            }
//
//            LoggerFacade.info("Downvote added to comment by user: " + username);
//            return;
//        }
//        TreeMap<Integer, Comment> replies = comment.getReplies();
//
//        int idx = Helper.extractFirstLevel(id);
//        if (!replies.containsKey(idx)) {
//            LoggerFacade.warning("Failed to downvote comment with invalid ID: " + id);
//            return;
//        }
//        LoggerFacade.debug("Adding downvote to nested comment at path: " + id);
//
//        String remaining_id = Helper.extractRemainingLevels(id);
//        addDownvote(replies.get(idx), remaining_id, username);
//    }
//
//    public int getUpvoteCount(Comment comment) {
//        // Use memory-based voting for better performance
//        // Votes are loaded and synchronized when comments are loaded from database
//        return votingService.getUpvoteCount(comment.getVote());
//    }
//
//    public int getDownvoteCount(Comment comment) {
//        // Use memory-based voting for better performance
//        // Votes are loaded and synchronized when comments are loaded from database
//        return votingService.getDownvoteCount(comment.getVote());
//    }
//
//    public boolean isEmoji(Comment comment) {
//        return votingService.isEmoji(comment.getVote());
//    }
//
//    public boolean isCommentDeleted(Comment comment, String id) {
//        if (id.isEmpty()) {
//            return comment.isDeleted();
//        }
//
//        TreeMap<Integer, Comment> replies = comment.getReplies();
//        int idx = Helper.extractFirstLevel(id);
//
//        if (!replies.containsKey(idx)) {
//            LoggerFacade.warning("Failed to check comment status with invalid ID: " + id);
//            return true; // Consider non-existing comments as "deleted" for safety
//        }
//
//        String remaining_id = Helper.extractRemainingLevels(id);
//        return isCommentDeleted(replies.get(idx), remaining_id);
//    }
//
//    // rendering function
//
//    public void renderComment(Comment comment, StringBuilder sb, int depth, String id) {
//        sb.append("     ".repeat(depth)).append("-> [").append(id).append("] ");
//        sb.append('(').append(comment.getUsername()).append(')');
//        if (isEmoji(comment))
//            sb.append("🔥");
//        sb.append("  ").append(comment.getContent()).append('\n');
//
//        sb.append("   ".repeat(depth));
//        sb.append("upvotes = ").append(getUpvoteCount(comment)).append("\n");
//
//        sb.append("   ".repeat(depth));
//        sb.append("downvotes = ").append(getDownvoteCount(comment)).append("\n\n\n");
//
//        comment.getReplies().forEach((idReply, reply) ->
//                renderComment(reply, sb, depth + 1, id + '.' + idReply));
//    }
//
//    public void loadCommentsForPost(Post post, UUID databasePostId) {
//        LoggerFacade.debug("Loading comments for post ID: " + databasePostId);
//
//        try {
//            // Load top-level comments from database
//            List<Comment> dbComments = commentRepository.findByPostId(databasePostId);
//
//            // Populate the post's comments TreeMap
//            TreeMap<Integer, Comment> comments = post.getComments();
//            int commentIndex = 0;
//
//            for (Comment comment : dbComments) {
//                comments.put(commentIndex, comment);
//
//                loadDirectRepliesForComment(comment, comment.getDatabaseId());
//
//                commentIndex++;
//            }
//
//            // Update the next comment ID counter
//            post.setIdNextComment(commentIndex);
//
//            LoggerFacade.info("Loaded " + dbComments.size() + " comments for post");
//
//        } catch (Exception e) {
//            LoggerFacade.warning("Could not load comments for post: " + e.getMessage());
//        }
//    }
//
//    private void loadDirectRepliesForComment(Comment parentComment, Integer commentDatabaseId) {
//        try {
//            loadAllRepliesForPost(parentComment);
//
//            LoggerFacade.debug("Loaded replies for comment at index: " + commentDatabaseId);
//
//        } catch (Exception e) {
//            LoggerFacade.warning("Could not load replies for comment: " + e.getMessage());
//        }
//    }
//
//    private void loadAllRepliesForPost(Comment rootComment) {
//        try {
//            // Get the database ID if it exists
//            Integer parentDatabaseId = rootComment.getDatabaseId();
//
//            if (parentDatabaseId != null) {
//                // Load direct replies for this comment
//                List<Comment> replies = commentRepository.findRepliesByParentId(parentDatabaseId);
//
//                // Add replies to the comment's reply map
//                TreeMap<Integer, Comment> replyMap = rootComment.getReplies();
//                int replyIndex = 0;
//
//                for (Comment reply : replies) {
//                    replyMap.put(replyIndex, reply);
//
//                    // Recursively load replies for this reply
//                    loadAllRepliesForPost(reply);
//
//                    replyIndex++;
//                }
//
//                // Update the next reply ID counter
//                rootComment.setIdNextReply(replyIndex);
//
//                LoggerFacade.debug("Loaded " + replies.size() + " replies for comment");
//            }
//
//        } catch (Exception e) {
//            LoggerFacade.warning("Error loading replies: " + e.getMessage());
//        }
//    }
//
//    // Load votes from database into memory objects for comments
//    public void loadVotesForComment(Comment comment) {
//        if (comment.getDatabaseId() == null) {
//            return; // Skip if no database ID
//        }
//
//        try {
//            VoteRepository voteRepository = new VoteRepository();
//
//            // Get upvotes and downvotes from database
//            java.util.List<String> upvotes = voteRepository.getCommentUpvotes(comment.getDatabaseId());
//            java.util.List<String> downvotes = voteRepository.getCommentDownvotes(comment.getDatabaseId());
//
//            // Load votes into the comment's Vote object in memory
//            Vote vote = comment.getVote();
//            for (String username : upvotes) {
//                vote.getUpvote().add(username);
//            }
//            for (String username : downvotes) {
//                vote.getDownvote().add(username);
//            }
//
//            // Load emoji status from database
//            VotingService votingService = VotingService.getInstance();
//            votingService.loadEmojiFromDatabase(vote, null, comment.getDatabaseId());
//
//            LoggerFacade.debug("Loaded " + upvotes.size() + " upvotes and " + downvotes.size() + " downvotes for comment");
//
//            // Recursively load votes for all replies
//            comment.getReplies().forEach((id, reply) -> loadVotesForComment(reply));
//
//        } catch (Exception e) {
//            LoggerFacade.warning("Error loading votes for comment: " + e.getMessage());
//        }
//    }


}
