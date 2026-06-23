/*
 * Copyright 2023-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.partmeai.openclaw.spring.boot;

import java.util.List;

import io.github.partmeai.openclaw.api.common.OpenClawApiConstants;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties for Spring AI OpenClaw.
 *
 * @author Loong Wan
 */
@Data
@ConfigurationProperties(prefix = OpenClawSpringAiProperties.PREFIX)
public class OpenClawSpringAiProperties {

	public static final String PREFIX = "spring.ai.openclaw";

	/**
	 * Enable OpenClaw Spring AI integration.
	 */
	private boolean enabled = true;

	/**
	 * OpenClaw Gateway base URL.
	 */
	private String baseUrl = OpenClawApiConstants.DEFAULT_BASE_URL;

	/**
	 * Chat Completions API options.
	 */
	@NestedConfigurationProperty
	private ChatOptions chat = new ChatOptions();

	/**
	 * Embeddings API options.
	 */
	@NestedConfigurationProperty
	private EmbeddingOptions embedding = new EmbeddingOptions();

	/**
	 * Responses API options.
	 */
	@NestedConfigurationProperty
	private ResponsesOptions responses = new ResponsesOptions();

	/**
	 * Chat Completions API options.
	 */
	@Data
	public static class ChatOptions {

		/**
		 * Default model/agent target.
		 */
		private String model = OpenClawApiConstants.AGENT_DEFAULT;

		/**
		 * Sampling temperature (0.0-2.0).
		 */
		private Double temperature;

		/**
		 * Nucleus sampling probability.
		 */
		private Double topP;

		/**
		 * Frequency penalty (-2.0 to 2.0).
		 */
		private Double frequencyPenalty;

		/**
		 * Presence penalty (-2.0 to 2.0).
		 */
		private Double presencePenalty;

		/**
		 * Maximum completion tokens (preferred).
		 */
		private Integer maxCompletionTokens;

		/**
		 * Maximum tokens (legacy alias).
		 */
		private Integer maxTokens;

		/**
		 * Stop sequences.
		 */
		private List<String> stop;

		/**
		 * Override backend model via x-openclaw-model header.
		 */
		private String modelOverride;

		/**
		 * Default user for stable session routing.
		 */
		private String user;
	}

	/**
	 * Embedding API options.
	 */
	@Data
	public static class EmbeddingOptions {

		/**
		 * Default model/agent target.
		 */
		private String model = OpenClawApiConstants.AGENT_DEFAULT;

		/**
		 * Override backend model via x-openclaw-model header.
		 */
		private String modelOverride;
	}

	/**
	 * Responses API options.
	 */
	@Data
	public static class ResponsesOptions {

		/**
		 * Default model/agent target.
		 */
		private String model = OpenClawApiConstants.AGENT_DEFAULT;

		/**
		 * Override backend model via x-openclaw-model header.
		 */
		private String modelOverride;

		/**
		 * Sampling temperature.
		 */
		private Double temperature;

		/**
		 * Nucleus sampling probability.
		 */
		private Double topP;

		/**
		 * Maximum output tokens.
		 */
		private Integer maxOutputTokens;
	}
}
