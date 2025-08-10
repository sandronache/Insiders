package org.insiders.backend.service;

import org.springframework.transaction.annotation.Transactional;
import org.insiders.backend.dto.subreddit.SubredditCreateRequestDto;
import org.insiders.backend.dto.subreddit.SubredditResponseDto;
import org.insiders.backend.dto.subreddit.SubredditUpdateRequestDto;
import org.insiders.backend.entity.Subreddit;
import org.insiders.backend.exceptions.BadRequestException;
import org.insiders.backend.exceptions.NotFoundException;
import org.insiders.backend.exceptions.ResourceConflictException;
import org.insiders.backend.mapper.SubredditMapper;
import org.insiders.backend.repository.PostRepository;
import org.insiders.backend.repository.SubredditRepository;
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

    @Transactional(readOnly = true)
    public List<SubredditResponseDto> getAllSubreddits(){
        return subredditRepository.findAllByOrderByCreatedAtDesc().stream().map(subreddit -> {
            int postCount = postRepository.countBySubreddit_Name(subreddit.getName());
            return subredditMapper.toDto(subreddit, postCount);
        }).toList();
    }

    @Transactional(readOnly = true)
    public SubredditResponseDto getSubredditByName(String name){
        Subreddit subreddit = subredditRepository.findByNameIgnoreCase(name).orElseThrow(()->new NotFoundException("Subreddit-ul nu a fost gasit"));
        int postCount = postRepository.countBySubreddit_Name(subreddit.getName());
        return subredditMapper.toDto(subreddit, postCount);
    }

    @Transactional(rollbackFor = Exception.class)
    public SubredditResponseDto createSubreddit(SubredditCreateRequestDto request){
        String normalizedName = request.name().trim().toLowerCase();

        if(subredditRepository.existsByNameIgnoreCase(normalizedName)){
            throw new ResourceConflictException("Subreddit-ul "+normalizedName+" deja exista!");
        }

        Subreddit subreddit = new Subreddit(normalizedName, request.displayName(),  request.description(), request.iconUrl());


        Subreddit savedSubreddit = subredditRepository.save(subreddit);
        return subredditMapper.toDto(savedSubreddit,0);

    }

    @Transactional(rollbackFor = Exception.class)
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
        int postCount = postRepository.countBySubreddit_Name(saved.getName());
        return subredditMapper.toDto(saved, postCount);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String name){
        Subreddit subreddit = subredditRepository.findByNameIgnoreCase(name).orElseThrow(()->new NotFoundException("Subreddit-ul nu a fost gasit"));
        int postCount = postRepository.countBySubreddit_Name(subreddit.getName());
        if(postCount>0){
            throw new BadRequestException("Nu poti sterge un subreddit ce are deja postari in el");
        }

        subredditRepository.delete(subreddit);
    }
}
