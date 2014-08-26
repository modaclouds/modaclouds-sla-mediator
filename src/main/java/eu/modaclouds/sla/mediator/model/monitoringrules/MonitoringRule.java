/**
 * Copyright 2014 Atos
 * Contact: Atos <roman.sosa@atos.net>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.06.11 at 02:23:53 PM BST 
//


package eu.modaclouds.sla.mediator.model.monitoringrules;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for monitoringRule complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="monitoringRule">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="monitoredTargets" type="{http://www.modaclouds.eu/xsd/1.0/monitoring_rules_schema}monitoredTargets" minOccurs="0"/>
 *         &lt;element name="collectedMetric" type="{http://www.modaclouds.eu/xsd/1.0/monitoring_rules_schema}collectedMetric"/>
 *         &lt;element name="metricAggregation" type="{http://www.modaclouds.eu/xsd/1.0/monitoring_rules_schema}monitoringMetricAggregation" minOccurs="0"/>
 *         &lt;element name="condition" type="{http://www.modaclouds.eu/xsd/1.0/monitoring_rules_schema}condition" minOccurs="0"/>
 *         &lt;element name="actions" type="{http://www.modaclouds.eu/xsd/1.0/monitoring_rules_schema}actions" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="label" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="relatedQosConstraintId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="parentMonitoringRuleId" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *       &lt;attribute name="startEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="timeStep" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="timeWindow" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "monitoringRule", propOrder = {
    "monitoredTargets",
    "collectedMetric",
    "metricAggregation",
    "condition",
    "actions"
})
public class MonitoringRule {

    protected MonitoredTargets monitoredTargets;
    @XmlElement(required = true)
    protected CollectedMetric collectedMetric;
    protected MonitoringMetricAggregation metricAggregation;
    protected Condition condition;
    protected Actions actions;
    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "label", required = true)
    protected String label;
    @XmlAttribute(name = "relatedQosConstraintId")
    protected String relatedQosConstraintId;
    @XmlAttribute(name = "parentMonitoringRuleId")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected MonitoringRule parentMonitoringRule;
    @XmlAttribute(name = "startEnabled")
    protected Boolean startEnabled;
    @XmlAttribute(name = "timeStep")
    protected String timeStep;
    @XmlAttribute(name = "timeWindow")
    protected String timeWindow;

    /**
     * Gets the value of the monitoredTargets property.
     * 
     * @return
     *     possible object is
     *     {@link MonitoredTargets }
     *     
     */
    public MonitoredTargets getMonitoredTargets() {
        return monitoredTargets;
    }

    /**
     * Sets the value of the monitoredTargets property.
     * 
     * @param value
     *     allowed object is
     *     {@link MonitoredTargets }
     *     
     */
    public void setMonitoredTargets(MonitoredTargets value) {
        this.monitoredTargets = value;
    }

    /**
     * Gets the value of the collectedMetric property.
     * 
     * @return
     *     possible object is
     *     {@link CollectedMetric }
     *     
     */
    public CollectedMetric getCollectedMetric() {
        return collectedMetric;
    }

    /**
     * Sets the value of the collectedMetric property.
     * 
     * @param value
     *     allowed object is
     *     {@link CollectedMetric }
     *     
     */
    public void setCollectedMetric(CollectedMetric value) {
        this.collectedMetric = value;
    }

    /**
     * Gets the value of the metricAggregation property.
     * 
     * @return
     *     possible object is
     *     {@link MonitoringMetricAggregation }
     *     
     */
    public MonitoringMetricAggregation getMetricAggregation() {
        return metricAggregation;
    }

    /**
     * Sets the value of the metricAggregation property.
     * 
     * @param value
     *     allowed object is
     *     {@link MonitoringMetricAggregation }
     *     
     */
    public void setMetricAggregation(MonitoringMetricAggregation value) {
        this.metricAggregation = value;
    }

    /**
     * Gets the value of the condition property.
     * 
     * @return
     *     possible object is
     *     {@link Condition }
     *     
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * Sets the value of the condition property.
     * 
     * @param value
     *     allowed object is
     *     {@link Condition }
     *     
     */
    public void setCondition(Condition value) {
        this.condition = value;
    }

    /**
     * Gets the value of the actions property.
     * 
     * @return
     *     possible object is
     *     {@link Actions }
     *     
     */
    public Actions getActions() {
        return actions;
    }

    /**
     * Sets the value of the actions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Actions }
     *     
     */
    public void setActions(Actions value) {
        this.actions = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the label property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Gets the value of the relatedQosConstraintId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelatedQosConstraintId() {
        return relatedQosConstraintId;
    }

    /**
     * Sets the value of the relatedQosConstraintId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelatedQosConstraintId(String value) {
        this.relatedQosConstraintId = value;
    }

    /**
     * Gets the value of the parentMonitoringRule property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public MonitoringRule getParentMonitoringRule() {
        return parentMonitoringRule;
    }

    /**
     * Sets the value of the parentMonitoringRule property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setParentMonitoringRule(MonitoringRule value) {
        this.parentMonitoringRule = value;
    }

    /**
     * Gets the value of the startEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isStartEnabled() {
        return startEnabled;
    }

    /**
     * Sets the value of the startEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStartEnabled(Boolean value) {
        this.startEnabled = value;
    }

    /**
     * Gets the value of the timeStep property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeStep() {
        return timeStep;
    }

    /**
     * Sets the value of the timeStep property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeStep(String value) {
        this.timeStep = value;
    }

    /**
     * Gets the value of the timeWindow property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeWindow() {
        return timeWindow;
    }

    /**
     * Sets the value of the timeWindow property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeWindow(String value) {
        this.timeWindow = value;
    }

}
