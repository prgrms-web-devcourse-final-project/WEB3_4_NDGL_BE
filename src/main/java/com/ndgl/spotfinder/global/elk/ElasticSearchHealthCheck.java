package com.ndgl.spotfinder.global.elk;

import org.springframework.stereotype.Component;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ElasticSearchHealthCheck {
	private final ElasticsearchClient client;

	public boolean isElasticSearchUp() { // ES 서버 상태 체크
		try {
			client.ping();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
