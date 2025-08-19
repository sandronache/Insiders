package org.insiders.backend.service;

// TODO: Service is using Dto objects but they must never get past Controller layer. Must only use from "model" package

import org.insiders.backend.dto.post.PostUpdateRequestDto;
import org.insiders.backend.dto.vote.VoteResponseDto;
import org.insiders.backend.entity.Post;
import org.insiders.backend.entity.Subreddit;
import org.insiders.backend.entity.User;
import org.insiders.backend.exceptions.InvalidVoteTypeException;
import org.insiders.backend.exceptions.NotFoundException;
import org.insiders.backend.logger.LoggerFacade;
import org.insiders.backend.model.PostModel;
import org.insiders.backend.repository.PostRepository;
import org.insiders.backend.repository.SubredditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Service responsible for post management operations
 */

@Service
public class PostManagementService {
    private final PostRepository postRepository;
    private final CommentService commentService;
    private final VotingService votingService;
    private final UserManagementService userManagementService;
    private final SubredditRepository subredditRepository;

    @Autowired
    public PostManagementService(PostRepository postRepository, CommentService commentService, VotingService votingService, UserManagementService userManagementService,SubredditRepository subredditRepository) {
        this.postRepository = postRepository;
        this.commentService = commentService;
        this.votingService = votingService;
        this.userManagementService = userManagementService;
        this.subredditRepository = subredditRepository;
    }

    private List<Post> getBasePosts(String subreddit) {
        if (subreddit == null || subreddit.isBlank()) {
            return postRepository.findAllByOrderByCreatedAtDesc();
        }
        return postRepository.findBySubreddit_NameOrderByCreatedAtDesc(subreddit);
    }

    private PostModel buildPostModel(PostModel post, String username) {

        int upVotes = votingService.countUpvotesForPost(post.getId());
        int downVotes = votingService.countDownvotesForPost(post.getId());
        int commentCount = commentService.countCommentsByPostId(post.getId());

        User currentUser = userManagementService.findByUsername(username);
        String voteType = votingService.getVoteTypeForUser(currentUser.getId(),post.getId(), null);

        post.setUpvotes(upVotes);
        post.setDownvotes(downVotes);
        post.setScore(upVotes - downVotes);
        post.setCommentCount(commentCount);
        post.setUserVote(voteType);

        return post;
    }

    @Transactional(readOnly = true)
    public List<PostModel> getAllPosts(String subreddit, String username) {
        List<Post> basePosts = getBasePosts(subreddit);

        List<PostModel> finalPosts = new LinkedList<>();

        basePosts.forEach(post -> {
            finalPosts.add(buildPostModel(new PostModel(post), username));
        });

        return finalPosts;
    }

    @Transactional(readOnly = true)
    public PostModel getPostByIdModel(UUID postId, String username) {
        Post basePost = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Postarea cu ID-ul " + postId + " nu a fost gasita"));

        return buildPostModel(new PostModel(basePost), username);
    }

    public Post getPostById(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Postarea cu ID-ul " + postId + " nu a fost gasita"));
    }


    private PostModel buildPostModelDefault(PostModel post) {
        post.setUpvotes(0);
        post.setDownvotes(0);
        post.setScore(0);
        post.setCommentCount(0);
        post.setUserVote(null);

        return post;
    }

