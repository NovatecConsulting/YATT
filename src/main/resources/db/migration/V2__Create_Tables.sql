CREATE TABLE projects(
    project_id VARCHAR(36) NOT NULL,
    project_name TEXT NOT NULL,
    planned_start_date DATE NOT NULL,
    deadline DATE NOT NULL,
    PRIMARY KEY(project_id)
);