package main.java.service;

import main.java.entity.Comment;

import java.util.UUID;

public interface CommentLookUpService {
    Comment findById(UUID id);
}
