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
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Owner} aggregate.
 */
class OwnerTests {

	private Owner owner;

	private Pet savedPet;

	@BeforeEach
	void setUp() {
		this.owner = new Owner();
		this.owner.setId(1);
		this.owner.setFirstName("George");
		this.owner.setLastName("Franklin");
		this.savedPet = pet("Leo");
		this.owner.addPet(this.savedPet);
		this.savedPet.setId(10);
	}

	private static Pet pet(String name) {
		Pet pet = new Pet();
		pet.setName(name);
		return pet;
	}

	@Test
	void addPetIgnoresPetThatIsAlreadyPersisted() {
		Pet persisted = pet("Basil");
		persisted.setId(20);

		this.owner.addPet(persisted);

		assertThat(this.owner.getPets()).containsExactly(this.savedPet);
	}

	@Test
	void getPetByNameIgnoresCase() {
		assertThat(this.owner.getPet("LEO")).isSameAs(this.savedPet);
		assertThat(this.owner.getPet("Unknown")).isNull();
	}

	@Test
	void getPetByNameCanSkipUnsavedPets() {
		Pet unsaved = pet("Rosy");
		this.owner.addPet(unsaved);

		assertThat(this.owner.getPet("Rosy", false)).isSameAs(unsaved);
		assertThat(this.owner.getPet("Rosy", true)).isNull();
		assertThat(this.owner.getPet("Leo", true)).isSameAs(this.savedPet);
	}

	@Test
	void getPetByNameSkipsPetsWithoutName() {
		this.owner.addPet(new Pet());

		assertThat(this.owner.getPet((String) null)).isNull();
	}

	@Test
	void getPetByIdIgnoresUnsavedPetsAndUnknownIds() {
		this.owner.addPet(pet("Rosy"));

		assertThat(this.owner.getPet(10)).isSameAs(this.savedPet);
		assertThat(this.owner.getPet(99)).isNull();
		assertThat(this.owner.getPet((Integer) null)).isNull();
	}

	@Test
	void addVisitAttachesVisitToMatchingPet() {
		Visit visit = new Visit();
		visit.setDescription("rabies shot");

		this.owner.addVisit(10, visit);

		assertThat(this.savedPet.getVisits()).containsExactly(visit);
	}

	@Test
	void addVisitRejectsPetOfAnotherOwner() {
		assertThatIllegalArgumentException().isThrownBy(() -> this.owner.addVisit(99, new Visit()))
			.withMessage("Invalid Pet identifier!");
	}

	@Test
	void addVisitRejectsNullArguments() {
		assertThatIllegalArgumentException().isThrownBy(() -> this.owner.addVisit(null, new Visit()))
			.withMessage("Pet identifier must not be null!");
		assertThatIllegalArgumentException().isThrownBy(() -> this.owner.addVisit(10, null))
			.withMessage("Visit must not be null!");
	}

	@Test
	void toStringContainsIdentifyingFields() {
		this.owner.setCity("Madison");
		this.owner.setTelephone("6085551023");

		assertThat(this.owner.toString()).contains("id = 1", "lastName = 'Franklin'", "firstName = 'George'",
				"city = 'Madison'", "telephone = '6085551023'", "new = false");
	}

}
