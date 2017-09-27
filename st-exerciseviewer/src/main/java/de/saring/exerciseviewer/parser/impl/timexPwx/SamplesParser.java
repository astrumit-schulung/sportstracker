package de.saring.exerciseviewer.parser.impl.timexPwx;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.saring.exerciseviewer.data.EVExercise;
import de.saring.exerciseviewer.data.ExerciseAltitude;
import de.saring.exerciseviewer.data.ExerciseSample;
import de.saring.exerciseviewer.data.ExerciseSpeed;
import de.saring.exerciseviewer.data.Position;
import de.saring.util.unitcalc.CalculationUtils;

public class SamplesParser {
    public static EVExercise parseWorkoutSamples(EVExercise exercise, Node workoutNode) {
        // obtain all the sample data.
        int totalSamples = countNodeItems(workoutNode, "sample");
        int currentSampleNumber = 0;
        float lastDistance = 0;
        boolean distanceinsample = false;
        boolean firstsample = true;
        exercise.setSampleList(new ExerciseSample[totalSamples]);
        double lastOffset = 0;
        double currentOffset = 0;
        Position lastPosition = new Position(0, 0);
        NodeList children = workoutNode.getChildNodes();
        NodeList sampleChildren = null;
        String childName;
        ExerciseSample lastSample = new ExerciseSample(); // Stop the jitters... assumes no
        Double latitude = 0.0, longitude = 0.0;
        double belowZone[] = {0, 0, 0, 0, 0, 0};
        double inZone[] = {0, 0, 0, 0, 0, 0};
        double aboveZone[] = {0, 0, 0, 0, 0, 0};
        int istop = children.getLength(); // getLength() is a slow function so keep it out of the loop.
        for (int i = 0; i < istop; i++) {
            childName = children.item(i).getNodeName();
            if (childName.equals("sample")) {
                ExerciseSample sample = new ExerciseSample();
                sampleChildren = children.item(i).getChildNodes();
                int jstop = sampleChildren.getLength();
                for (int j = 0; j < jstop; j++) {
                    childName = sampleChildren.item(j).getNodeName();
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
                        exercise.getRecordingMode().setCadence(true);
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
                // Eliminates the jitters of 0bpm samples... assumes that heart rate won't change instantiously by much and
                // that there will only be the occasional missed heart beat.  Also fixes the laps not adding up.
                if (sample.getHeartRate() == 0)
                    sample.setHeartRate(lastSample.getHeartRate());
                else
                    lastSample.setHeartRate(sample.getHeartRate());
                exercise.getSampleList()[currentSampleNumber++] = sample;

                // update Zone information
                if (exercise.getHeartRateLimits() != null) {
                    for (int j = 0; j < 6; j++) {
                        if (sample.getHeartRate() > exercise.getHeartRateLimits()[j].getUpperHeartRate()) {
                            aboveZone[j] += (currentOffset - lastOffset);
                        } else if (sample.getHeartRate() < exercise.getHeartRateLimits()[j].getLowerHeartRate()) {
                            belowZone[j] += (currentOffset - lastOffset);
                        } else {
                            inZone[j] += (currentOffset - lastOffset);
                        }
                    }
                }
            }

        }

        // Store Zone Information in the exercise file
        if (exercise.getHeartRateLimits() != null) {
            for (int i = 0; i < 6; i++) {
                exercise.getHeartRateLimits()[i].setTimeAbove((short) aboveZone[i]);
                exercise.getHeartRateLimits()[i].setTimeBelow((short) belowZone[i]);
                exercise.getHeartRateLimits()[i].setTimeWithin((short) inZone[i]);
            }
        }
        exercise.setRecordingInterval((short) 2);

        // some models (e.g. Timex Ironman Run Trainer) don't contain statistic date (avg, max, ...)
        // => compute the missing data   
        if (exercise.getSampleList().length > 0) {
            computeHeartrateStatisticIfMissing(exercise);
            computeSpeedStatisticIfMissing(exercise);
            computeAltitudeStatisticIfMissing(exercise);
        }
        return exercise;
    }
    

    private static void computeHeartrateStatisticIfMissing(EVExercise exercise) {
        if (exercise.getHeartRateAVG() == 0) {
            double sumHeartrate = 0;

            for (ExerciseSample sample : exercise.getSampleList()) {
                sumHeartrate += sample.getHeartRate();
                exercise.setHeartRateMax((short) Math.max(exercise.getHeartRateMax(), sample.getHeartRate()));
            }
            exercise.setHeartRateAVG((short) Math.round(sumHeartrate / (double) exercise.getSampleList().length));
        }
    }

    private static void computeSpeedStatisticIfMissing(EVExercise exercise) {
        if (exercise.getRecordingMode().isSpeed() && exercise.getSpeed() == null) {

            ExerciseSpeed exSpeed = new ExerciseSpeed();
            exSpeed.setSpeedMax(Float.MIN_VALUE);
            exercise.setSpeed(exSpeed);

            for (ExerciseSample sample : exercise.getSampleList()) {
                exSpeed.setSpeedMax(Math.max(exSpeed.getSpeedMax(), sample.getSpeed()));
            }

            ExerciseSample lastSample = exercise.getSampleList()[exercise.getSampleList().length - 1];
            exSpeed.setDistance(lastSample.getDistance());
            exSpeed.setSpeedAVG(CalculationUtils.calculateAvgSpeed(
                    exSpeed.getDistance() / 1000f,
                    Math.round(exercise.getDuration() / 10f)));
        }
    }

    private static void computeAltitudeStatisticIfMissing(EVExercise exercise) {
        if (exercise.getRecordingMode().isAltitude() && exercise.getAltitude() == null) {

            ExerciseAltitude exAltitude = new ExerciseAltitude();
            exAltitude.setAltitudeMin(Short.MAX_VALUE);
            exAltitude.setAltitudeMax(Short.MIN_VALUE);
            exercise.setAltitude(exAltitude);

            double sumAltitude = 0;
            short previousAltitude = Short.MAX_VALUE;

            for (ExerciseSample sample : exercise.getSampleList()) {
                sumAltitude += sample.getAltitude();
                exAltitude.setAltitudeMin((short) Math.min(exAltitude.getAltitudeMin(), sample.getAltitude()));
                exAltitude.setAltitudeMax((short) Math.max(exAltitude.getAltitudeMax(), sample.getAltitude()));

                if (previousAltitude < sample.getAltitude()) {
                    exAltitude.setAscent(exAltitude.getAscent() + (sample.getAltitude() - previousAltitude));
                }
                previousAltitude = sample.getAltitude();
            }
            exAltitude.setAltitudeAVG((short) Math.round(sumAltitude / (double) exercise.getSampleList().length));
        }
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
    
    static int countNodeItems(Node node, String string2count) {
        // Given a Node and a Child Node Name, count the number of children with that node name
        NodeList children = node.getChildNodes();

        int numChildren = children.getLength();
        String currentNodeName = null;
        int numMatches = 0;


        for (int i = 0; i < numChildren; i++) {
            currentNodeName = children.item(i).getNodeName();
            if (currentNodeName.equals(string2count)) {
                numMatches++;
            }
        }
        return numMatches;
    }
}
