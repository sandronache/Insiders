package main.java.service;

import main.java.dto.post.PostResponseDto;
import main.java.dto.post.PostUpdateRequestDto;
import main.java.dto.post.VoteResponseDto;
import main.java.exceptions.InvalidVoteTypeException;
import main.java.exceptions.PostNotFoundException;
import main.java.logger.LoggerFacade;
import main.java.mapper.PostMapper;
import main.java.model.Post;
import main.java.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Service responsible for post management operations
 */

@Service
public class PostManagementService {
    private final PostRepository postRepository;
    private final ContentService contentService;
    private final DatabaseMappingService mappingService;
    private final CommentService commentService;

    @Autowired
    public PostManagementService(PostRepository postRepository, ContentService contentService, DatabaseMappingService mappingService, CommentService commentService) {
        this.postRepository = postRepository;
        this.contentService = contentService;
        this.mappingService = mappingService;
        this.commentService = commentService;
    }

    public TreeMap<UUID, Post> loadPostsFromDatabase() {
        TreeMap<UUID, Post> posts = new TreeMap<>();

        try {
            // Use the new JPA method to get all posts ordered by creation date
            List<Post> allPosts = postRepository.findAllByOrderByCreatedAtDesc();

            for (Post post : allPosts) {
                posts.put(post.getId(), post);

                // Store mapping using the post's UUID (optional, if needed)
                mappingService.storePostMapping(post.getId(), post.getId());

                // Load comments for this post
                commentService.loadCommentsForPost(post, post.getId());

                // Load votes for this post
                contentService.loadVotesForPost(post, post.getId());

                // Load votes for all comments in this post
                post.getComments().forEach((commentId, comment) ->
                        commentService.loadVotesForComment(comment)
                );
            }

            LoggerFacade.info("Loaded " + allPosts.size() + " posts from database");
        } catch (Exception e) {
            LoggerFacade.warning("Could not load posts from database: " + e.getMessage());
            LoggerFacade.info("Starting with empty posts list");
        }

        return posts;
    }


    public List<Post> getAllPosts(String subreddit) {
        if (subreddit != null && !subreddit.trim().isEmpty()) {
            return postRepository.findBySubredditOrderByCreatedAtDesc(subreddit.trim());
        }
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public Post createPost(String title, String content, String author, String subreddit) {
        Post post = new Post(title, content, author, subreddit);
        return postRepository.save(post);
    }

    public PostResponseDto updatePost(UUID id, PostUpdateRequestDto requestDto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Postarea cu ID-ul " + id + " nu a fost gasita."));

        if (requestDto.title() != null && !requestDto.title().isBlank()) {
            post.setTitle(requestDto.title());
        }

        if (requestDto.content() != null && !requestDto.content().isBlank()) {
            post.setContent(requestDto.content());
        }

        postRepository.save(post);
        return PostMapper.postToDto(post);
    }


    public void deletePostById(UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("Postarea nu a fost gasita");
        }

        postRepository.deleteById(postId);
        LoggerFacade.info("Postarea a fost stearsa din baza de date: " + postId);
    }

    public Post getPostById(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Postarea cu ID-ul " + postId + " nu a fost gasita"));
    }

    public VoteResponseDto votePost(UUID postId, String voteType) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Postarea nu a fost gasita"));

        switch(voteType.toLowerCase()){
            case "up" ->{
                post.setUpvotes(post.getUpvotes() + 1);
                post.setCurrentUserVote("up");
            }
            case "down" ->{
                post.setDownvotes(post.getDownvotes()+1);
                post.setCurrentUserVote("down");
            }
            case "none" ->{
                String currentVote = post.getCurrentUserVote();
                if("up".equals(currentVote))
                    post.setUpvotes(post.getUpvotes()-1);
                if("down".equals(currentVote))
                    post.setDownvotes(post.getDownvotes()-1);
                post.setCurrentUserVote(null);
            }
            default -> throw new InvalidVoteTypeException("Tip de date invalid: "+voteType);
        }
        postRepository.save(post);

        return new VoteResponseDto(
                post.getUpvotes(),
                post.getDownvotes(),
                post.getScore(),
                post.getCurrentUserVote()
        );
    }


}
