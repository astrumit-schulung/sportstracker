package de.saring.exerciseviewer.parser.impl.timexPwx;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Node;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.is;

public class SummaryDataParserTest {

	@Test
	public void test() throws Exception {
		float distance = 15;
		SummaryData summary = SummaryDataParser.parseSummaryData(stubNodeWithDistance(15));
		
		assertThat(summary.getDistance(), is(15f));
	}
	
	private Node stubNodeWithDistance(float distance) throws Exception {
		return DocumentBuilderFactory
			    .newInstance()
			    .newDocumentBuilder()
			    .parse(new ByteArrayInputStream(("<summary><dist>" + Float.toString(distance) +"</dist></summary>").getBytes()))
			    .getDocumentElement();
	}

}
