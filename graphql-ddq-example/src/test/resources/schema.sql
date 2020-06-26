Drop table if exists hospital_tbl;
Drop table if exists surgeon_tbl;
Drop table if exists department_tbl;
Drop table if exists doc_speciality_tbl;
Drop table if exists speciality_tbl;
CREATE TABLE IF NOT EXISTS hospital_tbl
(
    hospital_id     INT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    no_of_employees int,
    city            VARCHAR(50),
    contact_no      varchar(15)
);
CREATE TABLE IF NOT EXISTS surgeon_tbl
(
    surgeon_id  INT PRIMARY KEY,
    hospital_id int,
    full_name   VARCHAR(255) NOT NULL,
    dept_id     int,
    contact_no  varchar(15)
);
CREATE TABLE IF NOT EXISTS department_tbl
(
    dept_id      INT          NOT NULL,
    hospital_id  INT          NOT NULL,
    dept_name    VARCHAR(255) NOT NULL,
    dept_head_id int,
    PRIMARY KEY (dept_id, hospital_id)
);
CREATE TABLE IF NOT EXISTS doc_speciality_tbl
(
    id            INT PRIMARY KEY,
    surgeon_id    INT,
    speciality_id INT,
    experience    int
);
CREATE TABLE IF NOT EXISTS speciality_tbl
(
    speciality_id INT PRIMARY KEY,
    speciality    VARCHAR(255) NOT NULL
);
insert into hospital_tbl(hospital_id, name, no_of_employees, city, contact_no)
values (1001, 'Hospital Name1', 200, 'Bangalore', '9883104201');
insert into hospital_tbl(hospital_id, name, no_of_employees, city, contact_no)
values (1002, 'Hospital Name2', 150, 'Hosmat', '8883104201');
insert into hospital_tbl(hospital_id, name, no_of_employees, city, contact_no)
values (1003, 'Hospital Name3', 100, 'Marathalli', '0883104201');
insert into hospital_tbl(hospital_id, name, no_of_employees, city, contact_no)
values (1004, 'Hospital Name4', 120, 'Munnekolala', '8333104201');
insert into hospital_tbl(hospital_id, name, no_of_employees, city, contact_no)
values (1005, 'Hospital Name5', 300, 'Bangalore', '8883155201');

insert into surgeon_tbl(surgeon_id, hospital_id, full_name, dept_id, contact_no)
values (4001, 1001, 'Human Name1', 901, '8281110112');
insert into surgeon_tbl(surgeon_id, hospital_id, full_name, dept_id, contact_no)
values (4002, 1002, 'Human Name2', 902, '9881110112');
insert into surgeon_tbl(surgeon_id, hospital_id, full_name, dept_id, contact_no)
values (4003, 1003, 'Human Name3', 903, '8281551112');
insert into surgeon_tbl(surgeon_id, hospital_id, full_name, dept_id, contact_no)
values (4004, 1005, 'Human Name4', 904, '1181110112');
insert into surgeon_tbl(surgeon_id, hospital_id, full_name, dept_id, contact_no)
values (4005, 1001, 'Human Name5', 905, '9981110112');
insert into surgeon_tbl(surgeon_id, hospital_id, full_name, dept_id, contact_no)
values (4006, 1001, 'Human Name6', 901, '7771110112');

insert into department_tbl(dept_id, hospital_id, dept_name, dept_head_id)
values (101, 1001, 'Cardiology', 4001);
insert into department_tbl(dept_id, hospital_id, dept_name, dept_head_id)
values (102, 1002, 'Neurology', 4002);
insert into department_tbl(dept_id, hospital_id, dept_name, dept_head_id)
values (103, 1003, 'Oncology', 4003);
insert into department_tbl(dept_id, hospital_id, dept_name, dept_head_id)
values (104, 1005, 'Gynaecology', 4004);
insert into department_tbl(dept_id, hospital_id, dept_name, dept_head_id)
values (105, 1001, 'Emergency department', 4005);

insert into speciality_tbl(speciality_id, speciality)
values (3001, 'Anesthesiologists');
insert into speciality_tbl(speciality_id, speciality)
values (3002, 'Cardiologists');
insert into speciality_tbl(speciality_id, speciality)
values (3003, 'Dermatologists');
insert into speciality_tbl(speciality_id, speciality)
values (3004, 'Endocrinologists');
insert into speciality_tbl(speciality_id, speciality)
values (3005, 'Gastroenterologists');

insert into doc_speciality_tbl(id, surgeon_id, speciality_id, experience)
values (1, 4001, 3001, 10);
insert into doc_speciality_tbl(id, surgeon_id, speciality_id, experience)
values (2, 4002, 3002, 5);
insert into doc_speciality_tbl(id, surgeon_id, speciality_id, experience)
values (3, 4006, 3002, 20);
insert into doc_speciality_tbl(id, surgeon_id, speciality_id, experience)
values (4, 4004, 3005, 25);
insert into doc_speciality_tbl(id, surgeon_id, speciality_id, experience)
values (5, 4001, 3004, 6);

