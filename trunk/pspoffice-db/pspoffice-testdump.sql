insert into last_name(id, name) values (1,'Матвеев');
insert into first_name(id, name) values (1,'Матвей');
insert into middle_name(id, name) values (1,'Матвеевич');
insert into person(object_id) values (1);
insert into person_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values (1,1,2000,1,2000), (1,1,2001,1,2001),
(1,1,2002,1,2002);