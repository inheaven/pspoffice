/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.pspoffice.person.strategy.web.list.apartment_card.grid;

import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.service.AddressRendererBean;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.back.BackInfo;
import org.complitex.dictionary.web.component.back.BackInfoManager;
import org.complitex.dictionary.web.component.back.BookmarkableBackInfo;
import org.complitex.pspoffice.person.strategy.ApartmentCardStrategy;
import org.complitex.pspoffice.person.strategy.PersonStrategy;
import org.complitex.pspoffice.person.strategy.entity.ApartmentCard;
import org.complitex.pspoffice.person.strategy.entity.grid.RegistrationsGridEntity;
import org.complitex.pspoffice.person.strategy.entity.grid.RegistrationsGridFilter;
import org.complitex.pspoffice.person.strategy.service.RegistrationsGridBean;
import org.complitex.pspoffice.person.strategy.web.list.apartment_card.ApartmentCardSearch;
import org.complitex.template.strategy.TemplateStrategy;
import org.complitex.template.web.pages.ListPage;
import org.complitex.template.web.security.SecurityRole;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class RegistrationsGrid extends ListPage {

    public static final String APARTMENT_CARD_PARAM = "apartment_card_id";
    public static final String BACK_PARAM = "registrations_grid_back";
    private static final String PAGE_SESSION_KEY = "registrations_grid_page";
    @EJB
    private RegistrationsGridBean registrationsGridBean;
    @EJB
    private AddressRendererBean addressRendererBean;
    @EJB
    private ApartmentCardStrategy apartmentCardStrategy;
    @EJB
    private PersonStrategy personStrategy;

    public RegistrationsGrid(PageParameters parameters) {
        final long apartmentCardId = parameters.getAsLong(APARTMENT_CARD_PARAM);
        final String backInfoSessionKey = parameters.getString(BACK_PARAM);

        final ApartmentCard apartmentCard = apartmentCardStrategy.findById(apartmentCardId, true);

        IModel<String> labelModel = new StringResourceModel("label", null,
                new Object[]{addressRendererBean.displayAddress(ApartmentCardStrategy.getAddressEntity(apartmentCard),
                    apartmentCard.getAddressId(), getLocale()),
                    apartmentCardId});

        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        //Filter
        final RegistrationsGridFilter filter = registrationsGridBean.newFilter(apartmentCard, getLocale());

        final List<RegistrationsGridEntity> registrationsGridEntities = registrationsGridBean.find(filter);

        //List View
        ListView<RegistrationsGridEntity> registrations =
                new ListView<RegistrationsGridEntity>("registrations", registrationsGridEntities) {

                    @Override
                    protected void populateItem(ListItem<RegistrationsGridEntity> item) {
                        final RegistrationsGridEntity registrationsGridEntity = item.getModelObject();

                        //order
                        item.add(new Label("order", StringUtil.valueOf(item.getIndex() + 1)));

                        //person
                        Link<Void> personLink = new Link<Void>("personLink") {

                            @Override
                            public void onClick() {
                                PageParameters params = personStrategy.getEditPageParams(registrationsGridEntity.getPersonId(), null, null);
                                BackInfoManager.put(this, PAGE_SESSION_KEY, gridBackInfo(apartmentCardId, backInfoSessionKey));
                                params.put(TemplateStrategy.BACK_INFO_SESSION_KEY, PAGE_SESSION_KEY);
                                setResponsePage(personStrategy.getEditPage(), params);
                            }
                        };
                        item.add(personLink);
                        personLink.add(new Label("personName", registrationsGridEntity.getPersonName()));

                        //person birth date
                        item.add(new Label("personBirthDate", registrationsGridEntity.getPersonBirthDate()));

                        //registration date
                        item.add(new Label("registrationDate", registrationsGridEntity.getRegistrationDate()));

                        //registration type
                        item.add(new Label("registrationType", registrationsGridEntity.getRegistrationType()));

                        //owner relationship
                        item.add(new Label("ownerRelationship", registrationsGridEntity.getOwnerRelationship()));
                    }
                };

        add(registrations);

        Link<Void> backSearch = new Link<Void>("backSearch") {

            @Override
            public void onClick() {
                setResponsePage(ApartmentCardSearch.class);
            }
        };
        add(backSearch);

        final BackInfo backInfo = !Strings.isEmpty(backInfoSessionKey) ? BackInfoManager.get(getPage(), backInfoSessionKey) : null;
        Link<Void> back = new Link<Void>("back") {

            @Override
            public void onClick() {
                backInfo.back(this);
            }
        };
        back.setVisible(backInfo != null);
        add(back);
    }

    private static BackInfo gridBackInfo(long apartmentCardId, String backInfoSessionKey) {
        PageParameters backPageParams = new PageParameters();
        backPageParams.put(APARTMENT_CARD_PARAM, apartmentCardId);
        backPageParams.put(BACK_PARAM, backInfoSessionKey);
        return new BookmarkableBackInfo(RegistrationsGrid.class, backPageParams);
    }
}
