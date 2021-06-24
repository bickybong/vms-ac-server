package com.vmsac.vmsacserver.controller;

import com.vmsac.vmsacserver.model.ScheduledVisit;
import com.vmsac.vmsacserver.model.Visitor;
import com.vmsac.vmsacserver.repository.VisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
public class VisitorController {

    @Autowired
    private VisitorRepository visitorRepository;

    @GetMapping("/visitors")
    List<Visitor> getVisitors(){
        return visitorRepository.findAll();
    }

    @GetMapping(path = "/qr-code/{lastfourdigit}")
    private ResponseEntity<List> getScheduledVisitByOther(
            @PathVariable("lastfourdigit") String lastFourDigitOfId){

        List<Visitor> scheduledVisits = visitorRepository.findByLastFourDigitsOfId(lastFourDigitOfId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(scheduledVisits);
    }

    @PostMapping(path = "/register-new-visitor", consumes = "application/json")
    ResponseEntity<Visitor> createVisitor(@Valid @RequestBody Visitor newVisitor) throws URISyntaxException {
        Visitor visitor = visitorRepository.save(newVisitor);
        return ResponseEntity.created(new URI("/api/register-new-visitor" + visitor.getVisitorId())).body(visitor);
    }
}
