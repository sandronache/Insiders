package main.java.service;

import main.java.dto.comment.CommentCreateRequestDto;
import main.java.dto.comment.CommentResponseDto;
import main.java.dto.post.PostResponseDto;
import main.java.dto.post.PostUpdateRequestDto;
import main.java.dto.vote.VoteResponseDto;
import main.java.entity.Comment;
import main.java.entity.User;
import main.java.exceptions.InvalidVoteTypeException;
import main.java.exceptions.NotFoundException;
import main.java.exceptions.PostNotFoundException;
import main.java.entity.Post;
import main.java.logger.LoggerFacade;
import main.java.model.PostModel;
import main.java.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for post management operations
 */

@Service
public class PostManagementService {
    private final PostRepository postRepository;
    private final CommentService commentService;
    private final VotingService votingService;
    private final UserManagementService userManagementService;

    @Autowired
    public PostManagementService(PostRepository postRepository, CommentService commentService, VotingService votingService, UserManagementService userManagementService) {
        this.postRepository = postRepository;
        this.commentService = commentService;
        this.votingService = votingService;
        this.userManagementService = userManagementService;
    }

    private List<Post> getBasePosts(String subreddit) {
        if (subreddit == null || subreddit.isBlank()) {
            return postRepository.findAllByOrderByCreatedAtDesc();
        }
        return postRepository.findBySubredditOrderByCreatedAtDesc(subreddit);
    }

    private PostModel buildFinalPost(Post post) {
        PostModel postModel = new PostModel(post);

        int upVotes = votingService.countUpvotesForPost(post.getId());
        int downVotes = votingService.countDownvotesForPost(post.getId());
        int commentCount = commentService.countCommentsByPostId(post.getId());

        postModel.setUpvotes(upVotes);
        postModel.setDownvotes(downVotes);
        postModel.setScore(upVotes - downVotes);
        postModel.setCommentCount(commentCount);
        postModel.setUserVote(null);

        return postModel;
    }

    public List<PostModel> getAllPosts(String subreddit) {
        List<Post> basePosts = getBasePosts(subreddit);

        List<PostModel> finalPosts = new LinkedList<>();

        basePosts.forEach(post -> {
            finalPosts.add(buildFinalPost(post));
        });

        return finalPosts;
    }

    public PostModel getPostByIdModel(UUID postId) {
        Post basePost = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Postarea cu ID-ul " + postId + " nu a fost gasita"));

        return buildFinalPost(basePost);
    }

    public Post getPostById(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Postarea cu ID-ul " + postId + " nu a fost gasita"));
    }

    private PostModel buildFinalPostForNew(Post post) {
        PostModel postModel = new PostModel(post);

        postModel.setUpvotes(1);
        postModel.setDownvotes(0);
        postModel.setScore(1);
        postModel.setCommentCount(0);
        postModel.setUserVote("up");

        return postModel;
    }

    public PostModel createPost(String title, String content, String author, String subreddit) {
        User user = userManagementService.findByUsername(author);
        Post post = new Post(title, content, user, subreddit);
        postRepository.save(post);

        votePost(post.getId(), "up", author);

        return buildFinalPostForNew(post);
    }

    public VoteResponseDto votePost(UUID postId, String voteType, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Postarea nu a fost gasita"));

        User user = userManagementService.findByUsername(username);

        switch (voteType.toLowerCase()) {
            case "up" -> votingService.createVote(user.getId(), post.getId(), null, true);
            case "down" -> votingService.createVote(user.getId(), post.getId(), null, false);
            case "none" -> votingService.deleteVoteForPost(post, user);
            default -> throw new InvalidVoteTypeException("Tip de vot invalid: " + voteType);
        }

        int upvotes = votingService.countUpvotesForPost(post.getId());
        int downvotes = votingService.countDownvotesForPost(post.getId());
        int score = upvotes - downvotes;
        String userVote = votingService.getVoteTypeForUser(user.getId(), postId, null);

        return new VoteResponseDto(upvotes, downvotes, score, userVote);
    }

    public PostModel updatePost(UUID id, PostUpdateRequestDto requestDto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Postarea cu ID-ul " + id + " nu a fost gasita."));

        if (requestDto.title() != null && !requestDto.title().isBlank()) {
            post.setTitle(requestDto.title());
        }

        if (requestDto.content() != null && !requestDto.content().isBlank()) {
            post.setContent(requestDto.content());
        }

        postRepository.save(post);
        return buildFinalPost(post);
    }

