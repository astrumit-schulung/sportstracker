package de.saring.exerciseviewer.parser.impl.timexPwx;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.saring.exerciseviewer.data.ExerciseSample;
import de.saring.exerciseviewer.data.Position;

public class SampleParser {
	
    private float lastDistance = 0;
    private boolean distanceinsample = false;
    private boolean firstsample = true;
    double lastOffset = 0;
    double currentOffset = 0;
    private Position lastPosition = new Position(0, 0);
    private NodeList sampleChildren = null;
    
    private Double latitude = 0.0, longitude = 0.0;

	public ExerciseSample parse(Node sampleNode) {
		ExerciseSample sample = new ExerciseSample();
        sampleChildren = sampleNode.getChildNodes();
        int jstop = sampleChildren.getLength();
        for (int j = 0; j < jstop; j++) {
            String childName = sampleChildren.item(j).getNodeName();
            if (childName.equals("timeoffset")) {
                if (currentOffset != 0)
                    lastOffset = currentOffset;
                currentOffset = Double.valueOf(sampleChildren.item(j).getTextContent());
                sample.setTimestamp((long) (1000 * currentOffset));
            } else if (childName.equals("hr")) {
                sample.setHeartRate(Short.valueOf(sampleChildren.item(j).getTextContent()));
            } else if (childName.equals("spd")) {
                sample.setSpeed((float) 3.6 * Float.valueOf(sampleChildren.item(j).getTextContent()).floatValue());
            } else if (childName.equals("pwr")) {
                // Not implemented in ExerciseSample class
            } else if (childName.equals("torq")) {
                // Not implemented in ExerciseSample class
            } else if (childName.equals("cad")) {
                sample.setCadence(Short.valueOf(sampleChildren.item(j).getTextContent()));
            } else if (childName.equals("dist")) {
                double dist = Double.valueOf(sampleChildren.item(j).getTextContent());
                sample.setDistance((int) Math.round(dist));
                distanceinsample = true;
            } else if (childName.equals("lat")) {
                latitude = Double.valueOf(sampleChildren.item(j).getTextContent());
            } else if (childName.equals("lon")) {
                longitude = Double.valueOf(sampleChildren.item(j).getTextContent());
            } else if (childName.equals("alt")) {
                sample.setAltitude(Float.valueOf(sampleChildren.item(j).getTextContent()).shortValue());
            } else if (childName.equals("temp")) {
                sample.setTemperature(Float.valueOf(sampleChildren.item(j).getTextContent()).shortValue());
            } else if (childName.equals("time")) {
                // Not implemented in ExerciseSample
            }
        }
        sample.setPosition(new Position(latitude, longitude));
        if (firstsample) {
            lastPosition = sample.getPosition();
            firstsample = false;
        }
        if (!distanceinsample) {
            lastDistance += getDistanceFromPositions(lastPosition, sample.getPosition());
            sample.setDistance((int) lastDistance);
            lastPosition = sample.getPosition();
        }

		return sample;
	}
	
private static float getDistanceFromPositions(Position startPosition, Position stopPosition) { //float lat1, float lng1, float lat2, float lng2) {
    double earthRadius = 6369.6; //3958.75;
    double dLat = Math.toRadians(stopPosition.getLatitude() - startPosition.getLatitude());
    double dLng = Math.toRadians(stopPosition.getLongitude() - startPosition.getLongitude());
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(startPosition.getLatitude())) * Math.cos(Math.toRadians(stopPosition.getLatitude()))
            * Math.sin(dLng / 2) * Math.sin(dLng / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double dist = earthRadius * c;

    int meterConversion = 1000; // 1609;

    if (dist < 0) {
        dist = 0 - dist;
    }
    return (float) (dist * meterConversion);
}
}
