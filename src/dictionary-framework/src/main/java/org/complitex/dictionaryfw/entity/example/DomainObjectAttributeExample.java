/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.entity.example;

import java.io.Serializable;

/**
 *
 * @author Artem
 */
public class DomainObjectAttributeExample implements Serializable {

    private Long attributeTypeId;

    private String value;

    public DomainObjectAttributeExample(Long attributeTypeId) {
        this.attributeTypeId = attributeTypeId;
    }

    public Long getAttributeTypeId() {
        return attributeTypeId;
    }

    public void setAttributeTypeId(Long attributeTypeId) {
        this.attributeTypeId = attributeTypeId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
