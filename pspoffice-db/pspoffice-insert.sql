
-- Person --
INSERT INTO `sequence` (`sequence_name`, `sequence_value`) VALUES ('person',1), ('person_string_culture',1);

insert into `string_culture`(`id`, `locale_id`, `value`) values (2000, 1, 'Персона'), (2000, 2, 'Персона');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (2000, 'person', 2000, '');
/* ФИО */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2001, 1, UPPER('Фамилия')), (2001, 2, UPPER('Прізвище'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2000, 2000, 1, 2001, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2002, 1, UPPER('Имя')), (2002, 2, UPPER('Ім\'я'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2001, 2000, 1, 2002, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2003, 1, UPPER('Отчество')), (2003, 2, UPPER('По батькові'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2002, 2000, 1, 2003, 1);
/* Идентификационный код  */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2004, 1, UPPER('Идентификационный код')), (2004, 2, UPPER('Идентификационный код'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2003, 2000, 0, 2004, 1);
/* Дата рождения */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2005, 1, UPPER('Дата рождения')), (2005, 2, UPPER('Дата нарождения'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2004, 2000, 0, 2005, 1);
/* Место рождения */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2006, 1, UPPER('Страна')), (2006, 2, UPPER('Страна'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2005, 2000, 0, 2006, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2007, 1, UPPER('Регион')), (2007, 2, UPPER('Регион'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2006, 2000, 0, 2007, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2008, 1, UPPER('Район')), (2008, 2, UPPER('Район'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2007, 2000, 0, 2008, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2009, 1, UPPER('Нас. пункт')), (2009, 2, UPPER('Місто'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2008, 2000, 0, 2009, 1);
/* Паспортные данные */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2010, 1, UPPER('Серия')), (2010, 2, UPPER('Серія'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2009, 2000, 0, 2010, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2011, 1, UPPER('Номер')), (2011, 2, UPPER('Номер'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2010, 2000, 0, 2011, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2012, 1, UPPER('Кем выдан')), (2012, 2, UPPER('Орган видачі'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2011, 2000, 0, 2012, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2013, 1, UPPER('Дата выдачи')), (2013, 2, UPPER('Дата видачі'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2012, 2000, 0, 2013, 1);
/* Дата смерти  */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2014, 1, UPPER('Дата смерти')), (2014, 2, UPPER('Дата смерти'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2013, 2000, 0, 2014, 1);
/* Отношение к воинской службе */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2015, 1, UPPER('Отношение к воинской службе')), (2015, 2, UPPER('Відношення до військової служби'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2014, 2000, 0, 2015, 1);
/* Ссылки на детей до 16 лет */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2016, 1, UPPER('Дети до 16 лет')), (2016, 2, UPPER('Діти віком до 16 років'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2015, 2000, 0, 2016, 1);
/* Пол */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2017, 1, UPPER('Пол')), (2017, 2, UPPER('Стать'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2016, 2000, 0, 2017, 1);
/* Для детей до 16 лет: информация о рождении */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2018, 1, UPPER('Свидетельство о рождении')), (2018, 2, UPPER('Свідоцтво про нарождення'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2017, 2000, 0, 2018, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2019, 1, UPPER('Дата выдачи')), (2019, 2, UPPER('Дата видачі'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2018, 2000, 0, 2019, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2020, 1, UPPER('Кем выдан')), (2020, 2, UPPER('Орган видачі'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2019, 2000, 0, 2020, 1);
/* Является ли гражданином Украины */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2021, 1, UPPER('Является гражданином Украины')), (2021, 2, UPPER('Свідоцтво про належність до громадянства України'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2020, 2000, 1, 2021, 1);

insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2000, 2000, UPPER('last_name'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2001, 2001, UPPER('first_name'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2002, 2002, UPPER('middle_name'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2003, 2003, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2004, 2004, UPPER('date2'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2005, 2005, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2006, 2006, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2007, 2007, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2008, 2008, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2009, 2009, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2010, 2010, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2011, 2011, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2012, 2012, UPPER('date2'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2013, 2013, UPPER('date'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2014, 2014, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2015, 2015, 'person');
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2016, 2016, UPPER('gender'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2017, 2017, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2018, 2018, UPPER('date2'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2019, 2019, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2020, 2020, UPPER('boolean'));

-- Apartment Card --
INSERT INTO `sequence` (`sequence_name`, `sequence_value`) VALUES ('apartment_card',1), ('apartment_card_string_culture',1);

insert into `string_culture`(`id`, `locale_id`, `value`) values (2400, 1, 'Поквартирная карточка'), (2400, 2, 'Поквартирная карточка');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (2400, 'apartment_card', 2400, '');
/* Лицевой счет */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2401, 1, UPPER('Лицевой счет')), (2401, 2, UPPER('Лицевой счет'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2400, 2400, 1, 2401, 1);
/* Собственник */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2402, 1, UPPER('Собственник')), (2402, 2, UPPER('Собственник'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2401, 2400, 1, 2402, 1);
/* Адрес */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2403, 1, UPPER('Адрес')), (2403, 2, UPPER('Адрес'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2402, 2400, 1, 2403, 1);
/* Форма собственности */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2404, 1, UPPER('Форма собственности')), (2404, 2, UPPER('Форма собственности'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2403, 2400, 0, 2404, 1);
/* Документ права на жилплощадь */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2405, 1, UPPER('Документ права на жилплощадь')), (2405, 2, UPPER('Документ права на жилплощадь'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2404, 2400, 0, 2405, 1);
/* Ссылка на регистрации */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2406, 1, UPPER('Зарегистрированные')), (2406, 2, UPPER('Зарегистрированные'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2405, 2400, 0, 2406, 1);

insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2400, 2400, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2401, 2401, 'person');
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2402, 2402, 'room');
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2403, 2402, 'apartment');
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2404, 2402, 'building');
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2405, 2403, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2406, 2404, UPPER('big_string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2407, 2405, 'registration');

-- Registration --
INSERT INTO `sequence` (`sequence_name`, `sequence_value`) VALUES ('registration',1), ('registration_string_culture',1);

insert into `string_culture`(`id`, `locale_id`, `value`) values (2100, 1, 'Регистрация'), (2100, 2, 'Регистрация');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (2100, 'registration', 2100, '');
/* Адрес прибытия */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2101, 1, UPPER('Страна')), (2101, 2, UPPER('Страна'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2100, 2100, 0, 2101, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2102, 1, UPPER('Регион')), (2102, 2, UPPER('Регион'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2101, 2100, 0, 2102, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2103, 1, UPPER('Район')), (2103, 2, UPPER('Район'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2102, 2100, 0, 2103, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2104, 1, UPPER('Нас. пункт')), (2104, 2, UPPER('Місто'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2103, 2100, 0, 2104, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2105, 1, UPPER('Улица')), (2105, 2, UPPER('Улица'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2104, 2100, 0, 2105, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2106, 1, UPPER('Дом №')), (2106, 2, UPPER('Дом №'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2105, 2100, 0, 2106, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2107, 1, UPPER('Корп.')), (2107, 2, UPPER('Корп.'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2106, 2100, 0, 2107, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2108, 1, UPPER('Кв.')), (2108, 2, UPPER('Кв.'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2107, 2100, 0, 2108, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2109, 1, UPPER('Дата прибытия')), (2109, 2, UPPER('Дата прибытия'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2108, 2100, 0, 2109, 1);
/* Адрес выбытия */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2110, 1, UPPER('Страна')), (2110, 2, UPPER('Страна'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2109, 2100, 0, 2110, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2111, 1, UPPER('Регион')), (2111, 2, UPPER('Регион'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2110, 2100, 0, 2111, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2112, 1, UPPER('Район')), (2112, 2, UPPER('Район'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2111, 2100, 0, 2112, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2113, 1, UPPER('Нас. пункт')), (2113, 2, UPPER('Місто'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2112, 2100, 0, 2113, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2114, 1, UPPER('Улица')), (2114, 2, UPPER('Улица'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2113, 2100, 0, 2114, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2115, 1, UPPER('Дом №')), (2115, 2, UPPER('Дом №'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2114, 2100, 0, 2115, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2116, 1, UPPER('Корп.')), (2116, 2, UPPER('Корп.'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2115, 2100, 0, 2116, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2117, 1, UPPER('Кв.')), (2117, 2, UPPER('Кв.'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2116, 2100, 0, 2117, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2118, 1, UPPER('Дата выбытия')), (2118, 2, UPPER('Дата вибуття'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2117, 2100, 0, 2118, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2119, 1, UPPER('Причина выбытия')), (2119, 2, UPPER('Причина вибуття'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2118, 2100, 0, 2119, 1);
/* Дата и тип регистрации */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2120, 1, UPPER('Дата начала регистрации')), (2120, 2, UPPER('Дата начала регистрации'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2119, 2100, 0, 2120, 1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2121, 1, UPPER('Тип регистрации')), (2121, 2, UPPER('Тип регистрации'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2120, 2100, 0, 2121, 1);
/* Отношение к владельцу */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2122, 1, UPPER('Отношение к владельцу')), (2122, 2, UPPER('Отношение к владельцу'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2121, 2100, 1, 2122, 1);
/* Ссылка на Person */
insert into `string_culture`(`id`, `locale_id`, `value`) values (2123, 1, UPPER('Персона')), (2123, 2, UPPER('Персона'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2122, 2100, 1, 2123, 1);

insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2100, 2100, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2101, 2101, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2102, 2102, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2103, 2103, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2104, 2104, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2105, 2105, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2106, 2106, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2107, 2107, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2108, 2108, UPPER('date2'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2109, 2109, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2110, 2110, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2111, 2111, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2112, 2112, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2113, 2113, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2114, 2114, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2115, 2115, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2116, 2116, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2117, 2117, UPPER('date2'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2118, 2118, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2119, 2119, UPPER('date'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2120, 2120, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2121, 2121, 'owner_relationship');
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2122, 2122, 'person');

-- Owner relationship --
INSERT INTO `sequence` (`sequence_name`, `sequence_value`) VALUES ('owner_relationship',1), ('owner_relationship_string_culture',1);
insert into `string_culture`(`id`, `locale_id`, `value`) values (2200, 1, 'Отношение к владельцу'), (2200, 2, 'Отношение к владельцу');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (2200, 'owner_relationship', 2200, '');
insert into `string_culture`(`id`, `locale_id`, `value`) values (2201, 1, UPPER('Наименование')), (2201, 2, UPPER('Наименование'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (2200, 2200, 1, 2201, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (2200, 2200, UPPER('string_culture'));

INSERT INTO `owner_relationship`(`object_id`) VALUES (1),(2),(3),(4),(5),(6),(7);
INSERT INTO `owner_relationship_string_culture`(`id`, `locale_id`, `value`) VALUES
(1, 1, UPPER('владелец')), (1, 2,UPPER('владелец')),
(2, 1, UPPER('ответственный квартиросьемщик')), (2, 2,UPPER('ответственный квартиросьемщик')),
(3, 1, UPPER('дети')), (3, 2, UPPER('дети')),
(4, 1, UPPER('гость')), (4, 2, UPPER('гость')),
(5, 1, UPPER('муж')), (5, 2, UPPER('муж')),
(6, 1, UPPER('жена')), (6, 2, UPPER('жена')),
(7, 1, UPPER('дальний родственник')), (7, 2, UPPER('дальний родственник'));
INSERT INTO `owner_relationship_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES
(1,1,2200,1,2200),(1,2,2200,2,2200),(1,3,2200,3,2200),(1,4,2200,4,2200),(1,5,2200,5,2200),(1,6,2200,6,2200),(1,7,2200,7,2200);
