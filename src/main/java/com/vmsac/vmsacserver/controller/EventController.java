package com.vmsac.vmsacserver.controller;

import com.vmsac.vmsacserver.model.Event;
import com.vmsac.vmsacserver.service.EventService;
import com.vmsac.vmsacserver.service.InOutEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping("unicon/events")
    public ResponseEntity<?> createEvents(
            @Valid @RequestBody List<Event> ListOfEvents ) {

        if (eventService.createEvents(ListOfEvents)){
            return  new ResponseEntity<>(HttpStatus.OK);
        }
        else{
            return  new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // if all saved successfully, return 200
        // else, save all excepts for errors and return 422
    }

    @GetMapping("events")
    public ResponseEntity<?> getEvents(@RequestParam(value = "queryString", required = false) String queryStr,
                                       @RequestParam(value = "start", required = false)
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                       @RequestParam(value = "end", required = false)
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        return new ResponseEntity<>(eventService.getEventsByTimeDesc(queryStr, start, end, 500), HttpStatus.OK);

    }
}
