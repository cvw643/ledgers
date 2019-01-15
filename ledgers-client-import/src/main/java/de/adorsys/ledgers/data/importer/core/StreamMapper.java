package de.adorsys.ledgers.data.importer.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.adorsys.ledgers.data.importer.data.MockbankInitData;

public class StreamMapper {

	private final InputStream stream;
	private final MapperType type;
	
	private static final Map<MapperType, ObjectMapper> mapperFac = new HashMap<MapperType, ObjectMapper>();
	
	static {
		mapperFac.put(MapperType.YAML, new ObjectMapper(new YAMLFactory()));
	}
	
	public StreamMapper(InputStream stream, MapperType type) {
		this.stream = stream;
		this.type = type;
	}
	public MockbankInitData mapTo(Class<MockbankInitData> class1) {
		try {
			return mapperFac.get(this.type).readValue(this.stream, class1);
		} catch (IOException e) {
			throw new IllegalStateException(String.format("Unable to create %s", class1.getName()), e);
		}
	}

}
