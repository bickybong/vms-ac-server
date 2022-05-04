package com.vmsac.vmsacserver.service;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmsac.vmsacserver.model.*;
import com.vmsac.vmsacserver.model.accessgroupentrance.AccessGroupEntranceNtoN;
import com.vmsac.vmsacserver.model.accessgroupschedule.AccessGroupSchedule;
import com.vmsac.vmsacserver.model.accessgroupschedule.AccessGroupScheduleDto;
import com.vmsac.vmsacserver.model.credential.CredentialDto;
import com.vmsac.vmsacserver.model.credentialtype.entranceschedule.EntranceSchedule;
import com.vmsac.vmsacserver.model.credential.Credential;
import com.vmsac.vmsacserver.repository.AuthDeviceRepository;
import com.vmsac.vmsacserver.repository.ControllerRepository;
import com.vmsac.vmsacserver.repository.EntranceRepository;
import com.vmsac.vmsacserver.repository.EntranceScheduleRepository;
import com.vmsac.vmsacserver.repository.AccessGroupEntranceNtoNRepository;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.Null;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;



@Service
public class ControllerService {


    String pinAssignment = "{'E1_IN_D0': '14', 'E1_IN_D1': '15', 'E1_IN_Buzz': '23', 'E1_IN_Led': '18', 'E1_OUT_D0': '2', 'E1_OUT_D1': '3', 'E1_OUT_Buzz': '17', 'E1_OUT_Led': '4', 'E1_Mag': '6', 'E1_Button': '5','E2_IN_D0': '24', 'E2_IN_D1':'25', 'E2_IN_Buzz': '7', 'E2_IN_Led': '8', 'E2_OUT_D0': '22', 'E2_OUT_D1': '10', 'E2_OUT_Buzz': '11', 'E2_OUT_Led': '9', 'E2_Mag': '26', 'E2_Button': '12', 'Relay_1': '27', 'Relay_2': '13', 'Fire': '26', 'Gen_In_1': '16', 'Gen_Out_1': '', 'Gen_In_2': '20', 'Gen_Out_2': '', 'Gen_In_3': '21', 'Gen_Out_3': ''}";
    String settingsConfig = "testsettings";



    @Autowired
    private AuthDeviceRepository authDeviceRepository;

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private AccessGroupScheduleService accessGroupScheduleService;

    @Autowired
    private PersonService personService;

    @Autowired
    private AccessGroupEntranceNtoNRepository accessGroupEntranceNtoNRepository;

    @Autowired
    private ControllerRepository controllerRepository;

    @Autowired
    EntranceScheduleRepository entranceScheduleRepository;

    public List<Controller> findAllNotDeleted() {
        return controllerRepository.findByDeleted(false).stream()
                .collect(Collectors.toList());
    }

    public Optional<Controller> findById (Long controllerId) {
        return controllerRepository.findByControllerIdEqualsAndDeletedFalse(controllerId);

    }

    public Optional<Controller> findBySerialNo (String controllerSerialNo) {
        return controllerRepository.findByControllerSerialNoEqualsAndDeletedIsFalse(controllerSerialNo);

    }

    public UniconControllerDto uniconControllerCreate(UniconControllerDto uniconControllerDto){
        String ip = InetAddress.getLoopbackAddress().getHostAddress();
        Boolean status = false;
        if (uniconControllerDto.getControllerIP() == ip){
            status = true;
        }

        return controllerRepository.save(uniconControllerDto.toCreateController(uniconControllerDto.getControllerSerialNo(),
                LocalDateTime.now(ZoneId.of("GMT+08:00")),status,LocalDateTime.now(ZoneId.of("GMT+08:00")),pinAssignment,settingsConfig,false)).touniconDto();
    }



