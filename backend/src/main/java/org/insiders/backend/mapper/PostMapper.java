package org.insiders.backend.mapper;

import org.insiders.backend.dto.post.PostResponseDto;
import org.insiders.backend.entity.Post;
import org.insiders.backend.model.PostModel;

public class PostMapper {
    public static PostResponseDto postToDto(Post post, int upvotes, int downvotes, int commentCount, String userVote) {
        int score = upvotes - downvotes;

        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getPhotoPath(),
                post.getUser().getUsername(),
                post.getSubreddit().getName(),
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
                postModel.getPhotoPath(),
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
