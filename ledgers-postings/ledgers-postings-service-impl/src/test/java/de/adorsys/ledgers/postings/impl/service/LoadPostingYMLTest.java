package de.adorsys.ledgers.postings.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.postings.db.domain.Posting;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class LoadPostingYMLTest {
	private ObjectMapper mapper = new ObjectMapper();
	
	@Before
	public void before(){
        final YAMLFactory ymlFactory = new YAMLFactory();
//        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
//        mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
//        mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
//        mapper.registerModule(new JavaTimeModule());
        mapper = new ObjectMapper(ymlFactory);
	}
	
	@Test
	public void testReadYml() throws IOException{
		InputStream inputStream = LoadPostingYMLTest.class.getResourceAsStream("LoadPostingYMLTest.yml");
		Posting[] postings = mapper.readValue(inputStream, Posting[].class);
		Assert.assertNotNull(postings);
		Assert.assertEquals(1, postings.length);
//		Assert.assertEquals("1",ledgerAccounts[0].getName());
//		Assert.assertEquals("Assets",ledgerAccounts[0].getShortDesc());
//		Assert.assertEquals(AccountCategory.AS, ledgerAccounts[0].getCategory());
//		Assert.assertEquals(BalanceSide.Dr, ledgerAccounts[0].getBalanceSide());
//		Assert.assertEquals("1.1",ledgerAccounts[1].getName());
//		Assert.assertEquals("Property, Plant And Equipment",ledgerAccounts[1].getShortDesc());
	} 
}
