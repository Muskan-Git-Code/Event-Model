package com.hackerrank.gevents.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hackerrank.gevents.model.Event;

import com.hackerrank.gevents.service.EventService;


@RestController
public class EventController {

  @Autowired
  EventService eventService;

  @PostMapping("/events")
  public ResponseEntity<Event> save(@RequestBody Event event){
    Event saved = eventService.saveEntry(event);

    if(!saved.getType().equals("PushEvent") && !saved.getType().equals("ReleaseEvent") && !saved.getType().equals("WatchEvent") ){
      return new ResponseEntity(saved, HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity(saved, HttpStatus.CREATED);
  }

  @GetMapping("/events")
  public ResponseEntity<Event> retrieveAll(){
    List<Event> retrieved = eventService.retrieveAllEvent();
    return new ResponseEntity(retrieved, HttpStatus.OK);
  }

  @GetMapping("/events/{eventId}")
  public ResponseEntity<Event> retrieve(@PathVariable("eventId") Integer id){
    Event retrieved = eventService.retrieve(id);

    if(retrieved == null){
       return new ResponseEntity(retrieved, HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity(retrieved, HttpStatus.OK);
  }

  @GetMapping("/users/{userId}/events")
  public ResponseEntity<Event> retrieveByUserId(@PathVariable("userId") Integer id){
    List<Event> retrieved = eventService.retrieveByUserId(id);
    return new ResponseEntity(retrieved, HttpStatus.OK);
  }

  @GetMapping("/repos/{repoId}/events")
  public ResponseEntity<Event> retrieveByRepoId(@PathVariable("repoId") Integer id){
    List<Event> retrieved = eventService.retrieveByRepoId(id);
    return new ResponseEntity(retrieved, HttpStatus.OK);
  }

}
