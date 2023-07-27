package com.hackerrank.gevents.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hackerrank.gevents.repository.EventRepository;
import com.hackerrank.gevents.model.Event;


@Service
public class EventService {
  
  @Autowired
  EventRepository eventRepository;

  public Event saveEntry(Event event){
    return eventRepository.save(event);
  }

  public List<Event> retrieveAllEvent(){
    return eventRepository.findAll();
  }

  public Event retrieve(Integer id){
    Optional<Event> event =  eventRepository.findById(id);

    return event.orElse(null);
  }

  public List<Event> retrieveByUserId(Integer id){
    return eventRepository.findAllByActorId(id);
  }

  public List<Event> retrieveByRepoId(Integer id){
    return eventRepository.findAllByRepoId(id);
  }
}

