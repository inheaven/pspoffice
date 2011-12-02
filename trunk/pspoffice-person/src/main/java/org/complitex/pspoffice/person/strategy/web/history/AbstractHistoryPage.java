/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.pspoffice.person.strategy.web.history;

import java.util.Date;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.string.Strings;
import org.complitex.template.web.template.TemplatePage;
import org.odlabs.wiquery.core.javascript.JsQuery;

/**
 *
 * @author Artem
 */
public abstract class AbstractHistoryPage extends TemplatePage {

    private static final String HISTORY_CONTENT_WICKET_ID = "historyContent";
    private final WebMarkupContainer historyContainer;
    private final long objectId;
    private Date currentEndDate;

    private abstract class HistoryLink extends AjaxLink<Void> {

        private boolean isPostBack;
        private final boolean initiallyVisible;

        private HistoryLink(String id, boolean initiallyVisible) {
            super(id);

            this.initiallyVisible = initiallyVisible;
        }

        @Override
        protected IAjaxCallDecorator getAjaxCallDecorator() {
            return new AjaxCallDecorator() {

                @Override
                public CharSequence decorateScript(CharSequence script) {
                    return "(function(){$(this).attr('disabled', true); $('#load_indicator').show();})();" + script;
                }
            };
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            historyContainer.replace(newHistoryContent(HISTORY_CONTENT_WICKET_ID, objectId, currentEndDate));
            target.addComponent(historyContainer);
            setHistoryButtonsVisibility(target);
        }

        protected final void postOnClick(AjaxRequestTarget target) {
            String enableButtonScript = new JsQuery(this).$().render(false) + ".attr('disabled', false);";
            target.appendJavascript("(function(){" + enableButtonScript + "$('#load_indicator').hide()})();");
        }

        @Override
        protected void onComponentTag(ComponentTag tag) {
            super.onComponentTag(tag);
            if (!isPostBack) {
                isPostBack = true;
                if (!initiallyVisible) {
                    String style = "";
                    CharSequence styleCS = tag.getString("style");
                    if (!Strings.isEmpty(styleCS)) {
                        style = styleCS.toString();
                    }
                    if (!Strings.isEmpty(style) && !style.endsWith(";")) {
                        style += ";";
                    }
                    tag.put("style", style + "visibility: hidden;");
                }
            }
        }
    }

    public AbstractHistoryPage(final long objectId) {
        this.objectId = objectId;

        add(CSSPackageResource.getHeaderContribution(AbstractHistoryPage.class, AbstractHistoryPage.class.getSimpleName() + ".css"));

        //history container and content.
        historyContainer = new WebMarkupContainer("historyContainer");
        historyContainer.setOutputMarkupId(true);
        historyContainer.add(newHistoryContent(HISTORY_CONTENT_WICKET_ID, objectId, currentEndDate));
        add(historyContainer);

        //history buttons
        boolean backButtonVisibleInitially = isBackButtonVisible();
        boolean forwardButtonVisibleInitially = isForwardButtonVisible();

        WebMarkupContainer historyButtonsContainer = new WebMarkupContainer("historyButtonsContainer");
        historyButtonsContainer.setVisible(backButtonVisibleInitially || forwardButtonVisibleInitially);
        add(historyButtonsContainer);

        historyButtonsContainer.add(new HistoryLink("back", backButtonVisibleInitially) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                Date newEndDate = getPreviousModificationDate(objectId, currentEndDate);
                if (getPreviousModificationDate(objectId, newEndDate) != null) {
                    currentEndDate = newEndDate;
                    super.onClick(target);
                }
                postOnClick(target);
            }
        });

        historyButtonsContainer.add(new HistoryLink("forward", forwardButtonVisibleInitially) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (currentEndDate != null) {
                    currentEndDate = getNextModificationDate(objectId, currentEndDate);
                    super.onClick(target);
                }
                postOnClick(target);
            }
        });
    }

    protected abstract Date getPreviousModificationDate(long objectId, Date currentEndDate);

    protected abstract Date getNextModificationDate(long objectId, Date currentEndDate);

    private boolean isBackButtonVisible() {
        return getPreviousModificationDate(objectId,
                getPreviousModificationDate(objectId, currentEndDate)) != null;
    }

    private boolean isForwardButtonVisible() {
        return currentEndDate != null;
    }

    private void setHistoryButtonsVisibility(AjaxRequestTarget target) {
        target.appendJavascript("(function(){ $('.history_back_button').css('visibility', '"
                + getCssVisibility(isBackButtonVisible()) + "');"
                + "$('.history_forward_button').css('visibility', '" + getCssVisibility(isForwardButtonVisible()) + "'); })()");
    }

    private String getCssVisibility(boolean visible) {
        return visible ? "visible" : "hidden";
    }

    protected abstract Component newHistoryContent(String id, long objectId, Date currentEndDate);
}
