package service;

import model.Vote;

import java.util.Set;

public class VotingService {
    public VotingService() {}

    public Vote createVote() {
        return new Vote();
    }

    public int getUpvoteCount(Vote vote) {
        return vote.getUpvote().size();
    }
    public int getDownvoteCount(Vote vote) {
        return vote.getDownvote().size();
    }

    public void checkEmoji(Vote vote) {
        int upvotesSize = getUpvoteCount(vote);
        int downvotesSize = getDownvoteCount(vote);
        vote.setEmoji(upvotesSize - downvotesSize >= 10);
    }

    private void toggleVote(Vote vote, Set<String> first, Set<String> second, String username) {
        if (first.contains(username)) {
            first.remove(username);
        } else {
            second.remove(username);
            first.add(username);
        }
        checkEmoji(vote);
    }

    public void addUpvote(Vote vote, String username) {
        toggleVote(vote, vote.getUpvote(), vote.getDownvote(), username);
    }

    public void addDownvote(Vote vote, String username) {
        toggleVote(vote, vote.getDownvote(), vote.getUpvote(), username);
    }

    public boolean isEmoji(Vote vote) {
        return vote.isEmoji();
    }


//    public void upvotePost(Post post, String username) {
//        if (post == null) return;
//        Vote votes = getVoteFromObject(post, "votes");
//        if (votes != null) {
//            votes.addUpVote(username);
//        }
//    }
//
//    public void downvotePost(Post post, String username) {
//        if (post == null) return;
//        Vote votes = getVoteFromObject(post, "votes");
//        if (votes != null) {
//            votes.addDownVote(username);
//        }
//    }
//
//    public void upvoteComment(Comment comment, String username) {
//        if (comment == null) return;
//        Vote votes = getVoteFromObject(comment, "votes");
//        if (votes != null) {
//            votes.addUpVote(username);
//        }
//    }
//
//    public void downvoteComment(Comment comment, String username) {
//        if (comment == null) return;
//        Vote votes = getVoteFromObject(comment, "votes");
//        if (votes != null) {
//            votes.addDownVote(username);
//        }
//    }
//
//    public void upvoteCommentByPath(Post post, String commentPath, String username) {
//        if (post == null || commentPath == null || commentPath.isEmpty()) return;
//        Comment comment = findComment(post, commentPath);
//        if (comment != null) {
//            upvoteComment(comment, username);
//        }
//    }
//
//    public void downvoteCommentByPath(Post post, String commentPath, String username) {
//        if (post == null || commentPath == null || commentPath.isEmpty()) return;
//        Comment comment = findComment(post, commentPath);
//        if (comment != null) {
//            downvoteComment(comment, username);
//        }
//    }
//
//    public int getPostUpvoteCount(Post post) {
//        if (post == null) return 0;
//        Vote votes = getVoteFromObject(post, "votes");
//        return votes != null ? votes.getUpvoteCount() : 0;
//    }
//
//    public int getPostDownvoteCount(Post post) {
//        if (post == null) return 0;
//        Vote votes = getVoteFromObject(post, "votes");
//        return votes != null ? votes.getDownvoteCount() : 0;
//    }
//
//    public int getCommentUpvoteCount(Comment comment) {
//        if (comment == null) return 0;
//        Vote votes = getVoteFromObject(comment, "votes");
//        return votes != null ? votes.getUpvoteCount() : 0;
//    }
//
//    public int getCommentDownvoteCount(Comment comment) {
//        if (comment == null) return 0;
//        Vote votes = getVoteFromObject(comment, "votes");
//        return votes != null ? votes.getDownvoteCount() : 0;
//    }
//
//    public boolean isPostEmoji(Post post) {
//        if (post == null) return false;
//        Vote votes = getVoteFromObject(post, "votes");
//        return votes != null && votes.isEmoji();
//    }
//
//    public boolean isCommentEmoji(Comment comment) {
//        if (comment == null) return false;
//        Vote votes = getVoteFromObject(comment, "votes");
//        return votes != null && votes.isEmoji();
//    }
//
//    public Comment findComment(Post post, String commentPath) {
//        if (post == null || commentPath == null || commentPath.isEmpty()) {
//            return null;
//        }
//
//        try {
//            int idx = utils.Helper.extractFirstLevel(commentPath);
//            String remainingPath = utils.Helper.extractRemainingLevels(commentPath);
//
//            // Get top-level comments from the post
//            java.util.ArrayList<Comment> comments = post.getComments();
//
//            if (idx < 0 || idx >= comments.size()) {
//                return null;
//            }
//
//            Comment targetComment = comments.get(idx);
//
//            // If there are no more levels to navigate, return the current comment
//            if (remainingPath.isEmpty()) {
//                return targetComment;
//            }
//
//            // Otherwise, recursively navigate to the nested comment
//            return findNestedComment(targetComment, remainingPath);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    private Comment findNestedComment(Comment comment, String path) {
//        if (comment == null || path == null || path.isEmpty()) {
//            return comment;
//        }
//
//        try {
//            int idx = utils.Helper.extractFirstLevel(path);
//            String remainingPath = utils.Helper.extractRemainingLevels(path);
//
//            java.util.ArrayList<Comment> replies = comment.getReplies();
//
//            if (idx < 0 || idx >= replies.size()) {
//                return null;
//            }
//
//            Comment targetComment = replies.get(idx);
//
//            if (remainingPath.isEmpty()) {
//                return targetComment;
//            }
//
//            return findNestedComment(targetComment, remainingPath);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    private Vote getVoteFromObject(Object object, String fieldName) {
//        if (object == null) return null;
//
//        try {
//            java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
//            field.setAccessible(true);
//            return (Vote) field.get(object);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
}
