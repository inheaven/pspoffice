/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.pspoffice.person.strategy.web.edit;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.text.MessageFormat;
import java.util.Collection;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.*;
import org.complitex.dictionary.entity.description.Entity;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.service.StringCultureBean;
import static org.complitex.dictionary.web.component.DomainObjectInputPanel.*;
import static org.complitex.dictionary.strategy.web.DomainObjectAccessUtil.*;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.list.AjaxRemovableListView;
import org.complitex.dictionary.web.component.name.FullNamePanel;
import org.complitex.dictionary.web.component.scroll.ScrollToElementUtil;
import org.complitex.dictionary.web.component.search.SearchComponent;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.pspoffice.person.registration.report.web.RegistrationStopCouponPage;
import org.complitex.pspoffice.person.strategy.PersonStrategy;
import org.complitex.pspoffice.person.strategy.RegistrationStrategy;
import static org.complitex.pspoffice.person.strategy.PersonStrategy.*;
import org.complitex.pspoffice.person.strategy.entity.Person;
import org.complitex.pspoffice.person.strategy.entity.Registration;

/**
 *
 * @author Artem
 */
public final class PersonInputPanel extends Panel {

    private static final String REGISTRATION_PANEL_ID = "registrationPanel";
    private static final String REGISTRATION_FOCUS_JS = "$('#" + REGISTRATION_PANEL_ID + " input[type=\"text\"]:enabled:first').focus()";
    @EJB
    private StringCultureBean stringBean;
    @EJB
    private PersonStrategy personStrategy;
    @EJB
    private RegistrationStrategy registrationStrategy;
    private Person person;
    private RegistrationInputPanel registrationInputPanel;
    private Date date;
    private FeedbackPanel messages;

    public PersonInputPanel(String id, Person person, Date date) {
        super(id);
        this.person = person;
        this.date = date;
        init();
    }

    public PersonInputPanel(String id, Person person, FeedbackPanel messages) {
        super(id);
        this.person = person;
        this.messages = messages;
        init();
    }

    private boolean isNew() {
        return person.getId() == null;
    }

    private boolean isHistory() {
        return date != null;
    }

