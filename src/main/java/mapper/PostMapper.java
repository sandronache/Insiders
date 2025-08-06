package main.java.mapper;

import main.java.dto.post.PostResponseDto;
import main.java.entity.Post;

public class PostMapper {
    public static PostResponseDto postToDto(Post post){
        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getUsername(),
                post.getSubreddit(),
                post.getUpvotes(),
                post.getDownvotes(),
                post.getScore(),
                post.getCommentCount(),
                post.getCurrentUserVote(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
