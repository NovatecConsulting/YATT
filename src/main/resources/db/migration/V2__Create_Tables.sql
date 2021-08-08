CREATE TABLE projects(
    projectId VARCHAR(36) NOT NULL,
    projectName TEXT NOT NULL,
    plannedStartDate DATE NOT NULL,
    deadline DATE NOT NULL,
    PRIMARY KEY(projectId)
);