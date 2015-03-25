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
package eu.modaclouds.sla.mediator.model.servicedefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Seff")
public class Resource {

    @XmlAttribute
    private String id;

    @XmlElement(name="period")
    private List<Period> periods;

    public Resource() {
    }
    
    public Resource(String id, List<Period> periods) {
        this.id = id;
        this.periods = periods;
    }

    public String getId() {
        return id;
    }
    
    public List<Period> getPeriods() {
        return Collections.unmodifiableList(periods);
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void addPeriod(Period period) {
        this.periods.add(period);
    }
    
    public boolean removePeriod(Period period) {
        return this.periods.remove(period);
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Period {
        @XmlAttribute
        private String hour;
        
        @XmlAttribute
        private Float avgRT;
        
        @XmlAttribute
        private Float throughput;
        
        @XmlElement(name="percentile")
        private List<Percentile> percentiles;
        
        public Period() {
            this.percentiles = new ArrayList<Percentile>();
        }

        public Period(String hour, Float avgRT, Float throughput,
                List<Percentile> percentiles) {
            this.hour = hour;
            this.avgRT = avgRT;
            this.throughput = throughput;
            this.percentiles = new ArrayList<Percentile>(percentiles);
        }
        
        public String getHour() {
            return hour;
        }
        
        public Float getAvgRT() {
            return avgRT;
        }
        
        public Float getThroughput() {
            return throughput;
        }
        
        public List<Percentile> getPercentiles() {
            return Collections.unmodifiableList(percentiles);
        }
        
        public Percentile getPercentile(int level) {
            for (Percentile p : percentiles) {
                if (level == p.getLevel()) {
                    return p;
                }
            }
            return Percentile.PERCENTILE_NOT_FOUND;
        }
        
        public void setHour(String hour) {
            this.hour = hour;
        }
        
        public void setAvgRT(Float avgRT) {
            this.avgRT = avgRT;
        }
        
        public void setThroughput(Float throughput) {
            this.throughput = throughput;
        }
        
        public void addPercentile(Percentile percentile) {
            this.percentiles.add(percentile);
        }
        
        public boolean removePercentile(Percentile percentile) {
            return this.percentiles.remove(percentile);
        }
        
        public void setPercentile(int level, Float value) {
            Percentile p = getPercentile(level);
            if (p == Percentile.PERCENTILE_NOT_FOUND) {
                p = new Percentile(level, value);
                this.addPercentile(p);
            }
            else {
                p.setValue(value);
            }
        }
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Percentile {
        
        public static Percentile PERCENTILE_NOT_FOUND = new Percentile(-1, -1f);
        
        @XmlAttribute
        private int level;
        
        @XmlAttribute
        private Float value;
        
        public Percentile() {
        }

        public Percentile(int level, Float value) {
            this.level = level;
            this.value = value;
        }
        
        public int getLevel() {
            return level;
        }
        
        public Float getValue() {
            return value;
        }
        
        public void setLevel(int level) {
            this.level = level;
        }
        
        public void setValue(Float value) {
            this.value = value;
        }
    }
}
