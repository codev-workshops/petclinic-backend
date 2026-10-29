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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Test class for {@link CacheConfiguration}.
 */
class CacheConfigurationTests {

	@Test
	@SuppressWarnings("unchecked")
	void customizerCreatesVetsCacheWithStatisticsEnabled() {
		CacheManager cacheManager = mock(CacheManager.class);
		ArgumentCaptor<Configuration<Object, Object>> configuration = ArgumentCaptor.forClass(Configuration.class);

		new CacheConfiguration().petclinicCacheConfigurationCustomizer().customize(cacheManager);

		verify(cacheManager).createCache(eq("vets"), configuration.capture());
		assertThat(configuration.getValue()).isInstanceOfSatisfying(MutableConfiguration.class,
				config -> assertThat(config.isStatisticsEnabled()).isTrue());
	}

}
