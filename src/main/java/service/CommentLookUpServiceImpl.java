package main.java.service;

import main.java.entity.Comment;
import main.java.exceptions.NotFoundException;
import main.java.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CommentLookUpServiceImpl implements CommentLookUpService {

    private final CommentRepository commentRepository;

    public CommentLookUpServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public Comment findById(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comentariul nu a fost gasit"));
    }
}

