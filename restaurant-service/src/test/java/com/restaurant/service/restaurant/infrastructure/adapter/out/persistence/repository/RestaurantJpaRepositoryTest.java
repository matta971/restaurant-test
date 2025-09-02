package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.repository;

import com.restaurant.service.restaurant.domain.model.Restaurant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.show-sql=true"
})
@DisplayName("Restaurant JPA Repository Tests")
class RestaurantJpaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RestaurantJpaRepository repository;

    @Test
    @DisplayName("Should save and find restaurant by ID")
    void shouldSaveAndFindRestaurantById() {
        // Given
        Restaurant restaurant = new Restaurant(
                "Le Petit Bistro",
                "123 Rue de la Paix, Paris",
                "+33 1 42 86 87 88",
                "contact@petitbistro.fr",
                50
        );

        // When
        Restaurant saved = repository.save(restaurant);
        entityManager.flush();
        entityManager.clear();
        
        Optional<Restaurant> found = repository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Le Petit Bistro");
        assertThat(found.get().isActive()).isTrue();
    }
}