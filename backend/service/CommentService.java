package service;

import model.Comment;
import utils.Helper;

import java.util.LinkedHashMap;
import java.util.Map;

public class CommentService {
    private final VotingService votingService;

    public CommentService(VotingService votingService) {
        this.votingService = votingService;
    }

    public Comment createComment(String content, String username) {
        return new Comment(content, username, votingService.createVote());
    }

    public void addReply(Comment comment, String id, String content, String username) {
        Map<Integer, Comment> replies = comment.getReplies();
        if (id.isEmpty()) {
            Comment reply = createComment(content, username);
            int newId = replies.size();
            replies.put(newId, reply);
            return;
        }

        int idx = Helper.extractFirstLevel(id);
        if (!replies.containsKey(idx)) return;

        String remaining_id = Helper.extractRemainingLevels(id);

        addReply(replies.get(idx), remaining_id, content, username);
    }

    public void deleteComment(Comment comment, String id) {
        if (id.isEmpty()) {
            comment.setIsDeleted(true);
            return;
        }
        Map<Integer,Comment> replies = comment.getReplies();

        int idx = Helper.extractFirstLevel(id);
        if (!replies.containsKey(idx)) return;

        String remaining_id = Helper.extractRemainingLevels(id);

        deleteComment(replies.get(idx), remaining_id);
    }

    public void addUpvote(Comment comment, String id, String username) {
        if (id.isEmpty()) {
            votingService.addUpvote(comment.getVote(), username);
            return;
        }
        Map<Integer, Comment> replies = comment.getReplies();

        int idx = Helper.extractFirstLevel(id);
        if (!replies.containsKey(idx)) return;

        String remaining_id = Helper.extractRemainingLevels(id);

        addUpvote(replies.get(idx), remaining_id, username);
    }

    public void addDownvote(Comment comment, String id, String username) {
        if (id.isEmpty()) {
            votingService.addDownvote(comment.getVote(), username);
            return;
        }
        Map<Integer, Comment> replies = comment.getReplies();

        int idx = Helper.extractFirstLevel(id);
        if (!replies.containsKey(idx)) return;

        String remaining_id = Helper.extractRemainingLevels(id);

        addDownvote(replies.get(idx), remaining_id, username);
    }

    public int getUpvoteCount(Comment comment) {
        return votingService.getUpvoteCount(comment.getVote());
    }

    public int getDownvoteCount(Comment comment) {
        return votingService.getDownvoteCount(comment.getVote());
    }

    public boolean isEmoji(Comment comment) {
        return votingService.isEmoji(comment.getVote());
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

        for (int i = 0; i < comment.getReplies().size(); i++) {
            renderComment(comment.getReplies().get(i), sb, depth + 1, id + '.' + i);
        }
    }
}
