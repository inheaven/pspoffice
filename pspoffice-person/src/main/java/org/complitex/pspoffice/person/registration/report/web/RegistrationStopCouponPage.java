/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.pspoffice.person.registration.report.web;

import static com.google.common.collect.Lists.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.feedback.FeedbackMessage;
import static org.apache.wicket.feedback.FeedbackMessage.*;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionary.entity.Gender;
import org.complitex.dictionary.util.OSInfoUtil;
import org.complitex.dictionary.web.component.type.BooleanPanel;
import org.complitex.dictionary.web.component.type.Date2Panel;
import org.complitex.dictionary.web.component.type.GenderPanel;
import org.complitex.pspoffice.person.download.RegistrationStopCouponDownload;
import org.complitex.pspoffice.person.registration.report.entity.RegistrationStopCoupon;
import org.complitex.pspoffice.person.registration.report.exception.UnregisteredPersonException;
import org.complitex.pspoffice.person.registration.report.service.RegistrationStopCouponBean;
import org.complitex.pspoffice.person.strategy.entity.Person;
import org.complitex.pspoffice.report.web.ReportDownloadPanel;
import org.complitex.template.web.component.toolbar.SaveButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class RegistrationStopCouponPage extends FormTemplatePage {

    private static final Logger log = LoggerFactory.getLogger(RegistrationStopCouponPage.class);
    @EJB
    private RegistrationStopCouponBean couponBean;
    private ReportDownloadPanel reportDownloadPanel;

    private class FieldLabel extends Label {

        public FieldLabel(final String fieldName) {
            super(fieldName + "Label", new AbstractReadOnlyModel<String>() {

                @Override
                public String getObject() {
                    return RegistrationStopCouponPage.this.getString(fieldName);
                }
            });
        }

        public IModel<String> getModel() {
            return (IModel<String>) getDefaultModel();
        }
    }

    private class MessagesFragment extends Fragment {

        private Collection<FeedbackMessage> messages;

        public MessagesFragment(String id, Collection<FeedbackMessage> messages) {
            super(id, "messages", RegistrationStopCouponPage.this);
            this.messages = messages;
            add(new FeedbackPanel("messages"));
        }

        @Override
        protected void onBeforeRender() {
            super.onBeforeRender();
            for (FeedbackMessage message : messages) {
                getSession().getFeedbackMessages().add(message);
            }
        }
    }

    private class ReportFragment extends Fragment {

        public ReportFragment(String id, final RegistrationStopCoupon coupon) {
            super(id, "report", RegistrationStopCouponPage.this);
            add(new Label("label", new ResourceModel("label")));
            final FeedbackPanel messages = new FeedbackPanel("messages");
            add(messages);
            final CompoundPropertyModel<RegistrationStopCoupon> model = new CompoundPropertyModel<RegistrationStopCoupon>(coupon);
            Form<RegistrationStopCoupon> form = new Form<RegistrationStopCoupon>("form", model);
            add(form);
            form.add(new FieldLabel("lastName"));
            form.add(new TextField("lastName"));
            form.add(new FieldLabel("firstName"));
            form.add(new TextField("firstName"));
            form.add(new FieldLabel("middleName"));
            form.add(new TextField("middleName"));
            form.add(new FieldLabel("previousNames"));
            form.add(new TextArea("previousNames"));
            form.add(new FieldLabel("birthCountry"));
            form.add(new TextField("birthCountry"));
            form.add(new FieldLabel("birthRegion"));
            form.add(new TextField("birthRegion"));
            form.add(new FieldLabel("birthDistrict"));
            form.add(new TextField("birthDistrict"));
            form.add(new FieldLabel("birthCity"));
            form.add(new TextField("birthCity"));
            FieldLabel birthDateLabel = new FieldLabel("birthDate");
            form.add(birthDateLabel);
            form.add(new Date2Panel("birthDate", new PropertyModel<Date>(model, "birthDate"), false, birthDateLabel.getModel(), true));
            FieldLabel genderLabel = new FieldLabel("gender");
            form.add(genderLabel);
            form.add(new GenderPanel("gender", new PropertyModel<Gender>(model, "gender"), false, genderLabel.getModel(), true));
            form.add(new FieldLabel("addressCountry"));
            form.add(new TextField("addressCountry"));
            form.add(new FieldLabel("addressRegion"));
            form.add(new TextField("addressRegion"));
            form.add(new FieldLabel("addressDistrict"));
            form.add(new TextField("addressDistrict"));
            form.add(new FieldLabel("addressCity"));
            form.add(new TextField("addressCity"));
            form.add(new FieldLabel("addressStreet"));
            form.add(new TextField("addressStreet"));
            form.add(new FieldLabel("addressBuildingNumber"));
            form.add(new TextField("addressBuildingNumber"));
            form.add(new FieldLabel("addressBuildingCorp"));
            form.add(new TextField("addressBuildingCorp"));
            form.add(new FieldLabel("addressApartment"));
            form.add(new TextField("addressApartment"));
            form.add(new FieldLabel("registrationOrganization"));
            form.add(new TextField("registrationOrganization"));
            form.add(new FieldLabel("departureCountry"));
            form.add(new TextField("departureCountry"));
            form.add(new FieldLabel("departureRegion"));
            form.add(new TextField("departureRegion"));
            form.add(new FieldLabel("departureDistrict"));
            form.add(new TextField("departureDistrict"));
            form.add(new FieldLabel("departureCity"));
            form.add(new TextField("departureCity"));
            form.add(new FieldLabel("departureStreet"));
            form.add(new TextField("departureStreet"));
            form.add(new FieldLabel("departureBuildingNumber"));
            form.add(new TextField("departureBuildingNumber"));
            form.add(new FieldLabel("departureBuildingCorp"));
            form.add(new TextField("departureBuildingCorp"));
            form.add(new FieldLabel("departureApartment"));
            form.add(new TextField("departureApartment"));
            FieldLabel departureDateLabel = new FieldLabel("departureDate");
            form.add(departureDateLabel);
            form.add(new Date2Panel("departureDate", new PropertyModel<Date>(model, "departureDate"), false, departureDateLabel.getModel(), true));
            form.add(new FieldLabel("passportSerialNumber"));
            form.add(new TextField("passportSerialNumber"));
            form.add(new FieldLabel("passportNumber"));
            form.add(new TextField("passportNumber"));
            FieldLabel passportAcquisitionDateLabel = new FieldLabel("passportAcquisitionDate");
            form.add(passportAcquisitionDateLabel);
            form.add(new Date2Panel("passportAcquisitionDate", new PropertyModel<Date>(model, "passportAcquisitionDate"), false,
                    passportAcquisitionDateLabel.getModel(), true));
            form.add(new FieldLabel("passportAcquisitionOrganization"));
            form.add(new TextArea("passportAcquisitionOrganization"));
            form.add(new FieldLabel("birthCertificateInfo"));
            form.add(new TextField("birthCertificateInfo"));
            FieldLabel ukraineCitizenshipLabel = new FieldLabel("ukraineCitizenship");
            form.add(ukraineCitizenshipLabel);
            form.add(new BooleanPanel("ukraineCitizenship", new PropertyModel<Boolean>(model, "ukraineCitizenship"),
                    ukraineCitizenshipLabel.getModel(), true));
            form.add(new FieldLabel("childrenInfo"));
            form.add(new TextArea("childrenInfo"));
            form.add(new FieldLabel("additionalInfo"));
            form.add(new TextArea("additionalInfo"));
        }
    }

    public RegistrationStopCouponPage(Person person) {
        add(new Label("title", new ResourceModel("label")));
        Collection<FeedbackMessage> messages = newArrayList();
        RegistrationStopCoupon coupon = null;
        try {
            coupon = couponBean.get(person, getLocale(),
                    OSInfoUtil.lineSeparator(getWebRequestCycle().getWebRequest()));
        } catch (UnregisteredPersonException e) {
            messages.add(new FeedbackMessage(this, getString("personNotRegistered"), ERROR));
        } catch (Exception e) {
            messages.add(new FeedbackMessage(this, getString("db_error"), ERROR));
            log.error("", e);
        }
        add(coupon == null ? new MessagesFragment("content", messages) : new ReportFragment("content", coupon));

        //Загрузка отчетов
        reportDownloadPanel = new ReportDownloadPanel("report_download", RegistrationStopCouponDownload.class, person.getId(),
                getString("report_download"));
        add(reportDownloadPanel);
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        return ImmutableList.of(
                new SaveButton(id, true) {

                    @Override
                    protected void onClick(AjaxRequestTarget target) {
                        reportDownloadPanel.open(target);
                    }
                });
    }
}

