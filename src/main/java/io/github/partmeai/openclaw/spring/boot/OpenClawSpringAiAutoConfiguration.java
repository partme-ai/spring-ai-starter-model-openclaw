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

import io.github.partmeai.openclaw.OpenClawChatModel;
import io.github.partmeai.openclaw.OpenClawEmbeddingModel;
import io.github.partmeai.openclaw.OpenClawResponsesModel;
import io.github.partmeai.openclaw.api.OpenClawApi;
import io.github.partmeai.openclaw.api.OpenClawApi.ChatRequest.Tool;
import io.github.partmeai.openclaw.api.OpenClawChatOptions;
import io.github.partmeai.openclaw.api.OpenClawResponsesApi;
import io.github.partmeai.openclaw.api.SseErrorHandler;
import io.github.partmeai.openclaw.api.common.OpenClawApiConstants;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Auto-configuration for Spring AI OpenClaw integration.
 * <p>
 * Provides:
 * <ul>
 *   <li>{@link OpenClawApi} - Chat Completions API client</li>
 *   <li>{@link OpenClawResponsesApi} - OpenResponses API client</li>
 *   <li>{@link ChatModel} - Spring AI ChatModel implementation</li>
 *   <li>{@link EmbeddingModel} - Spring AI EmbeddingModel implementation</li>
 *   <li>{@link OpenClawResponsesModel} - OpenResponses model (enriched input support)</li>
 * </ul>
 *
 * @author Loong Wan
 */
@AutoConfiguration
@ConditionalOnClass(ChatModel.class)
@EnableConfigurationProperties(OpenClawSpringAiProperties.class)
@ConditionalOnProperty(prefix = OpenClawSpringAiProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class OpenClawSpringAiAutoConfiguration {

	// --------------------------------------------------------------------------
	// API Clients
	// --------------------------------------------------------------------------

	@Bean
	@ConditionalOnMissingBean
	public OpenClawApi openClawApi(OpenClawSpringAiProperties properties,
			ObjectProvider<RestTemplate> restTemplateProvider,
			ObjectProvider<WebClient.Builder> webClientBuilderProvider) {

		RestTemplate restTemplate = restTemplateProvider.getIfAvailable(RestTemplate::new);
		WebClient.Builder webClientBuilder = webClientBuilderProvider.getIfAvailable(WebClient::builder);

		return OpenClawApi.builder()
				.baseUrl(properties.getBaseUrl())
				.restClientBuilder(restTemplate != null ?
						org.springframework.web.client.RestClient.builder(restTemplate) :
						org.springframework.web.client.RestClient.builder())
				.webClientBuilder(webClientBuilder != null ? webClientBuilder : WebClient.builder())
				.sseErrorHandler(SseErrorHandler.DEFAULT)
				.build();
	}

	@Bean
	@ConditionalOnMissingBean
	public OpenClawResponsesApi openClawResponsesApi(OpenClawSpringAiProperties properties,
			ObjectProvider<WebClient.Builder> webClientBuilderProvider) {

		WebClient.Builder webClientBuilder = webClientBuilderProvider.getIfAvailable(WebClient::builder);

		return OpenClawResponsesApi.builder()
				.baseUrl(properties.getBaseUrl())
				.webClientBuilder(webClientBuilder != null ? webClientBuilder : WebClient.builder())
				.sseErrorHandler(SseErrorHandler.DEFAULT)
				.build();
	}

	// --------------------------------------------------------------------------
	// Default ChatOptions
	// --------------------------------------------------------------------------

	@Bean
	@ConditionalOnMissingBean(name = "openClawDefaultChatOptions")
	public OpenClawChatOptions openClawDefaultChatOptions(OpenClawSpringAiProperties properties) {
		OpenClawSpringAiProperties.ChatOptions chatProps = properties.getChat();

		OpenClawChatOptions.Builder builder = OpenClawChatOptions.builder()
				.model(chatProps.getModel())
				.temperature(chatProps.getTemperature())
				.topP(chatProps.getTopP())
				.frequencyPenalty(chatProps.getFrequencyPenalty())
				.presencePenalty(chatProps.getPresencePenalty())
				.maxCompletionTokens(chatProps.getMaxCompletionTokens())
				.maxTokens(chatProps.getMaxTokens())
				.stop(chatProps.getStop())
				.user(chatProps.getUser());

		if (chatProps.getModelOverride() != null && !chatProps.getModelOverride().isEmpty()) {
			// Only set xOpenclawModel if it's not an agent target
			if (!OpenClawApiConstants.isAgentTarget(chatProps.getModelOverride())) {
				builder.xOpenclawModel(chatProps.getModelOverride());
			}
		}

		return builder.build();
	}

	// --------------------------------------------------------------------------
	// Spring AI Model Implementations
	// --------------------------------------------------------------------------

	@Bean
	@ConditionalOnMissingBean(ChatModel.class)
	public ChatModel openClawChatModel(OpenClawApi openClawApi,
			OpenClawChatOptions openClawDefaultChatOptions,
			ToolCallingManager toolCallingManager) {

		return OpenClawChatModel.builder()
				.openclawApi(openClawApi)
				.defaultOptions(openClawDefaultChatOptions)
				.toolCallingManager(toolCallingManager)
				.build();
	}

	@Bean
	@ConditionalOnMissingBean(EmbeddingModel.class)
	public EmbeddingModel openClawEmbeddingModel(OpenClawApi openClawApi,
			OpenClawSpringAiProperties properties) {

		// Embedding options - create a minimal options object for embedding
		OpenClawChatOptions embeddingOptions = OpenClawChatOptions.builder()
				.model(properties.getEmbedding().getModel())
				.build();

		if (properties.getEmbedding().getModelOverride() != null) {
			embeddingOptions.setXOpenclawModel(properties.getEmbedding().getModelOverride());
		}

		return OpenClawEmbeddingModel.builder()
				.api(openClawApi)
				.defaultOptions(embeddingOptions)
				.build();
	}

	@Bean
	@ConditionalOnMissingBean(OpenClawResponsesModel.class)
	public OpenClawResponsesModel openClawResponsesModel(OpenClawResponsesApi openClawResponsesApi,
			OpenClawChatOptions openClawDefaultChatOptions) {

		return OpenClawResponsesModel.builder()
				.responsesApi(openClawResponsesApi)
				.defaultOptions(openClawDefaultChatOptions)
				.build();
	}
}