    public void deletePostById(UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("Postarea nu a fost gasita");
        }

        postRepository.deleteById(postId);
        LoggerFacade.info("Postarea a fost stearsa din baza de date: " + postId);
    }

    public CommentResponseDto createComment(UUID postId, CommentCreateRequestDto request) {
        Post post = getPostById(postId);

        return commentService.createComment(post, request);
    }

}

//    public TreeMap<UUID, Post> loadPostsFromDatabase() {
//        TreeMap<UUID, Post> posts = new TreeMap<>();
//
//        try {
//            // Use the new JPA method to get all posts ordered by creation date
//            List<Post> allPosts = postRepository.findAllByOrderByCreatedAtDesc();
//
//            for (Post post : allPosts) {
//                posts.put(post.getId(), post);
//
//                // Store mapping using the post's UUID (optional, if needed)
//                mappingService.storePostMapping(post.getId(), post.getId());
//
//                // Load comments for this post
//                commentService.loadCommentsForPost(post, post.getId());
//
//                // Load votes for this post
//                contentService.loadVotesForPost(post, post.getId());
//
//                // Load votes for all comments in this post
//                post.getComments().forEach((commentId, comment) ->
//                        commentService.loadVotesForComment(comment)
//                );
//            }
//
//            LoggerFacade.info("Loaded " + allPosts.size() + " posts from database");
//        } catch (Exception e) {
//            LoggerFacade.warning("Could not load posts from database: " + e.getMessage());
//            LoggerFacade.info("Starting with empty posts list");
//        }
//
//        return posts;
//    }
//
//
//    public List<Post> getAllPosts(String subreddit) {
//        if (subreddit != null && !subreddit.trim().isEmpty()) {
//            return postRepository.findBySubredditOrderByCreatedAtDesc(subreddit.trim());
//        }
//        return postRepository.findAllByOrderByCreatedAtDesc();
//    }
//
//    public Post createPost(String title, String content, String author, String subreddit) {
//        User user = userManagementService.findByUsername(author);
//        Post post = new Post(title, content, user, subreddit);
//        return postRepository.save(post);
//    }
//
//    public PostResponseDto updatePost(UUID id, PostUpdateRequestDto requestDto) {
//        Post post = postRepository.findById(id)
//                .orElseThrow(() -> new PostNotFoundException("Postarea cu ID-ul " + id + " nu a fost gasita."));
//
//        if (requestDto.title() != null && !requestDto.title().isBlank()) {
//            post.setTitle(requestDto.title());
//        }
//
//        if (requestDto.content() != null && !requestDto.content().isBlank()) {
//            post.setContent(requestDto.content());
//        }
//
//        postRepository.save(post);
//        return PostMapper.postToDto(post);
//    }
//
//
//    public void deletePostById(UUID postId) {
//        if (!postRepository.existsById(postId)) {
//            throw new PostNotFoundException("Postarea nu a fost gasita");
//        }
//
//        postRepository.deleteById(postId);
//        LoggerFacade.info("Postarea a fost stearsa din baza de date: " + postId);
//    }
//
//    public Post getPostById(UUID postId) {
//        return postRepository.findById(postId)
//                .orElseThrow(() -> new PostNotFoundException("Postarea cu ID-ul " + postId + " nu a fost gasita"));
//    }
//
//    public VoteResponseDto votePost(UUID postId, String voteType, String username) {
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new PostNotFoundException("Postarea nu a fost gasita"));
//
//        User user = userManagementService.findByUsername(username);
//
//        switch (voteType.toLowerCase()) {
//            case "up" -> votingService.createVote(user.getId(), post.getId(), null, true);
//            case "down" -> votingService.createVote(user.getId(), post.getId(), null, false);
//            case "none" -> votingService.deleteVoteForPost(post, user);
//            default -> throw new InvalidVoteTypeException("Tip de vot invalid: " + voteType);
//        }
//
//        int upvotes = votingService.countUpvotesForPost(post.getId());
//        int downvotes = votingService.countDownvotesForPost(post.getId());
//        int score = upvotes - downvotes;
//        String userVote = votingService.getVoteTypeForUser(user.getId(),postId,null);
//
//        return new VoteResponseDto(upvotes, downvotes, score, userVote);
//    }
