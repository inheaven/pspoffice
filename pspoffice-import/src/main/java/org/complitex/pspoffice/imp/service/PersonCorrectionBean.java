/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.pspoffice.imp.service;

import com.google.common.collect.ImmutableMap;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.converter.BooleanConverter;
import org.complitex.dictionary.converter.DateConverter;
import org.complitex.dictionary.converter.GenderConverter;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.Gender;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.util.CloneUtil;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.pspoffice.document.strategy.DocumentStrategy;
import org.complitex.pspoffice.document.strategy.entity.Document;
import org.complitex.pspoffice.document_type.strategy.DocumentTypeStrategy;
import org.complitex.pspoffice.imp.entity.PersonCorrection;
import org.complitex.pspoffice.imp.service.exception.TooManyResultsException;
import org.complitex.pspoffice.person.strategy.PersonStrategy;
import org.complitex.pspoffice.person.strategy.entity.Person;
import org.complitex.pspoffice.person.strategy.entity.PersonName.PersonNameType;
import org.complitex.pspoffice.person.strategy.service.PersonNameBean;

/**
 *
 * @author Artem
 */
@Stateless
public class PersonCorrectionBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = PersonCorrectionBean.class.getName();
    /**
     * Gender consts
     */
    private static final String MALE = "Ч";
    private static final String FEMALE = "Ж";
    /**
     * Ukraine citizenship const
     */
    private static final String UKRAINE_CITIZENSHIP_INDICATOR = "УКРАЇНА";
    @EJB
    private PersonNameBean personNameBean;
    @EJB
    private PersonStrategy personStrategy;
    @EJB
    private DocumentStrategy documentStrategy;

    public void insert(PersonCorrection personCorrection) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insert", personCorrection);
    }

    public PersonCorrection find(long id) {
        return (PersonCorrection) sqlSession().selectOne(MAPPING_NAMESPACE + ".findById", id);
    }

    public boolean exists() {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".exists") > 0;
    }

    public void cleanData() {
        sqlSession().delete(MAPPING_NAMESPACE + ".delete");
    }

    public void update(PersonCorrection personCorrection) {
        sqlSession().update(MAPPING_NAMESPACE + ".update", personCorrection);
    }

    public void clearProcessingStatus() {
        sqlSession().update(MAPPING_NAMESPACE + ".clearProcessingStatus");
    }

    public int countForProcessing() {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".countForProcessing", Utils.NONARCHIVE_INDICATOR);
    }

    public int archiveCount() {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".archiveCount", Utils.NONARCHIVE_INDICATOR);
    }

    public List<PersonCorrection> findForProcessing(int size) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findForProcessing",
                ImmutableMap.of("size", size, "NONARCHIVE_INDICATOR", Utils.NONARCHIVE_INDICATOR));
    }

    public Person findSystemPerson(PersonCorrection p) throws TooManyResultsException {
        List<Long> ids = sqlSession().selectList(MAPPING_NAMESPACE + ".findSystemPerson",
                ImmutableMap.builder().
                put("personIdAT", PersonStrategy.OLD_SYSTEM_PERSON_ID).
                put("localeId", Utils.UKRAINIAN_LOCALE_ID).
                put("lastName", p.getFam()).put("firstName", p.getIm()).put("middleName", p.getOt()).
                put("personId", p.getId()).
                build());
        if (ids.size() == 1) {
            return personStrategy.findById(ids.get(0), true, false, false, false);
        } else if (ids.isEmpty()) {
            return null;
        } else {
            throw new TooManyResultsException();
        }
    }

    private static String getBirthDateAsDateString(Date birthDate) {
        return new DateConverter().toString(birthDate);
    }

    public Person newSystemPerson(PersonCorrection pc, Date birthDate, String militaryServiceRelation) {
        Person p = personStrategy.newInstance();

        //ФИО
        setName(PersonNameType.LAST_NAME, PersonStrategy.LAST_NAME, p, pc.getFam());
        setName(PersonNameType.FIRST_NAME, PersonStrategy.FIRST_NAME, p, pc.getIm());
        setName(PersonNameType.MIDDLE_NAME, PersonStrategy.MIDDLE_NAME, p, pc.getOt());

        //Дата рождения
        Utils.setValue(p.getAttribute(PersonStrategy.BIRTH_DATE), getBirthDateAsDateString(birthDate));

        //Пол
        Gender gender = null;
        if (pc.getPol().equalsIgnoreCase(MALE)) {
            gender = Gender.MALE;
        } else if (pc.getPol().equalsIgnoreCase(FEMALE)) {
            gender = Gender.FEMALE;
        }
        if (gender == null) {
            throw new IllegalArgumentException("Person correction has invalid gender value: " + pc.getPol());
        }
        Utils.setValue(p.getAttribute(PersonStrategy.GENDER), new GenderConverter().toString(gender));

        //Гражданство Украины
        final boolean isUkraineCitizenship = UKRAINE_CITIZENSHIP_INDICATOR.equalsIgnoreCase(pc.getGrajd());
        Utils.setValue(p.getAttribute(PersonStrategy.UKRAINE_CITIZENSHIP),
                new BooleanConverter().toString(isUkraineCitizenship));

        //Место рождения
        Utils.setValue(p.getAttribute(PersonStrategy.BIRTH_COUNTRY), pc.getNkra());
        Utils.setValue(p.getAttribute(PersonStrategy.BIRTH_REGION), pc.getNobl());
        Utils.setValue(p.getAttribute(PersonStrategy.BIRTH_DISTRICT), pc.getNrayon());
        Utils.setValue(p.getAttribute(PersonStrategy.BIRTH_CITY), pc.getNmisto());

        //Документ
        Long documentTypeId = null;
        if (String.valueOf(ReferenceDataCorrectionBean.PASSPORT).equalsIgnoreCase(pc.getIddok())) {
            documentTypeId = DocumentTypeStrategy.PASSPORT;
        } else if (String.valueOf(ReferenceDataCorrectionBean.BIRTH_CERTIFICATE).equalsIgnoreCase(pc.getIddok())) {
            documentTypeId = DocumentTypeStrategy.BIRTH_CERTIFICATE;
        }
        if (documentTypeId == null) {
            throw new IllegalArgumentException("Person correction has invalid document type id: " + pc.getIddok());
        }
        Document d = documentStrategy.newInstance(documentTypeId);
        Utils.setValue(d.getAttribute(DocumentStrategy.DOCUMENT_SERIA), pc.getDokseria());
        Utils.setValue(d.getAttribute(DocumentStrategy.DOCUMENT_NUMBER), pc.getDoknom());
        Utils.setValue(d.getAttribute(DocumentStrategy.ORGANIZATION_ISSUED), pc.getDokvidan());
        Utils.setValue(d.getAttribute(DocumentStrategy.DATE_ISSUED), pc.getDokdatvid());
        p.setDocument(d);

        //отношение к воиской обязанности
        if (militaryServiceRelation != null) {
            Utils.setValue(p.getAttribute(PersonStrategy.MILITARY_SERVICE_RELATION), militaryServiceRelation);
        }

        //ID в файле импорта
        Utils.setValue(p.getAttribute(PersonStrategy.OLD_SYSTEM_PERSON_ID), String.valueOf(pc.getId()));

        return p;
    }

    private void setName(PersonNameType personNameType, long personNameAttributeTypeId, DomainObject person, String name) {
        for (Attribute nameAttribute : person.getAttributes(personNameAttributeTypeId)) {
            nameAttribute.setValueId(personNameBean.saveIfNotExists(personNameType, name,
                    nameAttribute.getAttributeId()).getId());
        }
    }

    public static boolean isGenderValid(String gender) {
        return !Strings.isEmpty(gender) && (gender.equalsIgnoreCase(FEMALE) || gender.equalsIgnoreCase(MALE));
    }

    public static boolean isBirthDateValid(String birthDate) {
        return !Strings.isEmpty(birthDate) && DateUtil.asDate(birthDate, Utils.DATE_PATTERN) != null;
    }

    public static boolean isSupportedDocumentType(String documentType) {
        return String.valueOf(ReferenceDataCorrectionBean.PASSPORT).equalsIgnoreCase(documentType)
                || String.valueOf(ReferenceDataCorrectionBean.BIRTH_CERTIFICATE).equalsIgnoreCase(documentType);
    }

    public static boolean isDocumentDataValid(String documentType, String seria, String number) {
        return !Strings.isEmpty(documentType) && !Strings.isEmpty(seria) && !Strings.isEmpty(number);
    }

    public static boolean isParentDataValid(String idbud, String kv, String parentnom) {
        return !Strings.isEmpty(idbud) && !Strings.isEmpty(kv) && parentnom != null && StringUtil.isNumeric(parentnom);
    }

    public void addChild(long personId, long childId, String birthDate, Date updateDate) {
        if (DateUtil.isValidDateInterval(updateDate, DateUtil.asDate(birthDate, Utils.DATE_PATTERN),
                PersonStrategy.AGE_THRESHOLD)) {
            return;
        }

        Person person = personStrategy.findById(personId, true, false, false, false);
        Person newPerson = CloneUtil.cloneObject(person);

        List<Attribute> childrenAttributes = newPerson.getAttributes(PersonStrategy.CHILDREN);
        for (Attribute childAttribute : childrenAttributes) {
            if (childAttribute.getValueId().equals(childId)) {
                return;
            }
        }
        newPerson.addAttribute(newChildAttribute(childrenAttributes.size() + 1, childId));
        personStrategy.update(person, newPerson, updateDate);
    }

    private Attribute newChildAttribute(long attributeId, long childId) {
        Attribute childAttribute = new Attribute();
        childAttribute.setAttributeId(attributeId);
        childAttribute.setAttributeTypeId(PersonStrategy.CHILDREN);
        childAttribute.setValueTypeId(PersonStrategy.CHILDREN);
        childAttribute.setValueId(childId);
        return childAttribute;
    }

    public List<Long> findSystemParent(String idbud, String kv, String nom) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findSystemParent",
                ImmutableMap.of("NONARCHIVE_INDICATOR", Utils.NONARCHIVE_INDICATOR,
                "idbud", idbud, "kv", kv, "nom", nom));
    }

    public List<PersonCorrection> findChildren(int size) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findChildren",
                ImmutableMap.of("size", size, "NONARCHIVE_INDICATOR", Utils.NONARCHIVE_INDICATOR));
    }
}
