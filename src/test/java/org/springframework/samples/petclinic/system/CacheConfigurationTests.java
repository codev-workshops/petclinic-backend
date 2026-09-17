/*
 * Copyright 2012-2025 the original author or authors.
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

package org.springframework.samples.petclinic.system;

import javax.cache.CacheManager;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test class for {@link CacheConfiguration}
 */
class CacheConfigurationTests {

	final CacheConfiguration testee = new CacheConfiguration();

	@Test
	@SuppressWarnings("unchecked")
	void customizerCreatesVetsCacheWithStatisticsEnabled() {
		JCacheManagerCustomizer customizer = testee.petclinicCacheConfigurationCustomizer();
		CacheManager cacheManager = mock(CacheManager.class);

		customizer.customize(cacheManager);

		ArgumentCaptor<Configuration<Object, Object>> captor = ArgumentCaptor.forClass(Configuration.class);
		verify(cacheManager).createCache(eq("vets"), captor.capture());
		assertThat(captor.getValue()).isInstanceOf(CompleteConfiguration.class);
		assertThat(((CompleteConfiguration<Object, Object>) captor.getValue()).isStatisticsEnabled()).isTrue();
	}

}
