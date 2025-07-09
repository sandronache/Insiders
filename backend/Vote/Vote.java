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

    public void addUpVote(String username) {
        if(upvotes.contains(username))
            upvotes.remove(username);
        else {
                /* in cazul in care user-ul nu a dat nici un vot, atunci downvotes.remove nu va sterge nimic ca nu exista.
                asta pentru a evita cod redundant cu 3 if-uri */
            downvotes.remove(username);
            upvotes.add(username);
        }
        checkEmoji();
    }

    public void addDownVote(String username){
        if(downvotes.contains(username))
            downvotes.remove(username);
        else {
            // aceeasi logica dar mirrored.
            upvotes.remove(username);
            downvotes.add(username);
        }
        checkEmoji();
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