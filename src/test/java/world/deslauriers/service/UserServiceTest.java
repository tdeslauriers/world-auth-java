package world.deslauriers.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import world.deslauriers.model.database.*;
import world.deslauriers.model.profile.ProfileDto;
import world.deslauriers.repository.UserRepository;
import world.deslauriers.repository.UserRoleRepository;

import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class UserServiceTest {

    @Inject
    private UserRepository userRepository;

    @Inject
    private RoleService roleService;

    @Inject
    private UserRoleRepository userRoleRepository;

    @Inject
    private PasswordEncoderService passwordEncoderService;

    @Inject
    private UserService userService;

    private static final String VALID_EMAIL = "tom@deslauriers.world";
    private static final String VALID_CLEAR_PASSWORD = "H~Z\\ysbY[fOg|4^86:BQ";
    private static final String VALID_FIRST = "tom";
    private static final String VALID_LAST = "deslauriers";
    private static final String VALID_ROLE_1 = "GALLERY_READ";
    private static final String VALID_ROLE_2 = "GALLERY_EDIT";

    @Test
    void testUserServiceMethods(){

        var user = userRepository.save(new User(
                VALID_EMAIL, passwordEncoderService.encode(VALID_CLEAR_PASSWORD), VALID_FIRST, VALID_LAST, LocalDate.now(), true, false, false));

        var ur1 = userRoleRepository.save(new UserRole(user, roleService.save(new Role(VALID_ROLE_1, "Gallery Read", "disc"))));
        var ur2 = userRoleRepository.save(new UserRole(user, roleService.save(new Role(VALID_ROLE_2, "Gallery Edit", "disc"))));

        user = userService.lookupUserByUsername(user.username()).get();
        assertNotNull(user);
        assertEquals(VALID_EMAIL, user.username());
        assertEquals(2, user.userRoles().size());

        var addresses = new HashSet<Address>();
        addresses.add(new Address("456 Test Street", "City", "CA", "55555"));

        var phones = new HashSet<Phone>();
        phones.add((new Phone("4445556666", "WORK")));

        userService.updateUser(user, new ProfileDto(
                user.id(),
                user.username(),
                user.firstname(),
                user.lastname(),
                user.dateCreated(),
                user.enabled(),
                user.accountExpired(),
                user.accountLocked(),
                null,
                addresses,
                phones));

        // field changes
        // bad/malicious inputs require direct integration testing
        var updated = userService.lookupUserByUsername(user.username()).get();
        var addressId = updated.userAddresses().stream().filter(userAddress -> userAddress.address().address().equals("456 Test Street")).findFirst().get().id();
        addresses = new HashSet<Address>();
        addresses.add(new Address(addressId, "789 Different Ave", "City", "CA", "55555"));

        userService.updateUser(user, new ProfileDto(
                user.id(),
                user.username(),
                VALID_FIRST,
                "007",
                user.dateCreated(),
                false,
                user.accountExpired(),
                user.accountLocked(),
                null,
                addresses,
                null)); // needs id so putting null to avoid error

        // user get profile
        var profile = userService.getProfile(user.username());
        assertNotNull(profile.get().id());
        assertEquals(user.id(), profile.get().id());
        assertEquals(user.username(), profile.get().username());
        assertEquals(user.firstname(), profile.get().firstname());
        assertEquals("007", profile.get().lastname());
        assertFalse(profile.get().enabled());
        assertFalse(profile.get().accountExpired());
        assertFalse(profile.get().accountLocked());

        assertTrue(userService.getProfile("Not a real name").isEmpty());

        // admin get profile by id
        profile = userService.getProfileById(user.id());
        assertNotNull(profile.get().id());
        assertEquals(user.id(), profile.get().id());
        assertEquals(user.username(), profile.get().username());
        assertEquals(user.firstname(), profile.get().firstname());
        assertEquals("007", profile.get().lastname());
        assertFalse(profile.get().enabled());
        assertFalse(profile.get().accountExpired());
        assertFalse(profile.get().accountLocked());

        assertTrue(userService.getProfileById(666L).isEmpty());


        // find all
        var all = userRepository.findAll();
        assertNotNull(all);
        assertTrue(all.iterator().hasNext());
        assertNotNull(all.iterator().next().id());
        assertEquals(all.iterator().next().firstname(), VALID_FIRST);
    }

}
