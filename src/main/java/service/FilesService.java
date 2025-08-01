package main.java.service;

import main.java.logger.LoggerFacade;
import main.java.entity.Comment;
import main.java.entity.Post;
import main.java.entity.User;
import main.java.entity.Vote;
import main.java.util.Constants;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.TreeMap;

@Service
public class FilesService {

    private static FilesService instance;

    private FilesService() {}

    public static FilesService getInstance() {
        if (instance == null) {
            instance = new FilesService();
        }
        return instance;
    }

    // posts

    private Vote loadVote(BufferedReader reader) throws Exception {
        Vote vote = new Vote();

        int nrUpvotes = Integer.parseInt(reader.readLine());
        for (int i = 0; i < nrUpvotes; i++) {
            String username = reader.readLine();
            vote.getUpvote().add(username);
        }

        int nrDownvotes = Integer.parseInt(reader.readLine());
        for (int i = 0; i < nrDownvotes; i++) {
            String username = reader.readLine();
            vote.getDownvote().add(username);
        }

        int isEmoji = Integer.parseInt(reader.readLine());
        vote.setEmoji(isEmoji != 0);

        return vote;
    }

    private Comment loadComment(BufferedReader reader) throws Exception {
        String content = reader.readLine();
        String username = reader.readLine();
        Vote vote = loadVote(reader);

        Comment comment = new Comment(content, username, vote);

        int isDeleted = Integer.parseInt(reader.readLine());
        comment.setIsDeleted(isDeleted != 0);

        int nrReplies = Integer.parseInt(reader.readLine());

        for (int i = 0; i < nrReplies; i++) {
            Integer idReply = Integer.parseInt(reader.readLine());
            Comment reply = loadComment(reader);

            comment.getReplies().put(idReply, reply);
        }

        if (!comment.getReplies().isEmpty()) {
            comment.setIdNextReply(comment.getReplies().lastKey() + 1);
        } else {
            comment.setIdNextReply(0);
        }
        return comment;
    }

    public TreeMap<Integer, Post> loadPosts() {
        TreeMap<Integer, Post> posts = new TreeMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(Constants.CONTENT_FILE_PATH))) {
            String line = reader.readLine();
            if (line == null || line.isBlank()) {
                LoggerFacade.info("File is empty, start from scratch");
                return posts;
            }

            int nrPosts = Integer.parseInt(line);

            for (int i = 0; i < nrPosts; i++) {

                Integer idPost = Integer.parseInt(reader.readLine());

                String content = reader.readLine();
                String username = reader.readLine();
                Vote vote = loadVote(reader);

                Post post = new Post(content, username, vote);

                int nrComments = Integer.parseInt(reader.readLine());

                for (int j = 0; j < nrComments; j++) {
                    Integer idComment = Integer.parseInt(reader.readLine());
                    Comment comment = loadComment(reader);

                    post.getComments().put(idComment, comment);
                }

                if (!post.getComments().isEmpty()) {
                    post.setIdNextComment(post.getComments().lastKey() + 1);
                } else {
                    post.setIdNextComment(0);
                }

                posts.put(idPost, post);
            }

        } catch (Exception e) {
            LoggerFacade.fatal("File reading error: " + e.getMessage());
        }
        return posts;
    }

    private void writeVote(PrintWriter pw, Vote vote) {
        pw.println(vote.getUpvote().size());
        vote.getUpvote().forEach(pw::println);

        pw.println(vote.getDownvote().size());
        vote.getDownvote().forEach(pw::println);

        if (vote.isEmoji()) pw.println(1);
        else pw.println(0);
    }

    private void writeComment(PrintWriter pw, Comment comment) {
        pw.println(comment.getContent());
        pw.println(comment.getUsername());
        writeVote(pw, comment.getVote());

        if (comment.isDeleted()) pw.println(1);
        else pw.println(0);

        pw.println(comment.getReplies().size());

        comment.getReplies().forEach((idReply, reply) -> {
            pw.println(idReply);
            writeComment(pw, reply);
        });
    }

    public void writePosts(TreeMap<Integer, Post> posts) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(Constants.CONTENT_FILE_PATH))) {
            pw.println(posts.size());

            posts.forEach((idPost, post) -> {
                pw.println(idPost);
                pw.println(post.getContent());
                pw.println(post.getUsername());
                writeVote(pw, post.getVote());

                pw.println(post.getComments().size());

                post.getComments().forEach((idComment, comment) -> {
                    pw.println(idComment);
                    writeComment(pw, comment);
                });
            });

            if (pw.checkError()) {
                LoggerFacade.fatal("File writing error");
            }
        } catch (Exception e) {
            LoggerFacade.fatal("File writing error: " + e.getMessage());
        }
    }

    // users

    public HashMap<String, User> loadUsers() {
        HashMap<String, User> users = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(Constants.USERS_FILE_PATH))) {
            String line = reader.readLine();
            if (line == null || line.isBlank()) {
                LoggerFacade.info("File is empty, start from scratch");
                return users;
            }

            int nrUsers = Integer.parseInt(line);

            for (int i = 0; i < nrUsers; i++) {
                String username = reader.readLine();
                String email = reader.readLine();
                int hash = Integer.parseInt(reader.readLine());

                users.put(username, new User(username, email, hash));
            }

        } catch (Exception e) {
            LoggerFacade.fatal("File reading error: " + e.getMessage());
        }
        return users;
    }

    public void writeUsers(HashMap<String, User> users) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(Constants.USERS_FILE_PATH))) {
            pw.println(users.size());

            users.forEach((username, user) -> {
                pw.println(user.getUsername());
                pw.println(user.getEmail());
                pw.println(user.getHashedPassword());
            });

            if (pw.checkError()) {
                LoggerFacade.fatal("File writing error");
            }
        } catch (Exception e) {
            LoggerFacade.fatal("File writing error: " + e.getMessage());
        }
    }
}
