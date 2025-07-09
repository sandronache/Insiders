package Vote;

import java.util.HashSet;
import java.util.Set;

public class Vote {
    private final Set<String> upvotes;
    private final Set<String> downvotes;
    private boolean isEmoji;

    public Vote() {
        this.upvotes = new HashSet<>();
        this.downvotes = new HashSet<>();
        this.isEmoji = false;
    }

    private void toggleVote(Set<String> first, Set<String> second, String username) {
        if (first.contains(username)) {
            first.remove(username);
        } else {
            second.remove(username);
            first.add(username);
        }
        checkEmoji();
    }

    public void addUpVote(String username){
        toggleVote(upvotes, downvotes, username);
    }

    public void addDownVote(String username) {
        toggleVote(downvotes, upvotes, username);
    }

    private void checkEmoji() {
        isEmoji = upvotes.size() >= 10;
    }

    public int getUpvoteCount() {
        return upvotes.size();
    }

    public int getDownvoteCount(){
        return downvotes.size();
    }

    public boolean isEmoji(){
        return isEmoji;
    }

}