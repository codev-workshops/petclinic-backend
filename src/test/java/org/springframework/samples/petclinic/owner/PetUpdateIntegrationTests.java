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
package org.springframework.samples.petclinic.owner;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Exercises the pet update rules against the real repositories and sample data.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PetUpdateIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void rejectsRenamingPetToNameOfOwnersOtherPet() throws Exception {
		// Owner 6 (Jean Coleman) owns Samantha (id 7) and Max (id 8)
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/edit", 6, 8).param("id", "8")
				.param("name", "Samantha")
				.param("type", "dog")
				.param("birthDate", "2012-09-04"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrorCode("pet", "name", "duplicate"))
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

}