    public UniconControllerDto uniconControllerUpdate(UniconControllerDto uniconControllerDto) throws Exception{
        controllerRepository.findByControllerSerialNoEqualsAndDeletedIsFalse(uniconControllerDto.getControllerSerialNo())
                .orElseThrow(() -> new RuntimeException("Controller does not exist"));

        Controller existingcontroller = (((controllerRepository.findByControllerSerialNoEqualsAndDeletedIsFalse(uniconControllerDto.getControllerSerialNo())).get()));

        if ( (existingcontroller.getControllerId() == uniconControllerDto.getControllerId()) ||
                Objects.isNull(uniconControllerDto.getControllerId()) ){


            Controller toSave = uniconControllerDto.toController();
            toSave.setControllerId(existingcontroller.getControllerId());
            toSave.setControllerName(existingcontroller.getControllerName());
            toSave.setLastOnline(LocalDateTime.now(ZoneId.of("GMT+08:00")));
            toSave.setPinAssignmentConfig(pinAssignment);
            toSave.setSettingsConfig(settingsConfig);
            toSave.setPendingIP(existingcontroller.getPendingIP());
            toSave.setMasterController(existingcontroller.getMasterController());
            toSave.setCreated(existingcontroller.getCreated());

            return controllerRepository.save(toSave).touniconDto();


        }
        throw new RuntimeException("Controller Id clashes");


    }

    public FrontendControllerDto FrondEndControllerUpdate(FrontendControllerDto newFrontendControllerDto) throws Exception{
        controllerRepository.findByControllerSerialNoEqualsAndDeletedIsFalse(newFrontendControllerDto.getControllerSerialNo())
                .orElseThrow(() -> new RuntimeException("Controller does not exist"));

        Controller existingcontroller = (((controllerRepository.findByControllerSerialNoEqualsAndDeletedIsFalse(newFrontendControllerDto.getControllerSerialNo())).get()));

        if ( (existingcontroller.getControllerId() == newFrontendControllerDto.getControllerId()) ||
                Objects.isNull(newFrontendControllerDto.getControllerId()) ){


            existingcontroller.setControllerName(newFrontendControllerDto.getControllerName());
            existingcontroller.setControllerIP(newFrontendControllerDto.getControllerIP());
            existingcontroller.setControllerIPStatic(newFrontendControllerDto.getControllerIPStatic());

            return controllerRepository.save(existingcontroller).toFrontendDto();


        }
        throw new RuntimeException("Controller Id clashes");


    }

    public void deleteControllerWithId(Long controllerId) throws Exception {
        Controller toDeleted = controllerRepository.findByControllerIdEqualsAndDeletedFalse(controllerId)
                .orElseThrow(() -> new RuntimeException("Controller does not exist"));

        toDeleted.setDeleted(true);
        toDeleted.setAuthDevices(Collections.emptyList());
        controllerRepository.save(toDeleted);
    }

    public void shutdownunicon(String IPaddress) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String resourceUrl = "http://"+IPaddress+":5000/api/shutdown";
        HttpEntity<String> request = new HttpEntity<String>("");

        ResponseEntity<String> productCreateResponse =
                restTemplate.exchange(resourceUrl, HttpMethod.POST, request, String.class);

