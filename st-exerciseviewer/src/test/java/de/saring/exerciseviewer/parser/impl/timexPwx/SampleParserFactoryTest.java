package de.saring.exerciseviewer.parser.impl.timexPwx;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

public class SampleParserFactoryTest {

	@Test
	public void createCreatesSampleParser() {
		SampleParserFactory sut = new SampleParserFactory();
		
		assertThat(sut.create(), is(instanceOf(SampleParser.class)));
	}

}
