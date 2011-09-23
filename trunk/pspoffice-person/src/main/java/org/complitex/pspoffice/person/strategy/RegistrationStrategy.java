/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.pspoffice.person.strategy;

import static com.google.common.collect.ImmutableMap.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.strategy.Strategy;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.pspoffice.ownerrelationship.strategy.OwnerRelationshipStrategy;
import org.complitex.pspoffice.person.strategy.entity.ApartmentCard;
import org.complitex.pspoffice.person.strategy.entity.Person;
import org.complitex.pspoffice.person.strategy.entity.Registration;
import org.complitex.pspoffice.registration_type.strategy.RegistrationTypeStrategy;
import org.complitex.template.web.security.SecurityRole;

/**
 *
 * @author Artem
 */
@Stateless
public class RegistrationStrategy extends Strategy {

    private static final String REGISTRATION_MAPPING = RegistrationStrategy.class.getPackage().getName() + ".Registration";
    /**
     * Attribute type ids
     */
    public static final long ARRIVAL_COUNTRY = 2100;
    public static final long ARRIVAL_REGION = 2101;
    public static final long ARRIVAL_DISTRICT = 2102;
    public static final long ARRIVAL_CITY = 2103;
    public static final long ARRIVAL_STREET = 2104;
    public static final long ARRIVAL_BUILDING_NUMBER = 2105;
    public static final long ARRIVAL_BUILDING_CORP = 2106;
    public static final long ARRIVAL_APARTMENT = 2107;
    public static final long ARRIVAL_DATE = 2108;
    public static final long DEPARTURE_COUNTRY = 2109;
    public static final long DEPARTURE_REGION = 2110;
    public static final long DEPARTURE_DISTRICT = 2111;
    public static final long DEPARTURE_CITY = 2112;
    public static final long DEPARTURE_STREET = 2113;
    public static final long DEPARTURE_BUILDING_NUMBER = 2114;
    public static final long DEPARTURE_BUILDING_CORP = 2115;
    public static final long DEPARTURE_APARTMENT = 2116;
    public static final long DEPARTURE_DATE = 2117;
    public static final long DEPARTURE_REASON = 2118;
    public static final long REGISTRATION_DATE = 2119;
    public static final long REGISTRATION_TYPE = 2120;
    public static final long OWNER_RELATIONSHIP = 2121;
    public static final long PERSON = 2122;
    @EJB
    private OwnerRelationshipStrategy ownerRelationshipStrategy;
    @EJB
    private PersonStrategy personStrategy;
    @EJB
    private RegistrationTypeStrategy registrationTypeStrategy;
    @EJB
    private ApartmentCardStrategy apartmentCardStrategy;

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional
    @Override
    public Registration findById(long id, boolean runAsAdmin) {
        return findById(id, runAsAdmin, true, true, true);
    }

    @Transactional
    private Registration findById(long id, boolean runAsAdmin, boolean loadPerson, boolean loadOwnerRelationship,
            boolean loadRegistrationType) {
        DomainObject registrationObject = super.findById(id, runAsAdmin);
        if (registrationObject == null) {
            return null;
        }
        Registration registration = new Registration(registrationObject);
        if (loadPerson) {
            loadPerson(registration);
        }
        if (loadOwnerRelationship) {
            loadOwnerRelationship(registration);
        }
        if (loadRegistrationType) {
            loadRegistrationType(registration);
        }
        return registration;
    }

    @Transactional
    private void loadPerson(Registration registration) {
        long personId = registration.getAttribute(PERSON).getValueId();
        Person person = personStrategy.findPersonById(personId, true, true, false, false);
        registration.setPerson(person);
    }

    @Transactional
    private void loadOwnerRelationship(Registration registration) {
        long ownerRelationshipId = registration.getAttribute(OWNER_RELATIONSHIP).getValueId();
        DomainObject ownerRelationship = ownerRelationshipStrategy.findById(ownerRelationshipId, true);
        registration.setOwnerRelationship(ownerRelationship);
    }