        return;
    }

    public void rebootunicon(String IPaddress) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String resourceUrl = "http://"+IPaddress+":5000/api/reboot";
        HttpEntity<String> request = new HttpEntity<String>("");

        ResponseEntity<String> productCreateResponse =
                restTemplate.exchange(resourceUrl, HttpMethod.POST, request, String.class);

        return;
    }

    public Boolean backToDefault(Controller existingController) throws Exception {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(3000);
        httpRequestFactory.setConnectTimeout(3000);
        httpRequestFactory.setReadTimeout(3000);

        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);

        String IPaddress = existingController.getControllerIP();
        String resourceUrl = "http://"+IPaddress+":5000/api/reset";
        HttpEntity<String> request = new HttpEntity<String>("");

        try{

            ResponseEntity<String> productCreateResponse =
                    restTemplate.exchange(resourceUrl, HttpMethod.POST, request, String.class);
            return false;
        }
        catch(Exception e){
            {
                Thread.sleep(2000);
                LocalDateTime lastonlinedatetime = controllerRepository.findByControllerSerialNoEqualsAndDeletedIsFalse(existingController
                        .getControllerSerialNo()).get().getLastOnline();

                LocalDateTime currentdatetime = LocalDateTime.now(ZoneId.of("GMT+08:00"));

                if (lastonlinedatetime.isAfter(currentdatetime.minusSeconds(30))) {
                    return true;
                }
                return false;
            }

        }
    }

    public ControllerConnection getControllerConnectionUnicon(String IPaddress) throws Exception {
            RestTemplate restTemplate = new RestTemplate();

            String resourceUrl = "http://"+IPaddress+":5000/api/status";
            HttpEntity<String> request = new HttpEntity<String>("");

            ResponseEntity<String> productCreateResponse =
                    restTemplate.exchange(resourceUrl, HttpMethod.GET, request, String.class);

            if (productCreateResponse.getStatusCodeValue() == 200){
                ObjectMapper mapper = new ObjectMapper();
                ControllerConnection connection = mapper.readValue(productCreateResponse.getBody(), ControllerConnection.class);

                return connection;
            }
            else{
                return null;
            }

    }

    public HttpStatus sendEntranceNameRelationship(Long controllerId) throws Exception {

        Controller existingcontroller = controllerRepository.getById(1L);
        String IPaddress = existingcontroller.getControllerIP();

        RestTemplate restTemplate = new RestTemplate();
        String resourceUrl = "http://"+IPaddress+":5000/api/entrance-name";

        Map <String,Object> jsonbody = new HashMap();
        jsonbody.put("controllerSerialNo",existingcontroller.getControllerSerialNo());


        try{
            jsonbody.put("E1",existingcontroller.getAuthDevices().get(0).getEntrance().getEntranceName());
        }
        catch(Exception e){
            jsonbody.put("E1","");
        }

        try{
            jsonbody.put("E2",existingcontroller.getAuthDevices().get(2).getEntrance().getEntranceName());
        }
        catch(Exception e){
            jsonbody.put("E2","");
        }

        HttpEntity<Map> request = new HttpEntity<>
                (jsonbody);

        ResponseEntity<String> productCreateResponse =
                restTemplate.exchange(resourceUrl, HttpMethod.POST, request, String.class);

        if (productCreateResponse.getStatusCodeValue() == 200){
            ObjectMapper mapper = new ObjectMapper();
            ControllerConnection connection = mapper.readValue(productCreateResponse.getBody(), ControllerConnection.class);
            return HttpStatus.OK;
        }
        else{
            return HttpStatus.BAD_REQUEST;
        }
    }

    public HttpStatus generate(){
        try {

            Controller existingcontroller = controllerRepository.getById(1L);

            String test ="";

            String MASTERPASSWORD = "666666";

            List<Map> RulesSet = new ArrayList<Map>(1);

            for ( int i=0; i<2;i++) {
                try {
                    Entrance existingentrance = existingcontroller.getAuthDevices().get(i * 2).getEntrance();
                    EntranceSchedule exisitngEntranceSchedule = entranceScheduleRepository.findByEntranceIdEqualsAndDeletedIsFalse(existingentrance.getEntranceId());

                    Map<String, Object> entrance = new HashMap();
                    entrance.put("Entrance", existingentrance.getEntranceName());


                    String rawrrule = exisitngEntranceSchedule.getRrule();
                    String startdatetime = rawrrule.split("\n")[0].split(":")[1].split("T")[0];
                    String rrule = rawrrule.split("\n")[1].split(":")[1];
                    Integer year = Integer.parseInt(startdatetime.substring(0,4));
                    Integer month = Integer.parseInt(startdatetime.substring(4,6));
                    Integer day = Integer.parseInt(startdatetime.substring(6,8));

                    RecurrenceRule rule = new RecurrenceRule(rrule);
                    DateTime start = new DateTime(year, month /* 0-based month numbers! */,day);
                    RecurrenceRuleIterator it = rule.iterator(start);
                    int maxInstances = 100; // limit instances for rules that recur forever
                    // think about how to generate one year worth
                    while (it.hasNext() && (!rule.isInfinite() || maxInstances-- > 0))
                    {
                        DateTime nextInstance = it.nextDateTime();
                        // do something with nextInstance
                        System.out.println(nextInstance);
                    }

                    entrance.put("EntranceSchedule", exisitngEntranceSchedule);

                    Map<String, Object> existingentrancedetails = new HashMap();
                    existingentrancedetails.put("Antipassback", "No");
                    existingentrancedetails.put("Zone", "ZoneId");

                    Map<String, Object> authdevices = new HashMap();

                    AuthDevice exisitngDevice1 = authDeviceRepository.findByEntrance_EntranceIdIsAndAuthDeviceDirectionContains(existingentrance.getEntranceId(), "IN");
                    AuthDevice exisitngDevice2 = authDeviceRepository.findByEntrance_EntranceIdIsAndAuthDeviceDirectionContains(existingentrance.getEntranceId(), "OUT");

                    Map<String, Object> Device1 = new HashMap();

                    if (exisitngDevice1.getMasterpin() == true) {
                        Device1.put("Masterpassword", MASTERPASSWORD);
                    } else {
                        Device1.put("Masterpassword", "");
                    }

                    Device1.put("Direction", exisitngDevice1.getAuthDeviceDirection().substring(3));

                    List<AuthDevice> AuthMethod1 = new ArrayList<AuthDevice>(1);
                    Device1.put("AuthMethod", AuthMethod1);

                    Map<String, Object> Device2 = new HashMap();

                    if (exisitngDevice2.getMasterpin() == true) {
                        Device2.put("Masterpassword", MASTERPASSWORD);
                    } else {
                        Device2.put("Masterpassword", "");
                    }

                    Device2.put("Direction", exisitngDevice1.getAuthDeviceDirection().substring(3));

                    List<AuthDevice> AuthMethod2 = new ArrayList<AuthDevice>(1);
                    Device2.put("AuthMethod", AuthMethod2);

                    authdevices.put("Device1", Device1);
                    authdevices.put("Device2", Device2);

                    existingentrancedetails.put("AuthenticationDevices", authdevices);

                    // for all access group in entrances
//                accessgroups.put()
                    List<Map> accessGroups = new ArrayList<Map>(1);


                    List<AccessGroupEntranceNtoN> listOfAccessGroupsNtoN = accessGroupEntranceNtoNRepository.findAllByEntranceEntranceIdAndDeletedFalse(existingentrance.getEntranceId());

                    for (AccessGroupEntranceNtoN accessGroupEntranceNtoN : listOfAccessGroupsNtoN) {

                        List<Person> ListofPersons = personService.findByAccGrpId((accessGroupEntranceNtoN.getAccessGroup().getAccessGroupId()), false);
                        List<AccessGroupScheduleDto> ListofSchedule = accessGroupScheduleService.findAllByGroupToEntranceIdIn(Collections.singletonList(accessGroupEntranceNtoN.getGroupToEntranceId()));

                        Map<String, Object> oneAccessGroup = new HashMap();

                        Map<String, Object> personsAndSchedule = new HashMap();

                        List<Map> EditedListofPersons = new ArrayList<Map>(1);

                        for (Person person : ListofPersons) {
                            Map<String, Object> eachPerson = new HashMap();
                            eachPerson.put("Name", person.getPersonFirstName() + " " + person.getPersonLastName());

                            List<CredentialDto> ListofCred = credentialService.findByPersonId(person.getPersonId());
                            Map<String, Object> personcredentials = new HashMap();
                            for (CredentialDto credentialDto : ListofCred) {
                                personcredentials.put(credentialDto.getCredType().getCredTypeName(), credentialDto.getCredUid());
                            }
                            eachPerson.put("Credentials", personcredentials);
                            EditedListofPersons.add(eachPerson);
                        }

                        personsAndSchedule.put("Persons", EditedListofPersons);
                        personsAndSchedule.put("Schedule", ListofSchedule);

                        oneAccessGroup.put(accessGroupEntranceNtoN.getAccessGroup().getAccessGroupName(), personsAndSchedule);

                        accessGroups.add(oneAccessGroup);
                    }


//                for(User user : listOfUsers) {
//                    List<User> users = new ArrayList<User>(1);
//                    users.add(user);
//                    usersByCountry.put(user.getCountry(), users);
//                }


                    existingentrancedetails.put("AccessGroups", accessGroups);
                    entrance.put("EntranceDetails", existingentrancedetails);

                    System.out.println(entrance);

                    RulesSet.add(entrance);
                } catch (Exception e) {

                }

//                String json = new ObjectMapper().writeValueAsString(RulesSet);
//
//                System.out.println(json);

                String resourceUrl = "http://192.168.1.185:5000/credOccur";
                RestTemplate restTemplate = new RestTemplate();
                HttpEntity<List> request = new HttpEntity<>
                        (RulesSet);

                ResponseEntity<String> productCreateResponse =
                        restTemplate.exchange(resourceUrl, HttpMethod.POST, request, String.class);

                //call entrancename function
                return HttpStatus.OK;

            }
        }
        catch(Exception e){
            System.out.println(e);
        }
        return HttpStatus.BAD_REQUEST;
    }

    public void save(Controller existingcontroller) {
        controllerRepository.save(existingcontroller);
    }

    public Boolean IsIPavailable(String ipAddress)
            throws UnknownHostException, IOException
    {
        InetAddress geek = InetAddress.getByName(ipAddress);
        System.out.println("Sending Ping Request to " + ipAddress);
        if (geek.isReachable(5000))
            return false;
        else
            return true;
    }

    public Boolean isNotValidInet4Address(String ip)
    {
        String[] splitString = ip.split("[.]");
        if (splitString.length > 4) {
            return true;
        }
        for (String string : splitString) {
            if (string.isEmpty()) {
                return true;
            }
            if (!string.matches("[0-9]{1,3}")) {
                return true;
            }
            int number = Integer.parseInt(string);
            if (!(number >= 0 && number <= 255)) {
                return true;
            }
        }
        return false;
    }

    public Boolean UpdateUniconIP(FrontendControllerDto newFrontendControllerDto) throws Exception {

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(3000);
        httpRequestFactory.setConnectTimeout(3000);
        httpRequestFactory.setReadTimeout(3000);

        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);

        Controller existingController = controllerRepository.findById(newFrontendControllerDto.getControllerId()).get();
        existingController.setPendingIP(newFrontendControllerDto.getControllerIP());
        controllerRepository.save(existingController);

        String resourceUrl = "http://"+ existingController.getControllerIP()+":5000/api/config";

        Map<String,Object> requestBody = new LinkedHashMap<>();
        requestBody.put("controllerIPStatic",newFrontendControllerDto.getControllerIPStatic());
        requestBody.put("controllerIP",newFrontendControllerDto.getControllerIP());
        requestBody.put("controllerSerialNo",newFrontendControllerDto.getControllerSerialNo());

        HttpEntity<Map> request = new HttpEntity<Map>(requestBody);

        try{

            ResponseEntity<String> productCreateResponse =
                    restTemplate.exchange(resourceUrl, HttpMethod.POST, request, String.class);
            return false;
        }
        catch(Exception e){
            if (newFrontendControllerDto.getControllerIPStatic() == false){
                Thread.sleep(2000);
                LocalDateTime lastonlinedatetime = controllerRepository.findByControllerSerialNoEqualsAndDeletedIsFalse(newFrontendControllerDto
                        .getControllerSerialNo()).get().getLastOnline();

                LocalDateTime currentdatetime = LocalDateTime.now(ZoneId.of("GMT+08:00"));

                if (lastonlinedatetime.isAfter(currentdatetime.minusSeconds(30))) {
                    return true;
                }
                return false;
            }
            long startTime = System.currentTimeMillis(); //fetch starting time
            // get response
            while((System.currentTimeMillis()-startTime)<10000){
                Thread.sleep(1000);
                try{
                    if ( getControllerConnectionUnicon(newFrontendControllerDto.getControllerIP()) != null){
                        return true;
                }}
                catch (Exception e1){}
                }
            return false;
            }
        }
}
