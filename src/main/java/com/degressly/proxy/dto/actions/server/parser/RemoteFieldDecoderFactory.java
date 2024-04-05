package com.degressly.proxy.dto.actions.server.parser;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RemoteFieldDecoderFactory {

    @Autowired
    private List<FieldDecoder> decoders;

    private Map<Encoding, FieldDecoder> decoderMap;

    @PostConstruct
    public void init() {
        Map<Encoding, FieldDecoder> map = new HashMap<>();
        decoders.forEach((decoder) -> map.put(decoder.getEncoding(), decoder));
        decoderMap = Collections.unmodifiableMap(map);
    }

    public FieldDecoder get(Encoding encoding) {
        return decoderMap.get(encoding);
    }
}
