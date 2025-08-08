package main.java.service;

import main.java.dto.subreddit.SubredditCreateRequestDto;
import main.java.dto.subreddit.SubredditResponseDto;
import main.java.dto.subreddit.SubredditUpdateRequestDto;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SubredditService {

    public List<SubredditResponseDto> getAllSubreddits(){
        return List.of();
    }

    public SubredditResponseDto getSubredditByName(String name){
        return null;
    }

    public SubredditResponseDto createSubreddit(SubredditCreateRequestDto request){
        return null;
    }

    public SubredditResponseDto update(SubredditUpdateRequestDto request){
        return null;
    }

    public void delete(String name){

    }
}
