/*
 * Copyright 2012-2024 the original author or authors.
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

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Owner} aggregate rules.
 */
class OwnerTests {

	private Owner owner;

	private Pet savedPet;

	private Pet newPet;

	@BeforeEach
	void setup() {
		owner = new Owner();
		savedPet = pet("Leo");
		owner.addPet(savedPet);
		savedPet.setId(1);
		newPet = pet("Basil");
		owner.addPet(newPet);
	}

	private static Pet pet(String name) {
		Pet pet = new Pet();
		pet.setName(name);
		pet.setBirthDate(LocalDate.of(2020, 1, 1));
		return pet;
	}

	@Test
	void addPetAddsOnlyNewPets() {
		Pet alreadySaved = pet("Rex");
		alreadySaved.setId(42);

		owner.addPet(alreadySaved);

		assertThat(owner.getPets()).containsExactly(savedPet, newPet);
	}

	@Test
	void getPetByNameIsCaseInsensitive() {
		assertThat(owner.getPet("leo")).isSameAs(savedPet);
		assertThat(owner.getPet("LEO")).isSameAs(savedPet);
		assertThat(owner.getPet("basil")).isSameAs(newPet);
	}

	@Test
	void getPetByNameReturnsNullWhenUnknown() {
		assertThat(owner.getPet("Max")).isNull();
		assertThat(owner.getPet((String) null)).isNull();
	}

	@Test
	void getPetByNameIgnoresNewPetsWhenRequested() {
		assertThat(owner.getPet("Basil", true)).isNull();
		assertThat(owner.getPet("Basil", false)).isSameAs(newPet);
		assertThat(owner.getPet("Leo", true)).isSameAs(savedPet);
	}

	@Test
	void getPetByNameSkipsPetsWithoutName() {
		owner.getPets().add(new Pet());

		assertThat(owner.getPet("Leo")).isSameAs(savedPet);
	}

	@Test
	void getPetByIdSkipsNewPets() {
		assertThat(owner.getPet(1)).isSameAs(savedPet);
		assertThat(owner.getPet(2)).isNull();
		assertThat(owner.getPet((Integer) null)).isNull();
	}

	@Test
	void addVisitAddsVisitToMatchingPet() {
		Visit visit = new Visit();
		visit.setDescription("checkup");

		owner.addVisit(1, visit);

		assertThat(savedPet.getVisits()).containsExactly(visit);
		assertThat(newPet.getVisits()).isEmpty();
	}

	@Test
	void addVisitRejectsUnknownPet() {
		assertThatIllegalArgumentException().isThrownBy(() -> owner.addVisit(99, new Visit()))
			.withMessage("Invalid Pet identifier!");
	}

	@Test
	void addVisitRejectsNullArguments() {
		assertThatIllegalArgumentException().isThrownBy(() -> owner.addVisit(null, new Visit()))
			.withMessage("Pet identifier must not be null!");
		assertThatIllegalArgumentException().isThrownBy(() -> owner.addVisit(1, null))
			.withMessage("Visit must not be null!");
	}

	@Test
	void toStringIncludesContactDetails() {
		owner.setId(7);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		owner.setCity("Madison");

		assertThat(owner.toString()).contains("id = 7", "lastName = 'Franklin'", "city = 'Madison'");
	}

}
