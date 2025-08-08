package main.java.service;

import jakarta.transaction.Transactional;
import main.java.dto.subreddit.SubredditCreateRequestDto;
import main.java.dto.subreddit.SubredditResponseDto;
import main.java.dto.subreddit.SubredditUpdateRequestDto;
import main.java.entity.Subreddit;
import main.java.exceptions.BadRequestException;
import main.java.exceptions.NotFoundException;
import main.java.exceptions.ResourceConflictException;
import main.java.mapper.SubredditMapper;
import main.java.repository.PostRepository;
import main.java.repository.SubredditRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SubredditService {
    private final PostRepository postRepository;
    private final SubredditRepository subredditRepository;
    private final SubredditMapper subredditMapper;

    public SubredditService(PostRepository postRepository,SubredditRepository subredditRepository, SubredditMapper subredditMapper) {
        this.postRepository = postRepository;
        this.subredditRepository = subredditRepository;
        this.subredditMapper = subredditMapper;
    }

    @Transactional
    public List<SubredditResponseDto> getAllSubreddits(){
        return subredditRepository.findAllByOrderByCreatedAtDesc().stream().map(subreddit -> {
            int postCount = postRepository.countBySubreddit(subreddit.getName());
            return subredditMapper.toDto(subreddit, postCount);
        }).toList();
    }

    @Transactional
    public SubredditResponseDto getSubredditByName(String name){
        Subreddit subreddit = subredditRepository.findByNameIgnoreCase(name).orElseThrow(()->new NotFoundException("Subreddit-ul nu a fost gasit"));
        int postCount = postRepository.countBySubreddit(subreddit.getName());
        return subredditMapper.toDto(subreddit, postCount);
    }

    @Transactional
    public SubredditResponseDto createSubreddit(SubredditCreateRequestDto request){
        String normalizedName = request.name().trim().toLowerCase();

        if(subredditRepository.existsByNameIgnoreCase(normalizedName)){
            throw new ResourceConflictException("Subreddit-ul "+normalizedName+" deja exista!");
        }

        Subreddit subreddit = new Subreddit(normalizedName, request.displayName(),  request.description(), request.iconUrl());


        Subreddit savedSubreddit = subredditRepository.save(subreddit);
        return subredditMapper.toDto(savedSubreddit,0);

    }

    @Transactional
    public SubredditResponseDto update(String name,SubredditUpdateRequestDto request){
        Subreddit existingSubreddit = subredditRepository.findByNameIgnoreCase(name).orElseThrow(()->new NotFoundException("Subreddit-ul "+name +" nu a fost gasit"));

        if (request.displayName() != null && !request.displayName().isBlank()) {
            existingSubreddit.setDisplayName(request.displayName());
        }
        if (request.description() != null) {
            existingSubreddit.setDescription(request.description());
        }
        if (request.iconUrl() != null) {
            existingSubreddit.setIconUrl(request.iconUrl());
        }

        Subreddit saved = subredditRepository.save(existingSubreddit);
        int postCount = postRepository.countBySubreddit(saved.getName());
        return subredditMapper.toDto(saved, postCount);
    }

    @Transactional
    public void delete(String name){
        Subreddit subreddit = subredditRepository.findByNameIgnoreCase(name).orElseThrow(()->new NotFoundException("Subreddit-ul nu a fost gasit"));
        int postCount = postRepository.countBySubreddit(subreddit.getName());
        if(postCount>0){
            throw new BadRequestException("Nu poti sterge un subreddit ce are deja postari in el");
        }

        subredditRepository.delete(subreddit);
    }
}
