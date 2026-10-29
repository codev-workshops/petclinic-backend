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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.ServletException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Test class for the {@link PetController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@WebMvcTest(value = PetController.class,
		includeFilters = @ComponentScan.Filter(value = PetTypeFormatter.class, type = FilterType.ASSIGNABLE_TYPE))
@DisabledInNativeImage
@DisabledInAotMode
class PetControllerTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	@MockitoBean
	private PetTypeRepository types;

	private Owner owner;

	@BeforeEach
	void setup() {
		PetType cat = new PetType();
		cat.setId(3);
		cat.setName("hamster");
		given(this.types.findPetTypes()).willReturn(List.of(cat));

		this.owner = createOwnerWithPets();
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));
	}

	private Owner createOwnerWithPets() {
		PetType hamster = this.types.findPetTypes().get(0);
		Owner newOwner = new Owner();
		Pet pet = new Pet();
		Pet dog = new Pet();
		newOwner.addPet(pet);
		newOwner.addPet(dog);
		pet.setId(TEST_PET_ID);
		dog.setId(TEST_PET_ID + 1);
		pet.setName("petty");
		pet.setType(hamster);
		pet.setBirthDate(LocalDate.of(2010, 1, 1));
		dog.setName("doggy");
		return newOwner;
	}

	@Test
	void testInitCreationForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/new", TEST_OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"))
			.andExpect(model().attributeExists("pet"));
	}

	@Test
	void testProcessCreationFormSuccess() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Nested
	class ProcessCreationFormHasErrors {

		@Test
		void testProcessCreationFormWithBlankName() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "\t \n")
					.param("birthDate", "2015-02-12"))
				.andExpect(model().attributeHasNoErrors("owner"))
				.andExpect(model().attributeHasErrors("pet"))
				.andExpect(model().attributeHasFieldErrors("pet", "name"))
				.andExpect(model().attributeHasFieldErrorCode("pet", "name", "required"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

		@Test
		void testProcessCreationFormWithDuplicateName() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "petty")
					.param("birthDate", "2015-02-12"))
				.andExpect(model().attributeHasNoErrors("owner"))
				.andExpect(model().attributeHasErrors("pet"))
				.andExpect(model().attributeHasFieldErrors("pet", "name"))
				.andExpect(model().attributeHasFieldErrorCode("pet", "name", "duplicate"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

		@Test
		void testProcessCreationFormWithMissingPetType() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "Betty")
					.param("birthDate", "2015-02-12"))
				.andExpect(model().attributeHasNoErrors("owner"))
				.andExpect(model().attributeHasErrors("pet"))
				.andExpect(model().attributeHasFieldErrors("pet", "type"))
				.andExpect(model().attributeHasFieldErrorCode("pet", "type", "required"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

		@Test
		void testProcessCreationFormWithInvalidBirthDate() throws Exception {
			LocalDate currentDate = LocalDate.now();
			String futureBirthDate = currentDate.plusMonths(1).toString();

			mockMvc
				.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "Betty")
					.param("birthDate", futureBirthDate))
				.andExpect(model().attributeHasNoErrors("owner"))
				.andExpect(model().attributeHasErrors("pet"))
				.andExpect(model().attributeHasFieldErrors("pet", "birthDate"))
				.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "typeMismatch.birthDate"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

		@Test
		void testInitUpdateForm() throws Exception {
			mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("pet"))
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

	}

	@Test
	void testProcessUpdateFormSuccess() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Nested
	class ProcessUpdateFormHasErrors {

		@Test
		void testProcessUpdateFormWithInvalidBirthDate() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).param("name", " ")
					.param("birthDate", "2015/02/12"))
				.andExpect(model().attributeHasNoErrors("owner"))
				.andExpect(model().attributeHasErrors("pet"))
				.andExpect(model().attributeHasFieldErrors("pet", "birthDate"))
				.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "typeMismatch"))
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

		@Test
		void testProcessUpdateFormWithBlankName() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).param("name", "  ")
					.param("birthDate", "2015-02-12"))
				.andExpect(model().attributeHasNoErrors("owner"))
				.andExpect(model().attributeHasErrors("pet"))
				.andExpect(model().attributeHasFieldErrors("pet", "name"))
				.andExpect(model().attributeHasFieldErrorCode("pet", "name", "required"))
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

	}

	@Test
	void processCreationFormAddsPetToOwnerAndSaves() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection())
			.andExpect(flash().attribute("message", "New Pet has been Added"));

		verify(this.owners).save(this.owner);
		Pet betty = this.owner.getPet("Betty");
		assertThat(betty).isNotNull();
		assertThat(betty.getType().getName()).isEqualTo("hamster");
		assertThat(betty.getBirthDate()).isEqualTo(LocalDate.of(2015, 2, 12));
	}

	@Test
	void processCreationFormRejectsDuplicateNameIgnoringCase() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "PETTY")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrorCode("pet", "name", "duplicate"))
			.andExpect(view().name("pets/createOrUpdatePetForm"));

		verify(this.owners, never()).save(any(Owner.class));
	}

	@Test
	void processCreationFormAcceptsBirthDateOfToday() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "Newborn")
				.param("type", "hamster")
				.param("birthDate", LocalDate.now().toString()))
			.andExpect(status().is3xxRedirection());

		verify(this.owners).save(this.owner);
	}

	@Test
	void processCreationFormWithUnknownOwnerThrows() {
		given(this.owners.findById(99)).willReturn(Optional.empty());

		assertThatThrownBy(() -> mockMvc.perform(post("/owners/{ownerId}/pets/new", 99).param("name", "Betty")))
			.hasRootCauseInstanceOf(IllegalArgumentException.class)
			.rootCause()
			.hasMessageStartingWith("Owner not found with id: 99");
	}

	@Test
	void processUpdateFormUpdatesExistingPetAndSaves() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
				.param("id", String.valueOf(TEST_PET_ID))
				.param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection())
			.andExpect(flash().attribute("message", "Pet details has been edited"));

		verify(this.owners).save(this.owner);
		Pet updated = this.owner.getPet(TEST_PET_ID);
		assertThat(updated.getName()).isEqualTo("Betty");
		assertThat(updated.getType().getName()).isEqualTo("hamster");
		assertThat(updated.getBirthDate()).isEqualTo(LocalDate.of(2015, 2, 12));
		assertThat(this.owner.getPets()).hasSize(2);
	}

	@Test
	void processUpdateFormKeepingOwnNameIsNotADuplicate() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
				.param("id", String.valueOf(TEST_PET_ID))
				.param("name", "petty")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection());

		verify(this.owners).save(this.owner);
	}

	@Test
	void processUpdateFormForPetNotOwnedByOwnerFailsWithoutSaving() {
		assertThatThrownBy(
				() -> mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, 99).param("id", "99")
					.param("name", "Intruder")
					.param("type", "hamster")
					.param("birthDate", "2015-02-12")))
			.isInstanceOf(ServletException.class);

		verify(this.owners, never()).save(any(Owner.class));
		assertThat(this.owner.getPets()).hasSize(2);
	}

	@Nested
	class ProcessUpdateFormBusinessRules {

		@Test
		void rejectsRenamingToAnotherPetsName() throws Exception {
			// Without open-session-in-view, findOwner and findPet load separate instances
			given(owners.findById(TEST_OWNER_ID)).willAnswer(invocation -> Optional.of(createOwnerWithPets()));

			mockMvc
				.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
					.param("id", String.valueOf(TEST_PET_ID))
					.param("name", "doggy")
					.param("type", "hamster")
					.param("birthDate", "2015-02-12"))
				.andExpect(status().isOk())
				.andExpect(model().attributeHasFieldErrorCode("pet", "name", "duplicate"))
				.andExpect(view().name("pets/createOrUpdatePetForm"));

			verify(owners, never()).save(any(Owner.class));
		}

		@Test
		void rejectsFutureBirthDate() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
					.param("id", String.valueOf(TEST_PET_ID))
					.param("name", "petty")
					.param("type", "hamster")
					.param("birthDate", LocalDate.now().plusDays(1).toString()))
				.andExpect(status().isOk())
				.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "typeMismatch.birthDate"))
				.andExpect(view().name("pets/createOrUpdatePetForm"));

			verify(owners, never()).save(any(Owner.class));
		}

		@Test
		void withUnknownOwnerThrows() {
			given(owners.findById(99)).willReturn(Optional.empty());

			assertThatThrownBy(() -> mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", 99, TEST_PET_ID)))
				.hasRootCauseInstanceOf(IllegalArgumentException.class)
				.rootCause()
				.hasMessageStartingWith("Owner not found with id: 99");
		}

	}

}
