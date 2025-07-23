package main.java.service;

import main.java.logger.LoggerFacade;
import main.java.model.Comment;
import main.java.model.Post;
import main.java.repository.CommentRepository;
import main.java.repository.VoteRepository;
import main.java.util.Helper;

import java.util.List;
import java.util.TreeMap;

public class CommentService {
    private static CommentService instance;
    private final VotingService votingService;
    private final CommentRepository commentRepository;

    private CommentService(VotingService votingService) {
        this.votingService = votingService;
        this.commentRepository = new CommentRepository();
    }

    public static CommentService getInstance() {
        if (instance == null) {
            instance = new CommentService(VotingService.getInstance());
        }
        return instance;
    }

    public Comment createComment(String content, String username) {
        LoggerFacade.debug("Creating new comment by user: " + username);

        return new Comment(content, username, votingService.createVote());
    }

    public boolean addReply(Comment comment, String id, String content, String username) {
        TreeMap<Integer, Comment> replies = comment.getReplies();
        if (id.isEmpty()) {
            if (comment.isDeleted()) {
                LoggerFacade.warning("Cannot add reply to a deleted comment by user: " + username);
                return false;
            }

            Comment reply = createComment(content, username);

            Integer newId = comment.getIdNextReply();
            comment.setIdNextReply(newId + 1);

            replies.put(newId, reply);

            LoggerFacade.info("Direct reply added to comment by user: " + username);
            return true;
        }

        int idx = Helper.extractFirstLevel(id);
        if (!replies.containsKey(idx)) {
            LoggerFacade.warning("Failed to add reply with invalid ID: " + id);
            return false;
        }
        LoggerFacade.debug("Adding nested reply to comment path: " + id);

        String remaining_id = Helper.extractRemainingLevels(id);
        return addReply(replies.get(idx), remaining_id, content, username);
    }

    public void deleteComment(Comment comment, String id) {
        if (id.isEmpty()) {
            comment.setIsDeleted(true);

            LoggerFacade.info("Comment marked as deleted");
            return;
        }
        TreeMap<Integer,Comment> replies = comment.getReplies();

        int idx = Helper.extractFirstLevel(id);
        if (!replies.containsKey(idx)) {
            LoggerFacade.warning("Failed to delete comment with invalid ID: " + id);
            return;
        }
        LoggerFacade.debug("Deleting nested comment at path: " + id);

        String remaining_id = Helper.extractRemainingLevels(id);
        deleteComment(replies.get(idx), remaining_id);
    }

    public void addUpvote(Comment comment, String id, String username) {
        if (id.isEmpty()) {
            if (comment.isDeleted()) {
                LoggerFacade.warning("Cannot upvote a deleted comment by user: " + username);
                return;
            }

            // Add upvote in memory
            votingService.addUpvote(comment.getVote(), username);

            // Also save to database if comment has database ID
            Integer commentDatabaseId = comment.getDatabaseId();
            if (commentDatabaseId != null) {
                VoteRepository voteRepository = new VoteRepository();
                voteRepository.addCommentUpvote(commentDatabaseId, username);
                LoggerFacade.info("Comment upvote saved to database for comment ID: " + commentDatabaseId + " by user: " + username);
            }

            LoggerFacade.info("Upvote added to comment by user: " + username);
            return;
        }
        TreeMap<Integer, Comment> replies = comment.getReplies();

        int idx = Helper.extractFirstLevel(id);
        if (!replies.containsKey(idx)) {
            LoggerFacade.warning("Failed to upvote comment with invalid ID: " + id);
            return;
        }
        LoggerFacade.debug("Adding upvote to nested comment at path: " + id);

        String remaining_id = Helper.extractRemainingLevels(id);
        addUpvote(replies.get(idx), remaining_id, username);
    }

    public void addDownvote(Comment comment, String id, String username) {
        if (id.isEmpty()) {
            if (comment.isDeleted()) {
                LoggerFacade.warning("Cannot downvote a deleted comment by user: " + username);
                return;
            }

            // Add downvote in memory
            votingService.addDownvote(comment.getVote(), username);

            // Also save to database if comment has database ID
            Integer commentDatabaseId = comment.getDatabaseId();
            if (commentDatabaseId != null) {
                VoteRepository voteRepository = new VoteRepository();
                voteRepository.addCommentDownvote(commentDatabaseId, username);
                LoggerFacade.info("Comment downvote saved to database for comment ID: " + commentDatabaseId + " by user: " + username);
            }

            LoggerFacade.info("Downvote added to comment by user: " + username);
            return;
        }
        TreeMap<Integer, Comment> replies = comment.getReplies();

        int idx = Helper.extractFirstLevel(id);
        if (!replies.containsKey(idx)) {
            LoggerFacade.warning("Failed to downvote comment with invalid ID: " + id);
            return;
        }
        LoggerFacade.debug("Adding downvote to nested comment at path: " + id);

        String remaining_id = Helper.extractRemainingLevels(id);
        addDownvote(replies.get(idx), remaining_id, username);
    }

