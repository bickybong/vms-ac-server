package com.vmsac.vmsacserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.vmsac.vmsacserver.model.credential.CredentialDto;
import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name = " controller")
@Builder
public class Controller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( name = "controllerid", columnDefinition = "serial")
    private Long controllerId;

    @Column( name = "controlleripstatic")
    private Boolean controllerIPStatic;

    @Column( name ="controllername")
    private String controllerName;

    @Column( name = "controllerip")
    private String controllerIP;

    @Column( name = "controllermac")
    private String controllerMAC;

    @Column( name = "controllerserialno")
    private String controllerSerialNo;


    @Column( name = "lastonline")
    private LocalDateTime lastOnline;

    @Column( name = "pinassignmentconfig")
    private String pinAssignmentConfig;

    @Column( name = "settingsconfig")
    private String settingsConfig;

    @JsonIgnore
    @Column( name = "deleted")
    private Boolean deleted;

    @OneToMany(mappedBy = "controller")
    private List<AuthDevice> AuthDevices;

    public UniconControllerDto touniconDto(){
        return new UniconControllerDto(this.controllerId,this.controllerIP,
                this.controllerIPStatic,this.controllerMAC,
                this.controllerSerialNo,this.deleted);
    }

    public FrontendControllerDto toFrontendDto(){
        return new FrontendControllerDto(this.controllerId,this.controllerName,
                this.controllerIP,
                this.controllerIPStatic,this.controllerMAC,
                this.controllerSerialNo,this.pinAssignmentConfig,
                this.settingsConfig);
    }

    @Override
    public String toString() {
        return "Controller : {" +
                "controllerId=" + controllerId +
                ", controllerIPStatic=" + controllerIPStatic +
                ", controllerName='" + controllerName + '\'' +
                ", controllerIP='" + controllerIP + '\'' +
                ", controllerMAC='" + controllerMAC + '\'' +
                ", controllerSerialNo='" + controllerSerialNo + '\'' +
                ", lastOnline=" + lastOnline +
                ", pinAssignmentConfig='" + pinAssignmentConfig + '\'' +
                ", settingsConfig='" + settingsConfig + '\'' +
                ", deleted=" + deleted +
                ", controllerAuthDevices=" + AuthDevices +
                '}';
    }
}