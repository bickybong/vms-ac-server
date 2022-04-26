package com.vmsac.vmsacserver.repository;

import com.vmsac.vmsacserver.model.Controller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ControllerRepository extends JpaRepository<Controller, Long>{

    List<Controller> findByDeleted(Boolean deleted);

    Optional<Controller> findByControllerSerialNoEqualsAndDeletedIsFalse(String controllerSerialNo);

    Optional<Controller> findByControllerIdEquals(Long controllerId);

    Optional<Controller> findByControllerIdEqualsAndDeletedFalse(Long controllerId);




}