    @Transactional
    public PostModel createPost(String title, String content, String author, String subredditName, MultipartFile image, String filterName) {
        User user = userManagementService.findByUsername(author);
        String normalizedSubredditName = subredditName.trim().toLowerCase();
        Subreddit subreddit = subredditRepository.findByNameIgnoreCase(normalizedSubredditName).orElseThrow
                (() -> new NotFoundException("Subreddit " + normalizedSubredditName+" nu a fost gasit"));

        String imagePath = null;

        if (image != null && !image.isEmpty()) {
            String uploadsDir = "/home/ubuntu/images/";

            File dir = new File(uploadsDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new RuntimeException("Could not create upload directory!");
            }

            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
            File file = new File(dir, filename);

            try {
                byte[] finalBytes = image.getBytes();

                if (filterName != null && !filterName.isBlank() && !filterName.equalsIgnoreCase("none")) {
                    finalBytes = filterImage(finalBytes, filename, filterName);
                    filename = filename.replaceAll("\\.(png|jpeg|jpg)$", "") + ".jpg";
                }

                Files.write(Path.of(uploadsDir + filename), finalBytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            imagePath = "http://ec2-3-74-161-90.eu-central-1.compute.amazonaws.com/images/" + filename;
        }

        Post post = postRepository.saveAndFlush(new Post(title, content, user, subreddit, imagePath));

        return buildPostModelDefault(new PostModel(post));
    }

    @Transactional
    public VoteResponseDto votePost(UUID postId, String voteType, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Postarea nu a fost gasita"));

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

    @Transactional
    public PostModel updatePost(UUID id, PostUpdateRequestDto requestDto, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Postarea cu ID-ul " + id + " nu a fost gasita."));

        if (requestDto.title() != null && !requestDto.title().isBlank()) {
            post.setTitle(requestDto.title());
        }

        if (requestDto.content() != null && !requestDto.content().isBlank()) {
            post.setContent(requestDto.content());
        }

        post = postRepository.saveAndFlush(post);
        return buildPostModel(new PostModel(post), username);
    }

    public void deletePostById(UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("Postarea nu a fost gasita");
        }

        postRepository.deleteById(postId);
        LoggerFacade.info("Postarea a fost stearsa din baza de date: " + postId);
    }

    private byte[] filterImage(byte[] imageBytes, String originalFilename, String filterName) {
//        try {
//            // Build the JSON array of operations
//            String operations = "[{ \"Type\": \"" + filterName + "\", \"Parameters\": {} }]";
//            String boundary = "----JavaBoundary-" + UUID.randomUUID();
//
//            // Build multipart/form-data body
//            var sb = new StringBuilder();
//            sb.append("--").append(boundary).append("\r\n");
//            sb.append("Content-Disposition: form-data; name=\"Operations\"\r\n\r\n");
//            sb.append(operations).append("\r\n");
//
//            // File header
//            sb.append("--").append(boundary).append("\r\n");
//            sb.append("Content-Disposition: form-data; name=\"Image\"; filename=\"")
//                    .append(originalFilename).append("\"\r\n");
//            sb.append("Content-Type: image/jpeg\r\n\r\n");
//
//            byte[] headerBytes = sb.toString().getBytes();
//            byte[] endBytes = ("\r\n--" + boundary + "--\r\n").getBytes();
//
//            // concatenate => header + image + ending
//            byte[] body = new byte[headerBytes.length + imageBytes.length + endBytes.length];
//            System.arraycopy(headerBytes, 0, body, 0, headerBytes.length);
//            System.arraycopy(imageBytes, 0, body, headerBytes.length, imageBytes.length);
//            System.arraycopy(endBytes, 0, body, headerBytes.length + imageBytes.length, endBytes.length);
//
//            // Send request to C# EC2
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(new URI("http://16.170.234.239/api/"))
//                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
//                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
//                    .build();
//
//            HttpClient client = HttpClient.newHttpClient();
//            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
//            if (response.statusCode() >= 200 && response.statusCode() < 300) {
//                return response.body(); // Response is binary JPEG
//            } else {
//                throw new RuntimeException("Filtering server returned status: " + response.statusCode());
//            }
//        } catch (IOException | InterruptedException | URISyntaxException e) {
//            throw new RuntimeException("Failed to process image through C# server", e);
//        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            String operationsJson = "[{ \"Type\": \"" + filterName + "\", \"Parameters\": {} }]";

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("Image", new ByteArrayResource(imageBytes) {
                @Override public String getFilename() { return originalFilename; }
            });
            body.add("Operations", operationsJson);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<byte[]> response = restTemplate.postForEntity("http://16.170.234.239/api/", request, byte[].class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody(); // JPEG bytes from C# server
            } else {
                throw new RuntimeException("C# server returned status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send image to C# server", e);
        }
    }
}