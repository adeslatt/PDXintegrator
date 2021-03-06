//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.02.24 at 06:07:06 PM EST 
//


package org.jax.pdxintegrator.drugbank;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for reaction-type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="reaction-type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sequence" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="left-element" type="{http://www.drugbank.ca}reaction-element-type"/>
 *         &lt;element name="right-element" type="{http://www.drugbank.ca}reaction-element-type"/>
 *         &lt;element name="enzymes" type="{http://www.drugbank.ca}reaction-enzyme-list-type"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "reaction-type", propOrder = {
    "sequence",
    "leftElement",
    "rightElement",
    "enzymes"
})
public class ReactionType {

    @XmlElement(required = true)
    protected String sequence;
    @XmlElement(name = "left-element", required = true)
    protected ReactionElementType leftElement;
    @XmlElement(name = "right-element", required = true)
    protected ReactionElementType rightElement;
    @XmlElement(required = true)
    protected ReactionEnzymeListType enzymes;

    /**
     * Gets the value of the sequence property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Sets the value of the sequence property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSequence(String value) {
        this.sequence = value;
    }

    /**
     * Gets the value of the leftElement property.
     * 
     * @return
     *     possible object is
     *     {@link ReactionElementType }
     *     
     */
    public ReactionElementType getLeftElement() {
        return leftElement;
    }

    /**
     * Sets the value of the leftElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReactionElementType }
     *     
     */
    public void setLeftElement(ReactionElementType value) {
        this.leftElement = value;
    }

    /**
     * Gets the value of the rightElement property.
     * 
     * @return
     *     possible object is
     *     {@link ReactionElementType }
     *     
     */
    public ReactionElementType getRightElement() {
        return rightElement;
    }

    /**
     * Sets the value of the rightElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReactionElementType }
     *     
     */
    public void setRightElement(ReactionElementType value) {
        this.rightElement = value;
    }

    /**
     * Gets the value of the enzymes property.
     * 
     * @return
     *     possible object is
     *     {@link ReactionEnzymeListType }
     *     
     */
    public ReactionEnzymeListType getEnzymes() {
        return enzymes;
    }

    /**
     * Sets the value of the enzymes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReactionEnzymeListType }
     *     
     */
    public void setEnzymes(ReactionEnzymeListType value) {
        this.enzymes = value;
    }

}
