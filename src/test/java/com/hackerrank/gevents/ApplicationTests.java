package com.hackerrank.gevents;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackerrank.gevents.model.Event;
import com.hackerrank.gevents.repository.EventRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class ApplicationTests {
    ObjectMapper om = new ObjectMapper();
    @Autowired
    EventRepository eventRepository;
    @Autowired
    MockMvc mockMvc;

    Map<String, Event> testData;

    @Before
    public void setup() {
        eventRepository.deleteAll();
        testData = getTestData();
    }

    @Test
    public void testEventCreation() throws Exception {
        Event expectedRecord = testData.get("event_01_push_actor_1");
        Event actualRecord = om.readValue(mockMvc.perform(post("/events")
                .contentType("application/json")
                .content(om.writeValueAsString(expectedRecord)))
                .andDo(print())
                .andExpect(jsonPath("$.id", greaterThan(0)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Event.class);

        Assert.assertTrue(new ReflectionEquals(expectedRecord, "id").matches(actualRecord));
        assertEquals(true, eventRepository.findById(actualRecord.getId()).isPresent());

        expectedRecord = testData.get("event_02_watch_actor_1");
        actualRecord = om.readValue(mockMvc.perform(post("/events")
                .contentType("application/json")
                .content(om.writeValueAsString(expectedRecord)))
                .andDo(print())
                .andExpect(jsonPath("$.id", greaterThan(0)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Event.class);

        Assert.assertTrue(new ReflectionEquals(expectedRecord, "id").matches(actualRecord));
        assertEquals(true, eventRepository.findById(actualRecord.getId()).isPresent());

        //test invalid
        expectedRecord = testData.get("event_01_push_actor_1");
        expectedRecord.setType("pushevent");
        mockMvc.perform(post("/events")
                .contentType("application/json")
                .content(om.writeValueAsString(expectedRecord)))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    public void testGetAllEvents() throws Exception {
        Map<String, Event> testData = getTestData().entrySet().stream().filter(kv -> "event_01_push_actor_1, event_01_release_actor_1, event_01_watch_actor_1".contains(kv.getKey())).collect(Collectors.toMap(kv -> kv.getKey(), kv -> kv.getValue()));

        List<Event> expectedRecords = new ArrayList<>();
        for (Map.Entry<String, Event> kv : testData.entrySet()) {
            expectedRecords.add(om.readValue(mockMvc.perform(post("/events")
                    .contentType("application/json")
                    .content(om.writeValueAsString(kv.getValue())))
                    .andDo(print())
                    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Event.class));
        }
        Collections.sort(expectedRecords, Comparator.comparing(Event::getId));

        List<Event> actualRecords = om.readValue(mockMvc.perform(get("/events"))
                .andDo(print())
                .andExpect(jsonPath("$.*", isA(ArrayList.class)))
                .andExpect(jsonPath("$.*", hasSize(expectedRecords.size())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), new TypeReference<List<Event>>() {
        });

        for (int i = 0; i < expectedRecords.size(); i++) {
            Assert.assertTrue(new ReflectionEquals(expectedRecords.get(i)).matches(actualRecords.get(i)));
        }
    }

    @Test
    public void testGetEventWithId() throws Exception {
        Event expectedRecord = getTestData().get("event_01_push_actor_1");

        expectedRecord = om.readValue(mockMvc.perform(post("/events")
                .contentType("application/json")
                .content(om.writeValueAsString(expectedRecord)))
                .andDo(print())
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Event.class);

        Event actualRecord = om.readValue(mockMvc.perform(get("/events/" + expectedRecord.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), Event.class);

        Assert.assertTrue(new ReflectionEquals(expectedRecord).matches(actualRecord));

        //non existing record test
        mockMvc.perform(get("/events/" + Integer.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetEventByRepos() throws Exception {
        Map<String, Event> eventsWithRepoId1 = getTestData().entrySet().stream().filter(kv -> "event_01_push_actor_1, event_01_release_actor_1, event_01_watch_actor_1".contains(kv.getKey())).collect(Collectors.toMap(kv -> kv.getKey(), kv -> kv.getValue()));
        Map<String, Event> eventsWithRepoId2 = getTestData().entrySet().stream().filter(kv -> "event_02_watch_actor_1".contains(kv.getKey())).collect(Collectors.toMap(kv -> kv.getKey(), kv -> kv.getValue()));

        //1
        List<Event> expectedEventsWithRepoId1 = new ArrayList<>();
        for (Event event : eventsWithRepoId1.values()) {
            expectedEventsWithRepoId1.add(om.readValue(mockMvc.perform(post("/events")
                    .contentType("application/json")
                    .content(om.writeValueAsString(event)))
                    .andDo(print())
                    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Event.class));
        }
        Collections.sort(expectedEventsWithRepoId1, Comparator.comparing(Event::getId));

        //2
        List<Event> expectedEventsWithRepoId2 = new ArrayList<>();
        for (Event event : eventsWithRepoId2.values()) {
            expectedEventsWithRepoId2.add(om.readValue(mockMvc.perform(post("/events")
                    .contentType("application/json")
                    .content(om.writeValueAsString(event)))
                    .andDo(print())
                    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Event.class));
        }
        Collections.sort(expectedEventsWithRepoId2, Comparator.comparing(Event::getId));

        //get 1
        List<Event> actualEventsWithRepoId1 = om.readValue(mockMvc.perform(get("/repos/1/events"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), new TypeReference<List<Event>>() {
        });

        //get 2
        List<Event> actualEventsWithRepoId2 = om.readValue(mockMvc.perform(get("/repos/2/events"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), new TypeReference<List<Event>>() {
        });

        Assert.assertTrue(new ReflectionEquals(expectedEventsWithRepoId1).matches(actualEventsWithRepoId1));
        Assert.assertTrue(new ReflectionEquals(expectedEventsWithRepoId2).matches(actualEventsWithRepoId2));

        //non existing record test
        mockMvc.perform(get("/repos/" + Integer.MAX_VALUE + "/events")
                .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetEventByUser() throws Exception {
        Map<String, Event> eventsWithUserId1 = getTestData().entrySet().stream().filter(kv -> "event_01_push_actor_1, event_01_release_actor_1, event_02_watch_actor_1".contains(kv.getKey())).collect(Collectors.toMap(kv -> kv.getKey(), kv -> kv.getValue()));
        Map<String, Event> eventsWithUserId2 = getTestData().entrySet().stream().filter(kv -> "event_02_watch_actor_2".contains(kv.getKey())).collect(Collectors.toMap(kv -> kv.getKey(), kv -> kv.getValue()));

        //1
        List<Event> expectedEventsWithUserId1 = new ArrayList<>();
        for (Event event : eventsWithUserId1.values()) {
            expectedEventsWithUserId1.add(om.readValue(mockMvc.perform(post("/events")
                    .contentType("application/json")
                    .content(om.writeValueAsString(event)))
                    .andDo(print())
                    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Event.class));
        }
        Collections.sort(expectedEventsWithUserId1, Comparator.comparing(Event::getId));

        //2
        List<Event> expectedEventsWithUserId2 = new ArrayList<>();
        for (Event event : eventsWithUserId2.values()) {
            expectedEventsWithUserId2.add(om.readValue(mockMvc.perform(post("/events")
                    .contentType("application/json")
                    .content(om.writeValueAsString(event)))
                    .andDo(print())
                    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Event.class));
        }
        Collections.sort(expectedEventsWithUserId2, Comparator.comparing(Event::getId));

        //get 1
        List<Event> actualEventsWithUserId1 = om.readValue(mockMvc.perform(get("/users/1/events"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), new TypeReference<List<Event>>() {
        });

        //get 2
        List<Event> actualEventsWithUserId2 = om.readValue(mockMvc.perform(get("/users/2/events"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), new TypeReference<List<Event>>() {
        });

        Assert.assertTrue(new ReflectionEquals(expectedEventsWithUserId1).matches(actualEventsWithUserId1));
        Assert.assertTrue(new ReflectionEquals(expectedEventsWithUserId2).matches(actualEventsWithUserId2));

        //non existing record test
        mockMvc.perform(get("/users/" + Integer.MAX_VALUE + "/events")
                .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    private Map<String, Event> getTestData() {
        Map<String, Event> data = new HashMap<>();

        Event event_01_push_actor_1 = new Event(
                "PushEvent",
                true,
                1,
                1);
        data.put("event_01_push_actor_1", event_01_push_actor_1);

        Event event_01_release_actor_1 = new Event(
                "ReleaseEvent",
                true,
                1,
                1);
        data.put("event_01_release_actor_1", event_01_release_actor_1);

        Event event_01_watch_actor_1 = new Event(
                "WatchEvent",
                true,
                1,
                1);
        data.put("event_01_watch_actor_1", event_01_watch_actor_1);

        Event event_02_watch_actor_1 = new Event(
                "WatchEvent",
                true,
                2,
                1);
        data.put("event_02_watch_actor_1", event_02_watch_actor_1);

        Event event_02_watch_actor_2 = new Event(
                "WatchEvent",
                true,
                2,
                2);
        data.put("event_02_watch_actor_2", event_02_watch_actor_2);


        return data;
    }
}
