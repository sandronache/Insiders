package main.java.mapper;

import main.java.dto.post.PostResponseDto;
import main.java.entity.Post;
import main.java.model.PostModel;

public class PostMapper {
    public static PostResponseDto postToDto(Post post,int upvotes, int downvotes, int commentCount, String userVote){
        int score = upvotes -  downvotes;

        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getUsername(),
                post.getSubreddit(),
                upvotes,
                downvotes,
                score,
                commentCount,
                userVote,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public static PostResponseDto postModelToDto(PostModel postModel) {
        return new PostResponseDto(
                postModel.getId(),
                postModel.getTitle(),
                postModel.getContent(),
                postModel.getAuthor(),
                postModel.getSubreddit(),
                postModel.getUpvotes(),
                postModel.getDownvotes(),
                postModel.getScore(),
                postModel.getCommentCount(),
                postModel.getUserVote(),
                postModel.getCreatedAt(),
                postModel.getUpdatedAt()
        );
    }
}
