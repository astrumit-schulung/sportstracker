package de.saring.exerciseviewer.parser.impl.timexPwx;

import static org.mockito.Matchers.any;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Node;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertThat;

import static org.hamcrest.CoreMatchers.is;

import de.saring.exerciseviewer.data.EVExercise;
import de.saring.exerciseviewer.data.ExerciseSample;
import de.saring.exerciseviewer.data.RecordingMode;

public class SamplesParserTest {

	@Test
	public void It_sets_recording_mode_to_Cadence_when_sample_contains_cadence() throws Exception {
		ExerciseSample sample = new ExerciseSample();
		sample.setCadence((short) 123);
		
		SampleParserFactory sampleParserFactory = stubSampleParserFactoryToReturn(sample);
		
		SamplesParser sut = new SamplesParser(sampleParserFactory);
		EVExercise exercise = new EVExercise();
		exercise.setRecordingMode(new RecordingMode());
		sut.parseWorkoutSamples(exercise, stubNode() );
		
		assertThat(exercise.getRecordingMode().isCadence(), is(true));
	}

	private SampleParserFactory stubSampleParserFactoryToReturn(ExerciseSample sample) {
		SampleParser sampleParser = mock(SampleParser.class);
		when(sampleParser.parse(any())).thenReturn(sample);
		SampleParserFactory sampleParserFactory = mock(SampleParserFactory.class);
		when(sampleParserFactory.create()).thenReturn(sampleParser);
		return sampleParserFactory;
	}

	private Node stubNode() throws Exception {
		return DocumentBuilderFactory
			    .newInstance()
			    .newDocumentBuilder()
			    .parse(new ByteArrayInputStream("<pwx><sample></sample></pwx>".getBytes()))
			    .getDocumentElement();
	}
}
