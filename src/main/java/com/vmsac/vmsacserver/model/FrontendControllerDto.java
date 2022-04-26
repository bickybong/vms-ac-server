package com.vmsac.vmsacserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FrontendControllerDto {

    @NonNull
    private Long controllerId;

    @NonNull
    private String controllerIP;

    @NonNull
    private String controllerName;

    @NonNull
    private Boolean controllerIPStatic;

    @NonNull
    private String controllerMAC;

    @NonNull
    private String controllerSerialNo;



    private String pinAssignmentConfig;

    private String settingsConfig;


    public Controller toController(){
        return new Controller(controllerId,controllerIPStatic,controllerName,controllerIP,
                controllerMAC,controllerSerialNo,null,
                pinAssignmentConfig,settingsConfig,false,null);
    }
}