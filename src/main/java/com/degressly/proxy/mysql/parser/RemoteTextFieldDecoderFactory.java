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
public class RemoteTextFieldDecoderFactory {

	@Autowired
	private List<TextFieldDecoder> decoders;

	private Map<Encoding, TextFieldDecoder> decoderMap;

	@PostConstruct
	public void init() {
		Map<Encoding, TextFieldDecoder> map = new HashMap<>();
		decoders.forEach((decoder) -> map.put(decoder.getEncoding(), decoder));
		decoderMap = Collections.unmodifiableMap(map);
	}

	public TextFieldDecoder get(Encoding encoding) {
		return decoderMap.get(encoding);
	}

}
