package com.degressly.proxy.mysql.parser;

import com.degressly.proxy.constants.Encoding;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RemoteFieldEncodeDecodeFactory {

	@Autowired
	private List<FieldDecoder> decoders;

	@Autowired
	private List<FieldEncoder> encoders;

	private Map<Encoding, FieldDecoder> decoderMap;

	private Map<Encoding, FieldEncoder> encoderMap;

	@PostConstruct
	public void init() {
		Map<Encoding, FieldDecoder> tempDecoderMap = new HashMap<>();
		decoders.forEach((decoder) -> tempDecoderMap.put(decoder.getEncoding(), decoder));
		decoderMap = Collections.unmodifiableMap(tempDecoderMap);

		Map<Encoding, FieldEncoder> tempEncoderMap = new HashMap<>();
		encoders.forEach((encoder) -> tempEncoderMap.put(encoder.getEncoding(), encoder));
		encoderMap = Collections.unmodifiableMap(tempEncoderMap);
	}

	public FieldDecoder getFieldDecoder(Encoding encoding) {
		return decoderMap.get(encoding);
	}

	public FieldEncoder getFieldEncoder(Encoding encoding) {
		return encoderMap.get(encoding);
	}

}
