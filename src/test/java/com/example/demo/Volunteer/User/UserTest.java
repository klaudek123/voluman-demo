package com.example.demo.Volunteer.User;

import com.example.demo.Volunteer.Volunteer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1L, "test@example.com", "securePassword123", null, new Volunteer());
    }

    @Test
    void testConstructorInitialization() {
        assertEquals(1L, user.getUserId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("securePassword123", user.getPassword());
        assertNotNull(user.getVolunteer());
    }

    @Test
    void testSetAndGetUserId() {
        user.setUserId(2L);
        assertEquals(2L, user.getUserId());
    }

    @Test
    void testSetAndGetEmail() {
        user.setEmail("newemail@example.com");
        assertEquals("newemail@example.com", user.getEmail());
    }

    @Test
    void testSetAndGetPassword() {
        user.setPassword("newSecurePassword456");
        assertEquals("newSecurePassword456", user.getPassword());
    }

    @Test
    void testSetAndGetVolunteer() {
        Volunteer newVolunteer = new Volunteer();
        user.setVolunteer(newVolunteer);
        assertEquals(newVolunteer, user.getVolunteer());
    }

    @Test
    void testEmailValidation() {
        user.setEmail("invalid-email");
        assertEquals("invalid-email", user.getEmail());
        // Jeśli jest walidacja emaila, dodaj sprawdzenie, czy rzuca wyjątek lub zwraca odpowiedni komunikat
    }

    @Test
    void testEqualsAndHashCode() {
        User sameUser = new User(1L, "test@example.com", "securePassword123", null, user.getVolunteer());
        User differentUser = new User(2L, "other@example.com", "otherPassword", null, new Volunteer());

        assertEquals(user, sameUser);
        assertNotEquals(user, differentUser);
        assertEquals(user.hashCode(), sameUser.hashCode());
        assertNotEquals(user.hashCode(), differentUser.hashCode());
    }

    @Test
    void testNullValues() {
        user.setUserId(null);
        user.setEmail(null);
        user.setPassword(null);
        user.setVolunteer(null);

        assertNull(user.getUserId());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getVolunteer());
    }
}
