CREATE DATABASE projectWork
    DEFAULT CHARACTER SET = 'utf8mb4';

USE projectWork;

CREATE TABLE users (
    
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    Email VARCHAR(255) NOT NULL UNIQUE,
    PasswordHash VARCHAR(255) NOT NULL,
    FirstName VARCHAR(50) NOT NULL,
    LastName VARCHAR(50) NOT NULL,
    CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LastAccessDate TIMESTAMP NULL,
    Active BOOLEAN DEFAULT FALSE
);


CREATE TABLE `groups` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    GroupName VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE UserGroups (
    user_id BIGINT,
    group_id BIGINT,
    PRIMARY KEY (user_id, group_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES `groups`(id)
);