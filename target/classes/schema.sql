create table USERS(
    ID int not null AUTO_INCREMENT,
    NAME varchar(255) not null,
    PASSWORD varchar(255) not null,
    PRIMARY KEY ( ID )
);

create table SESSIONS(
    ID int not null AUTO_INCREMENT,
    SESSION_ID varchar(255) not null,
    USER_ID int,
    PRIMARY KEY ( ID ),
    FOREIGN KEY (USER_ID) REFERENCES USERS(ID)
);