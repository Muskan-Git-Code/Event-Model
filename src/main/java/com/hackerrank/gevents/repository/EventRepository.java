package com.hackerrank.gevents.repository;

import com.hackerrank.gevents.model.Event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
   List<Event> findAllByActorId(Integer actorId);
   List<Event> findAllByRepoId(Integer repoId);
}

