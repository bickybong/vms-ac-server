CREATE TABLE Visitor (
    idNumber VARCHAR(128) NOT NULL,
    firstName VARCHAR(128),
    lastName VARCHAR(128),
    mobileNumber VARCHAR(128),
    emailAdd VARCHAR(128),
    PRIMARY KEY (idNumber)
);
CREATE TABLE ScheduledVisit (
    scheduledVisitId BIGINT NOT NULL AUTO_INCREMENT,
    idNumber VARCHAR(128),
    startDateOfVisit DATE,
    endDateOfVisit DATE,
    qrCodeId VARCHAR(128),
    valid BOOLEAN,
    oneTimeUse BOOLEAN,
    raisedBy BIGINT,
    PRIMARY KEY (scheduledVisitId),
    FOREIGN KEY (idNumber) REFERENCES Visitor (idNumber)
);