    private void init() {
        //full name:
        FullNamePanel fullNamePanel = new FullNamePanel("fullNamePanel", newNameModel(FIRST_NAME), newNameModel(MIDDLE_NAME),
                newNameModel(LAST_NAME));
        fullNamePanel.setEnabled(!isHistory() && canEdit(null, personStrategy.getEntityTable(), person));
        add(fullNamePanel);

        Entity entity = personStrategy.getEntity();

        //registration panel:
        Label registrationLabel = new Label("registrationLabel",
                labelModel(entity.getAttributeType(REGISTRATION).getAttributeNames(), getLocale()));
        registrationLabel.setOutputMarkupId(true);
        final String registrationLabelMarkupId = registrationLabel.getMarkupId();
        add(registrationLabel);
        Form registrationForm = new Form("registrationForm");
        add(registrationForm);
        final WebMarkupContainer registrationContainer = new WebMarkupContainer("registrationContainer");
        registrationContainer.setOutputMarkupId(true);
        registrationForm.add(registrationContainer);

        final Link<Void> registrationStopCouponLink = new Link("registrationStopCouponLink") {

            @Override
            public void onClick() {
                setResponsePage(new RegistrationStopCouponPage(person));
            }
        };
        registrationStopCouponLink.setVisible(false);
        registrationContainer.add(registrationStopCouponLink);

        if (person.getRegistration() != null) {
            registrationInputPanel = new RegistrationInputPanel(REGISTRATION_PANEL_ID, person.getRegistration(), date);
            registrationContainer.add(registrationInputPanel);
        } else {
            registrationContainer.add(new NoRegistrationPanel(REGISTRATION_PANEL_ID));
        }

        final WebMarkupContainer registrationControlContainer = new WebMarkupContainer("registrationControlContainer");
        registrationControlContainer.setOutputMarkupPlaceholderTag(true);
        registrationContainer.add(registrationControlContainer);
        AjaxLink<Void> addRegistration = new AjaxLink<Void>("addRegistration") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                person.setRegistration(registrationStrategy.newInstance());
                registrationInputPanel = new RegistrationInputPanel(REGISTRATION_PANEL_ID, person.getRegistration(), date);
                updateRegistrationContainer(registrationContainer, registrationControlContainer, registrationInputPanel,
                        target, registrationLabelMarkupId);
            }
        };
        addRegistration.setVisible(!isHistory() && (person.getRegistration() == null)
                && canEdit(null, personStrategy.getEntityTable(), person));
        registrationControlContainer.add(addRegistration);
        AjaxSubmitLink changeRegistration = new AjaxSubmitLink("changeRegistration", registrationForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (registrationInputPanel.validate()) {
                    registrationInputPanel.beforePersist();
                    Registration changedRegistration = registrationStrategy.newInstance();
                    person.setChangedRegistration(changedRegistration);
                    changedRegistration.setPerson(person);
                    registrationInputPanel = new RegistrationInputPanel(REGISTRATION_PANEL_ID, person.getChangedRegistration(), date);
                    registrationStopCouponLink.setVisible(true);
                    updateRegistrationContainer(registrationContainer, registrationControlContainer, registrationInputPanel,
                            target, registrationLabelMarkupId);
                } else {
                    target.appendJavascript(ScrollToElementUtil.scrollTo(messages.getMarkupId()));
                }
                target.addComponent(messages);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.appendJavascript(ScrollToElementUtil.scrollTo(messages.getMarkupId()));
                target.addComponent(messages);
            }
        };
        changeRegistration.setVisible(!isHistory() && (person.getRegistration() != null)
                && !isNew() && canEdit(null, personStrategy.getEntityTable(), person));
        registrationControlContainer.add(changeRegistration);
        AjaxSubmitLink stopRegistration = new AjaxSubmitLink("stopRegistration", registrationForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (registrationInputPanel.validate()) {
                    registrationInputPanel.beforePersist();
                    person.setRegistrationStopped(true);
                    registrationStopCouponLink.setVisible(true);
                    updateRegistrationContainer(registrationContainer, registrationControlContainer,
                            new NoRegistrationPanel(REGISTRATION_PANEL_ID), target, registrationLabelMarkupId);
                } else {
                    target.appendJavascript(ScrollToElementUtil.scrollTo(messages.getMarkupId()));
                }
                target.addComponent(messages);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.appendJavascript(ScrollToElementUtil.scrollTo(messages.getMarkupId()));
                target.addComponent(messages);
            }
        };
        stopRegistration.setVisible(!isHistory() && (person.getRegistration() != null)
                && !isNew() && canEdit(null, personStrategy.getEntityTable(), person));
        registrationControlContainer.add(stopRegistration);

        //system attributes:
        initSystemAttributeInput(this, "birthCountry", BIRTH_COUNTRY, true);
        initSystemAttributeInput(this, "birthRegion", BIRTH_REGION, true);
        initSystemAttributeInput(this, "birthDistrict", BIRTH_DISTRICT, false);
        initSystemAttributeInput(this, "birthCity", BIRTH_CITY, true);
        initSystemAttributeInput(this, "birthDate", BIRTH_DATE, true);

        //passport info
        WebMarkupContainer passportContainer = new WebMarkupContainer("passportContainer");
        passportContainer.setVisible(isPassportContainerVisible());
        add(passportContainer);
        initSystemAttributeInput(passportContainer, "passportSerialNumber", PASSPORT_SERIAL_NUMBER, false);
        initSystemAttributeInput(passportContainer, "passportNumber", PASSPORT_NUMBER, false);
        initSystemAttributeInput(passportContainer, "passportAcquisitionDate", PASSPORT_ACQUISITION_DATE, false);
        initSystemAttributeInput(passportContainer, "passportAcquisitionOrganization", PASSPORT_ACQUISITION_ORGANIZATION, false);

        // birth certificate info
        WebMarkupContainer birthCertificateContainer = new WebMarkupContainer("birthCertificateContainer");
        birthCertificateContainer.setVisible(isBirthCertificateContainerVisible());
        add(birthCertificateContainer);
        initSystemAttributeInput(birthCertificateContainer, "birthCertificateInfo", BIRTH_CERTIFICATE_INFO, false);
        initSystemAttributeInput(birthCertificateContainer, "birthCertificateAcquisitionDate", BIRTH_CERTIFICATE_ACQUISITION_DATE, false);
        initSystemAttributeInput(birthCertificateContainer, "birthCertificateAcquisitionOrganization", BIRTH_CERTIFICATE_ACQUISITION_ORGANIZATION, false);

        initSystemAttributeInput(this, "gender", GENDER, false);
        initSystemAttributeInput(this, "nationality", NATIONALITY, false);
        initSystemAttributeInput(this, "ukraineCitizenship", UKRAINE_CITIZENSHIP, false);
        initSystemAttributeInput(this, "jobInfo", JOB_INFO, false);
        initSystemAttributeInput(this, "militaryServiceRelation", MILITARY_SERVISE_RELATION, false);

        //user attributes:
        List<Long> userAttributeTypeIds = newArrayList(transform(filter(entity.getEntityAttributeTypes(),
                new Predicate<EntityAttributeType>() {

                    @Override
                    public boolean apply(EntityAttributeType attributeType) {
                        return !attributeType.isSystem();
                    }
                }),
                new Function<EntityAttributeType, Long>() {

                    @Override
                    public Long apply(EntityAttributeType attributeType) {
                        return attributeType.getId();
                    }
                }));

        List<Attribute> userAttributes = newArrayList();
        for (Long attributeTypeId : userAttributeTypeIds) {
            Attribute userAttribute = person.getAttribute(attributeTypeId);
            if (userAttribute != null) {
                userAttributes.add(userAttribute);
            }
        }

        ListView<Attribute> userAttributesView = new ListView<Attribute>("userAttributesView", userAttributes) {

            @Override
            protected void populateItem(ListItem<Attribute> item) {
                long userAttributeTypeId = item.getModelObject().getAttributeTypeId();
                initAttributeInput(item, userAttributeTypeId, false);
            }
        };
        add(userAttributesView);

        //children
        WebMarkupContainer childrenFieldsetContainer = new WebMarkupContainer("childrenFieldsetContainer");
        add(childrenFieldsetContainer);
        childrenFieldsetContainer.add(new Label("childrenLabel",
                labelModel(entity.getAttributeType(CHILDREN).getAttributeNames(), getLocale())));
        final WebMarkupContainer childrenContainer = new WebMarkupContainer("childrenContainer");
        childrenContainer.setOutputMarkupId(true);
        childrenFieldsetContainer.add(childrenContainer);
        ListView<Person> children = new AjaxRemovableListView<Person>("children", person.getChildren()) {

            @Override
            protected void populateItem(ListItem<Person> item) {
                final WebMarkupContainer fakeContainer = new WebMarkupContainer("fakeContainer");
                item.add(fakeContainer);
                item.add(new Label("label", new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        return MessageFormat.format(getString("children_number"), getCurrentIndex(fakeContainer) + 1);
                    }
                }));

                SearchComponentState searchComponentState = new SearchComponentState() {

                    @Override
                    public DomainObject put(String entity, DomainObject child) {
                        super.put(entity, child);
                        int index = getCurrentIndex(fakeContainer);

                        person.setChild(index, (Person) child);

                        return child;
                    }
                };
                Person child = item.getModelObject();
                if (child != null) {
                    searchComponentState.put(personStrategy.getEntityTable(), child);
                }

                SearchComponent searchChildComponent = new SearchComponent("searchChildComponent", searchComponentState,
                        ImmutableList.of(personStrategy.getEntityTable()), null, ShowMode.ACTIVE,
                        !isHistory() && canEdit(null, personStrategy.getEntityTable(), person));
                item.add(searchChildComponent);

                addRemoveLink("removeChild", item, null, childrenContainer).
                        setVisible(!isHistory() && canEdit(null, personStrategy.getEntityTable(), person));
            }
        };
        AjaxLink<Void> addChild = new AjaxLink<Void>("addChild") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                Person newChild = null;
                person.addChild(newChild);
                target.addComponent(childrenContainer);
            }
        };
        addChild.setVisible(!isHistory() && canEdit(null, personStrategy.getEntityTable(), person));
        childrenFieldsetContainer.add(addChild);
        childrenContainer.add(children);
        if (isHistory() && person.getChildren().isEmpty()) {
            childrenFieldsetContainer.setVisible(false);
        }
    }

    private IModel<Long> newNameModel(final long attributeTypeId) {
        return new Model<Long>() {

            @Override
            public Long getObject() {
                return person.getAttribute(attributeTypeId).getValueId();
            }

            @Override
            public void setObject(Long object) {
                person.getAttribute(attributeTypeId).setValueId(object);
            }
        };
    }

    private void updateRegistrationContainer(WebMarkupContainer registrationContainer, WebMarkupContainer registrationControlContainer,
            Panel registrationPanel, AjaxRequestTarget target, String scrollToElementId) {
        registrationContainer.get(REGISTRATION_PANEL_ID).replaceWith(registrationPanel);
        registrationControlContainer.setVisible(false);
        target.appendJavascript(ScrollToElementUtil.scrollTo(scrollToElementId));
        target.appendJavascript(REGISTRATION_FOCUS_JS);
        target.addComponent(registrationContainer);
    }

    private void initSystemAttributeInput(MarkupContainer parent, String id, long attributeTypeId, boolean showIfMissing) {
        WebMarkupContainer container = new WebMarkupContainer(id + "Container");
        parent.add(container);
        initAttributeInput(container, attributeTypeId, showIfMissing);
    }

    private boolean isPassportContainerVisible() {
        return !(isHistory() && (person.getAttribute(PASSPORT_SERIAL_NUMBER) == null) && (person.getAttribute(PASSPORT_NUMBER) == null)
                && (person.getAttribute(PASSPORT_ACQUISITION_ORGANIZATION) == null)
                && (person.getAttribute(PASSPORT_ACQUISITION_DATE) == null));
    }

    private boolean isBirthCertificateContainerVisible() {
        return !(isHistory() && (person.getAttribute(BIRTH_CERTIFICATE_INFO) == null)
                && (person.getAttribute(BIRTH_CERTIFICATE_ACQUISITION_DATE) == null)
                && (person.getAttribute(BIRTH_CERTIFICATE_ACQUISITION_ORGANIZATION) == null));
    }

    private void initAttributeInput(MarkupContainer parent, long attributeTypeId, boolean showIfMissing) {
        final EntityAttributeType attributeType = personStrategy.getEntity().getAttributeType(attributeTypeId);

        //label
        parent.add(new Label("label", labelModel(attributeType.getAttributeNames(), getLocale())));

        //required container
        WebMarkupContainer requiredContainer = new WebMarkupContainer("required");
        requiredContainer.setVisible(attributeType.isMandatory());
        parent.add(requiredContainer);

        //input component
        Attribute attribute = person.getAttribute(attributeTypeId);
        if (attribute == null) {
            attribute = new Attribute();
            attribute.setLocalizedValues(stringBean.newStringCultures());
            attribute.setAttributeTypeId(attributeTypeId);
            parent.setVisible(showIfMissing);
        }
        parent.add(newInputComponent(personStrategy.getEntityTable(), null, person, attribute, getLocale(), isHistory()));
    }

    public void beforePersist() {
        if (registrationInputPanel != null) {
            registrationInputPanel.beforePersist();
        }
        updateChildrenAttributes();
    }

    private void updateChildrenAttributes() {
        person.getAttributes().removeAll(Collections2.filter(person.getAttributes(), new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                return attr.getAttributeTypeId().equals(CHILDREN);
            }
        }));
        long attributeId = 1;
        for (Person child : person.getChildren()) {
            Attribute childrenAttribute = new Attribute();
            childrenAttribute.setAttributeId(attributeId++);
            childrenAttribute.setAttributeTypeId(CHILDREN);
            childrenAttribute.setValueTypeId(CHILDREN);
            childrenAttribute.setValueId(child.getId());
            person.addAttribute(childrenAttribute);
        }
    }

    public boolean validate() {
        boolean childrenValid = validateChildren();
        boolean registrationValid = registrationInputPanel != null ? registrationInputPanel.validate() : true;
        return childrenValid && registrationValid;
    }

    private boolean validateChildren() {
        boolean valid = true;

        Collection<Person> nonNullChildren = newArrayList(filter(person.getChildren(), new Predicate<Person>() {

            @Override
            public boolean apply(Person child) {
                return child != null && child.getId() != null && child.getId() > 0;
            }
        }));
        if (nonNullChildren.size() != person.getChildren().size()) {
            error(getString("children_error"));
            valid = false;
        }

        Set<Long> childrenIds = newHashSet(transform(nonNullChildren, new Function<Person, Long>() {

            @Override
            public Long apply(Person child) {
                return child.getId();
            }
        }));

        if (!isNew()) {
            if (childrenIds.contains(person.getId())) {
                error(getString("references_themselves"));
                valid = false;
            }
        }

        if (childrenIds.size() != nonNullChildren.size()) {
            error(getString("children_duplicate"));
            valid = false;
        }
        return valid;
    }
}
