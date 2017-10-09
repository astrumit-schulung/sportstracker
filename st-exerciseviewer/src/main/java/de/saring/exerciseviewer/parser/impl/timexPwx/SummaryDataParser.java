package de.saring.exerciseviewer.parser.impl.timexPwx;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SummaryDataParser {
    public static SummaryData parseSummaryData(Node summaryDataNode) {
        SummaryData nodeSummaryData = new SummaryData();
        NodeList children = summaryDataNode.getChildNodes();

        String childName;
        for (int i = 0; i < children.getLength(); i++) {
            childName = children.item(i).getNodeName();
            if (childName.equals("beginning")) {
                // obtain beginning time
                nodeSummaryData.setBeginning(Double.valueOf(children.item(i).getTextContent()));
            } else if (childName.equals("duration")) {
                // obtain duration
                nodeSummaryData.setDuration(Double.valueOf(children.item(i).getTextContent()));
            } else if (childName.equals("hr")) {
                // obtain hr (MinMaxAvg)  (bpm)
                nodeSummaryData.setHr(node2MinMaxAvg(children.item(i)));
            } else if (childName.equals("work")) {
                // obtain work (Apparently Not used in Laps) (kJ)
                nodeSummaryData.setWork(Integer.valueOf(children.item(i).getTextContent()));
            } else if (childName.equals("spd")) {
                // obtain spd (MinMaxAvg) (meters/second)
                nodeSummaryData.setSpeed(node2MinMaxAvg(children.item(i)));
            } else if (childName.equals("alt")) {
                // obtain altitude (MinMaxAvg) (meters)
                nodeSummaryData.setAltitude(node2MinMaxAvg(children.item(i)));
            } else if (childName.equals("dist")) {
                // obtain distance (meters)
                nodeSummaryData.setDistance(Float.valueOf(children.item(i).getTextContent()));
            }
            // 1st time its for the entire workout
            // remaining times is for the Laps
            // obtain duration stopped
            // obtain tss
            // obtain normalizedPower (watts)
            // obtain pwr (MinMaxAvg) (watts)
            // obtain torq (MinMaxAvg) (nM)
            // obtain cadence (MinMaxAvg) (rpm)
            // obtain temp (MinMaxAvg) (C)
            // obtain variabilityIndex - Not sure what this is
            // obtain climbingelevation
        }
        return nodeSummaryData; // Probably don't want to pass and return the Exercise itself.
    }
    
    private static MinMaxAvg node2MinMaxAvg(Node inNode) {
        MinMaxAvg result = new MinMaxAvg();
        NamedNodeMap attributes = inNode.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            if (attributes.item(i).getNodeName().equals("max")) {
                result.setMax(Float.valueOf(attributes.item(i).getTextContent()));
            } else if (attributes.item(i).getNodeName().equals("min")) {
                result.setMin(Float.valueOf(attributes.item(i).getTextContent()));
            } else if (attributes.item(i).getNodeName().equals("avg")) {
                result.setAvg(Float.valueOf(attributes.item(i).getTextContent()));
            }
        }
        return result;
    }
}