    @Transactional
    private void loadRegistrationType(Registration registration) {
        long registrationTypeId = registration.getAttribute(REGISTRATION_TYPE).getValueId();
        DomainObject registrationType = registrationTypeStrategy.findById(registrationTypeId, true);
        registration.setRegistrationType(registrationType);
    }

    @Transactional
    private Registration findFinishedRegistration(long objectId, boolean loadPerson, boolean loadOwnerRelationship,
            boolean loadRegistrationType) {
        DomainObjectExample example = new DomainObjectExample(objectId);
        example.setTable(getEntityTable());
        example.setStartDate(DateUtil.getCurrentDate());

        DomainObject registrationObject = (DomainObject) sqlSession().selectOne(DOMAIN_OBJECT_NAMESPACE + "." + FIND_HISTORY_OBJECT_OPERATION, example);
        if (registrationObject == null) {
            return null;
        }
        List<Attribute> historyAttributes = loadHistoryAttributes(objectId, DateUtil.justBefore(registrationObject.getEndDate()));
        loadStringCultures(historyAttributes);
        registrationObject.setAttributes(historyAttributes);
        updateStringsForNewLocales(registrationObject);

        Registration registration = new Registration(registrationObject);
        if (loadPerson) {
            loadPerson(registration);
        }
        if (loadOwnerRelationship) {
            loadOwnerRelationship(registration);
        }
        if (loadRegistrationType) {
            loadRegistrationType(registration);
        }
        return registration;
    }

    @Transactional
    public Registration findRegistrationById(long id, boolean runAsAdmin, boolean loadPerson, boolean loadOwnerRelationship,
            boolean loadRegistrationType) {
        Registration registration = findById(id, runAsAdmin, loadPerson, loadOwnerRelationship, loadRegistrationType);
        if (registration == null) {
            //find history registration
            registration = findFinishedRegistration(id, loadPerson, loadOwnerRelationship, loadRegistrationType);
        }
        return registration;
    }

    @Override
    public Class<? extends WebPage> getEditPage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PageParameters getEditPageParams(Long objectId, Long parentId, String parentEntity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getEntityTable() {
        return "registration";
    }

    @Override
    public Class<? extends WebPage> getHistoryPage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PageParameters getHistoryPageParams(long objectId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class<? extends WebPage> getListPage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PageParameters getListPageParams() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getEditRoles() {
        return new String[]{SecurityRole.AUTHORIZED};
    }

    @Override
    public Registration newInstance() {
        return new Registration(super.newInstance());
    }

    @Override
    public String[] getDescriptionRoles() {
        return new String[]{SecurityRole.PERSON_MODULE_DESCRIPTION_EDIT};
    }

    @Override
    public Page getObjectNotFoundPage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional
    public String checkOwner(long apartmentCardId, Long ownerRelationshipId, long personId, Locale locale) {
        if (ownerRelationshipId == null || !ownerRelationshipId.equals(OwnerRelationshipStrategy.OWNER)) {
            return null;
        }

        ApartmentCard apartmentCard = apartmentCardStrategy.findById(apartmentCardId, true, false, false, false);
        long ownerId = apartmentCard.getAttribute(ApartmentCardStrategy.OWNER).getValueId();
        if (ownerId != personId) {
            Person owner = personStrategy.findPersonById(ownerId, true, true, false, false);
            return personStrategy.displayDomainObject(owner, locale);
        } else {
            return null;
        }
    }

    @Transactional
    public boolean validateDuplicatePerson(long apartmentCardId, long personId) {
        Map<String, Long> params = of("apartmentCardRegistrationAT", ApartmentCardStrategy.REGISTRATIONS,
                "registrationPersonAT", PERSON, "personId", personId, "apartmentCardId", apartmentCardId);
        return sqlSession().selectOne(REGISTRATION_MAPPING + ".validateDuplicatePerson", params) == null;
    }

    @Override
    public String[] getListRoles() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
