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
	
	private SampleParserFactory sampleParserFactory;

	public SamplesParser(SampleParserFactory sampleParserFactory) {
		this.sampleParserFactory = sampleParserFactory;
	}
	
    public EVExercise parseWorkoutSamples(EVExercise exercise, Node workoutNode) {
        // obtain all the sample data.
        int totalSamples = countNodeItems(workoutNode, "sample");
        int currentSampleNumber = 0;

        ExerciseSample lastSample = new ExerciseSample(); // Stop the jitters... assumes no

        
        NodeList children = workoutNode.getChildNodes();
        exercise.setSampleList(new ExerciseSample[totalSamples]);
        String childName;
        double belowZone[] = {0, 0, 0, 0, 0, 0};
        double inZone[] = {0, 0, 0, 0, 0, 0};
        double aboveZone[] = {0, 0, 0, 0, 0, 0};
        
        int istop = children.getLength(); // getLength() is a slow function so keep it out of the loop.
        
        SampleParser sampleParser = sampleParserFactory.create();
        
        for (int i = 0; i < istop; i++) {
            childName = children.item(i).getNodeName();
            if (childName.equals("sample")) {
            	
//                ExerciseSample sample = new ExerciseSample();
            	Node sampleNode = children.item(i);
            	ExerciseSample sample = sampleParser.parse(sampleNode);
           	
            	// TODO set cadence
                //exercise.getRecordingMode().setCadence(true);
            	
                // Eliminates the jitters of 0bpm samples... assumes that heart rate won't change instantiously by much and
                // that there will only be the occasional missed heart beat.  Also fixes the laps not adding up.
                if (sample.getHeartRate() == 0)
                    sample.setHeartRate(lastSample.getHeartRate());
                else
                    lastSample.setHeartRate(sample.getHeartRate());
                exercise.getSampleList()[currentSampleNumber++] = sample;

                // TODO I need current offset in SampleParser as well as here
                // update Zone information
                if (exercise.getHeartRateLimits() != null) {
                	double currentOffset = sampleParser.currentOffset;
                	double lastOffset = sampleParser.lastOffset;
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
                // TODO I think we have to update the last sample
//            	lastSample = sample;
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
