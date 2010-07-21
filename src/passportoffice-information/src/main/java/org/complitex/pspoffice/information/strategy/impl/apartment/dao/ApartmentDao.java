/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.pspoffice.information.strategy.impl.apartment.dao;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import org.complitex.dictionaryfw.dao.AbstractEntityDao;
import org.complitex.dictionaryfw.dao.aop.SqlSessionInterceptor;
import org.complitex.pspoffice.information.strategy.impl.apartment.Apartment;
import org.complitex.pspoffice.information.strategy.impl.apartment.example.ApartmentExample;

/**
 *
 * @author Artem
 */
@Stateless
@LocalBean
@Interceptors({SqlSessionInterceptor.class})
public class ApartmentDao extends AbstractEntityDao<Apartment, ApartmentExample> {

    public static final String TABLE_NAME = "apartment";

    public static enum OrderBy {

        NAME;
    }

    @Override
    protected String getNamespace() {
        return "org.complitex.pspoffice.information.strategy.impl.apartment.Apartment";
    }

    @Override
    public Apartment newInstance() {
        Apartment apartment = new Apartment();
        configureNewEntity(apartment);
        return apartment;
    }

    @Override
    public String getTable() {
        return TABLE_NAME;
    }
}