    public int getUpvoteCount(Comment comment) {
        // Try to get from database first if comment has database ID
        Integer commentDatabaseId = comment.getDatabaseId();
        if (commentDatabaseId != null) {
            VoteRepository voteRepository = new VoteRepository();
            return voteRepository.getCommentUpvoteCount(commentDatabaseId);
        }
        // Fallback to memory if no database ID
        return votingService.getUpvoteCount(comment.getVote());
    }

    public int getDownvoteCount(Comment comment) {
        // Try to get from database first if comment has database ID
        Integer commentDatabaseId = comment.getDatabaseId();
        if (commentDatabaseId != null) {
            VoteRepository voteRepository = new VoteRepository();
            return voteRepository.getCommentDownvoteCount(commentDatabaseId);
        }
        // Fallback to memory if no database ID
        return votingService.getDownvoteCount(comment.getVote());
    }

    public boolean isEmoji(Comment comment) {
        return votingService.isEmoji(comment.getVote());
    }

    public boolean isCommentDeleted(Comment comment, String id) {
        if (id.isEmpty()) {
            return comment.isDeleted();
        }

        TreeMap<Integer, Comment> replies = comment.getReplies();
        int idx = Helper.extractFirstLevel(id);

        if (!replies.containsKey(idx)) {
            LoggerFacade.warning("Failed to check comment status with invalid ID: " + id);
            return true; // Consider non-existing comments as "deleted" for safety
        }

        String remaining_id = Helper.extractRemainingLevels(id);
        return isCommentDeleted(replies.get(idx), remaining_id);
    }

    // rendering function

    public void renderComment(Comment comment, StringBuilder sb, int depth, String id) {
        sb.append("     ".repeat(depth)).append("-> [").append(id).append("] ");
        sb.append('(').append(comment.getUsername()).append(')');
        if (isEmoji(comment))
            sb.append("🔥");
        sb.append("  ").append(comment.getContent()).append('\n');

        sb.append("   ".repeat(depth));
        sb.append("upvotes = ").append(getUpvoteCount(comment)).append("\n");

        sb.append("   ".repeat(depth));
        sb.append("downvotes = ").append(getDownvoteCount(comment)).append("\n\n\n");

        comment.getReplies().forEach((idReply, reply) ->
                renderComment(reply, sb, depth + 1, id + '.' + idReply));
    }

    public void loadCommentsForPost(Post post, Integer databasePostId, Integer postIndex) {
        LoggerFacade.debug("Loading comments for post ID: " + databasePostId);

        try {
            // Load top-level comments from database
            List<Comment> dbComments = commentRepository.findByPostId(databasePostId);

            // Populate the post's comments TreeMap
            TreeMap<Integer, Comment> comments = post.getComments();
            int commentIndex = 0;

            for (Comment comment : dbComments) {
                comments.put(commentIndex, comment);

                loadDirectRepliesForComment(comment, databasePostId, commentIndex);

                commentIndex++;
            }

            // Update the next comment ID counter
            post.setIdNextComment(commentIndex);

            LoggerFacade.info("Loaded " + dbComments.size() + " comments for post");

        } catch (Exception e) {
            LoggerFacade.warning("Could not load comments for post: " + e.getMessage());
        }
    }

    private void loadDirectRepliesForComment(Comment parentComment, Integer postId, Integer commentIndex) {
        try {
            loadAllRepliesForPost(postId, parentComment);

            LoggerFacade.debug("Loaded replies for comment at index: " + commentIndex);

        } catch (Exception e) {
            LoggerFacade.warning("Could not load replies for comment: " + e.getMessage());
        }
    }

    private void loadAllRepliesForPost(Integer postId, Comment rootComment) {
        try {
            // Get the database ID if it exists
            Integer parentDatabaseId = rootComment.getDatabaseId();

            if (parentDatabaseId != null) {
                // Load direct replies for this comment
                List<Comment> replies = commentRepository.findRepliesByParentId(parentDatabaseId);

                // Add replies to the comment's reply map
                TreeMap<Integer, Comment> replyMap = rootComment.getReplies();
                int replyIndex = 0;

                for (Comment reply : replies) {
                    replyMap.put(replyIndex, reply);

                    // Recursively load replies for this reply
                    loadAllRepliesForPost(postId, reply);

                    replyIndex++;
                }

                // Update the next reply ID counter
                rootComment.setIdNextReply(replyIndex);

                LoggerFacade.debug("Loaded " + replies.size() + " replies for comment");
            }

        } catch (Exception e) {
            LoggerFacade.warning("Error loading replies: " + e.getMessage());
        }
    }


}
