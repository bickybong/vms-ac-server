package com.vmsac.vmsacserver.service;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.vmsac.vmsacserver.model.*;
import com.vmsac.vmsacserver.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepo;

    @Autowired
    private EntranceService entranceService;

    @Autowired
    private EntranceRepository entranceRepo;

    @Autowired
    private AccessGroupService accessGroupService;

    @Autowired
    private AccessGroupRepository accessGroupRepo;

    @Autowired
    private ControllerService controllerService;

    @Autowired
    private ControllerRepository controllerRepo;

    @Autowired
    private EventActionTypeRepository eventActionTypeRepository;

    public Boolean createEvents(List<Event> newEvents){
        // iterate over the whole list
        // check if it exists in db
        // check if linked columns are valid
        // record and save
        Boolean success = true;
        System.out.println(newEvents);

        try{
            for (Event singleEvent : newEvents){

                if (eventRepository.existsByEventTimeEqualsAndController_ControllerSerialNoEqualsAndEventActionType_EventActionTypeId(singleEvent.getEventTime(), singleEvent.getController().getControllerSerialNo(),singleEvent.getEventActionType().getEventActionTypeId())){
                    continue;
                }

                Event toSave = new Event();
                System.out.println("1");
                toSave.setDirection(singleEvent.getDirection());
                toSave.setEventTime(singleEvent.getEventTime());
                toSave.setDeleted(false);

                System.out.println(singleEvent.getPerson());
                if (singleEvent.getPerson() != null){
                    Optional<Person> optionalPerson = personService.findByIdInUse(singleEvent.getPerson().getPersonId());
                    if (optionalPerson.isEmpty()){
                        toSave.setPerson(null);
                    }
                    else{
                        toSave.setPerson(optionalPerson.get());
                    }}
                else{
                    toSave.setPerson(null);
                }

                System.out.println("3");
                if (singleEvent.getEntrance() != null){
                    Optional<Entrance> optionalEntrance = entranceService.findById(singleEvent.getEntrance().getEntranceId());
                    if (optionalEntrance.isEmpty()){
                        toSave.setEntrance(null);
                    }
                    else{
                        toSave.setEntrance(optionalEntrance.get());
                    }}
                else{
                    toSave.setEntrance(null);
                }
                System.out.println("4");
                if (singleEvent.getAccessGroup() != null){
                    Optional<AccessGroup> optionalAccessGroup = accessGroupService.findById(singleEvent.getAccessGroup().getAccessGroupId());
                    if (optionalAccessGroup.isEmpty()){
                        toSave.setAccessGroup(null);
                    }
                    else{
                        toSave.setAccessGroup(optionalAccessGroup.get());
                    }}
                else{
                    toSave.setAccessGroup(null);
                }

                toSave.setEventActionType(eventActionTypeRepository.getById(singleEvent.getEventActionType().getEventActionTypeId()));

                Optional<Controller> optionalController = controllerService.findBySerialNo(singleEvent.getController().getControllerSerialNo());
                if (optionalController.isEmpty()){
                    toSave.setController(null);
                }
                else{
                    toSave.setController(optionalController.get());
                }
                System.out.println("5");
                eventRepository.save(toSave);

            }}
        catch (Exception e){
            System.out.println(e);
            success = false;
        }
        System.out.println("6");
        return success;
    }

    public List<Event> getEventsByTimeDesc(int limit){
        List<Event> allEvents = eventRepository.findByDeletedIsFalseOrderByEventTimeDesc(PageRequest.of(0, limit));
        return allEvents;
    }

    public List<Event> getEventsByTimeDesc(String queryString, LocalDateTime start, LocalDateTime end, int limit) {
        List<Event> result;
        if (queryString != null && !queryString.equals("")) {

            List<EventActionType> eventTypes = eventActionTypeRepository.searchByEventActionTypeName(queryString);
            List<Long> eventTypeIds = eventTypes.stream().map(EventActionType::getEventActionTypeId).collect(Collectors.toList());

            List<Entrance> entrances = entranceRepo.searchByEntranceName(queryString);
            List<Long> entranceIds = entrances.stream().map(Entrance::getEntranceId).collect(Collectors.toList());

            List<Controller> controllers = controllerRepo.searchByControllerName(queryString);
            List<Long> controllerIds = controllers.stream().map(Controller::getControllerId).collect(Collectors.toList());

            List<Person> persons = personRepo.findAllByQueryString(queryString);
            List<Long> personIds = persons.stream().map(Person::getPersonId).collect(Collectors.toList());

            List<AccessGroup> accessGroups = accessGroupRepo.searchByAccessGroupName(queryString);
            List<Long> accessGroupIds = accessGroups.stream().map(AccessGroup::getAccessGroupId).collect(Collectors.toList());

            result = eventRepository.findByQueryString(eventTypeIds, entranceIds, controllerIds, personIds, accessGroupIds);
        } else
            result = getEventsByTimeDesc(limit);

        List<Event> resultAfterDatetime = result.stream().filter(e -> {
            LocalDateTime eventTime = LocalDateTime.parse(e.getEventTime(), DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss"));
            if (start != null) {
                if (!(eventTime.equals(start) || eventTime.isAfter(start)))
                    return false;
            }
            if (end != null) {
                if (!eventTime.isBefore(end))
                    return false;
            }
            return true;
        })
                .collect(Collectors.toList());

        return resultAfterDatetime.stream().limit(limit).collect(Collectors.toList());
    }
//
//    public MappingJacksonValue filterAllEvents(){
//        SimpleBeanPropertyFilter simpleBeanPropertyFilter =
//                SimpleBeanPropertyFilter.serializeAllExcept("id", "dob");
//
//        FilterProvider filterProvider = new SimpleFilterProvider()
//                .addFilter("userFilter", simpleBeanPropertyFilter);
//
//        MappingJacksonValue mappingJacksonValue = new MappingJacksonValue();
//        mappingJacksonValue.setFilters(filterProvider);
//
//        return mappingJacksonValue;
//    }
//
//    public MappingJacksonValue filterAllEvents(List<Event> AllEvents){
//        SimpleBeanPropertyFilter simpleBeanPropertyFilter =
//                SimpleBeanPropertyFilter.serializeAllExcept("id", "dob");
//
//        FilterProvider filterProvider = new SimpleFilterProvider()
//                .addFilter("eventFilter", simpleBeanPropertyFilter);
//
//        List<User> userList = userService.getAllUsers();
//        MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(userList);
//        mappingJacksonValue.setFilters(filterProvider);
//
//        return mappingJacksonValue;
//    }

}