package com.luemi.pehuame.service;

import com.luemi.pehuame.dto.RegisterWorkerRequest;
import com.luemi.pehuame.dto.UserDTO;
import com.luemi.pehuame.enums.UserRole;
import com.luemi.pehuame.mapper.UserMapper;
import com.luemi.pehuame.model.Farm;
import com.luemi.pehuame.model.User;
import com.luemi.pehuame.repository.FarmRepository;
import com.luemi.pehuame.repository.UserRepository;
import com.luemi.pehuame.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private UserMapper userMapper = new UserMapper();

    @InjectMocks
    private UserService userService;

    private Farm farmA;
    private Farm farmB;

    @BeforeEach
    void setUp() {
        farmA = new Farm();
        farmA.setId(1);

        farmB = new Farm();
        farmB.setId(2);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Integer userId, Farm farm, UserRole role) {
        User user = new User();
        user.setId(userId);
        user.setEmail("caller@farm.test");
        user.setPasswordHash("hash");
        user.setFarm(farm);
        user.setRole(role);

        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private User userInFarm(Integer id, Farm farm) {
        User user = new User();
        user.setId(id);
        user.setEmail("user" + id + "@farm.test");
        user.setPasswordHash("stored-hash");
        user.setFarm(farm);
        user.setRole(UserRole.WORKER);
        user.setActive(true);
        return user;
    }

    @Test
    void listByFarm_returnsUsers_whenOwnFarm() {
        authenticateAs(99, farmA, UserRole.ADMIN);
        when(userRepository.findByFarmId(1)).thenReturn(List.of(userInFarm(2, farmA)));

        List<UserDTO> result = userService.listByFarm(1);

        assertThat(result).hasSize(1);
    }

    @Test
    void listByFarm_throwsFarmNotFound_whenDifferentFarm() {
        authenticateAs(99, farmA, UserRole.ADMIN);

        assertThatThrownBy(() -> userService.listByFarm(2))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Farm not found");
    }

    @Test
    void getById_returnsUser_whenSameFarm() {
        authenticateAs(99, farmA, UserRole.ADMIN);
        when(userRepository.findById(2)).thenReturn(Optional.of(userInFarm(2, farmA)));

        UserDTO result = userService.getById(2);

        assertThat(result.getId()).isEqualTo(2);
    }

    @Test
    void getById_throwsUserNotFound_whenDifferentFarm() {
        authenticateAs(99, farmA, UserRole.ADMIN);
        when(userRepository.findById(2)).thenReturn(Optional.of(userInFarm(2, farmB)));

        assertThatThrownBy(() -> userService.getById(2))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void createWorker_throwsFarmNotFound_whenTargetFarmNotOwn() {
        authenticateAs(99, farmA, UserRole.ADMIN);
        RegisterWorkerRequest request = new RegisterWorkerRequest();

        assertThatThrownBy(() -> userService.createWorker(2, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Farm not found");
    }

    @Test
    void update_allowsSelfUpdate() {
        authenticateAs(2, farmA, UserRole.WORKER);
        User existing = userInFarm(2, farmA);
        UserDTO dto = new UserDTO();
        dto.setName("New name");
        dto.setPhone("555");

        when(userRepository.findById(2)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDTO result = userService.update(2, dto);

        assertThat(result.getName()).isEqualTo("New name");
    }

    @Test
    void update_allowsAdminToUpdateSameFarmUser() {
        authenticateAs(99, farmA, UserRole.ADMIN);
        User existing = userInFarm(2, farmA);
        UserDTO dto = new UserDTO();
        dto.setName("New name");

        when(userRepository.findById(2)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDTO result = userService.update(2, dto);

        assertThat(result.getName()).isEqualTo("New name");
    }

    @Test
    void update_throwsUserNotFound_whenWorkerUpdatesSomeoneElse() {
        authenticateAs(3, farmA, UserRole.WORKER);
        User existing = userInFarm(2, farmA);

        when(userRepository.findById(2)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.update(2, new UserDTO()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void changePassword_updatesHash_whenSelfAndCurrentPasswordMatches() {
        authenticateAs(2, farmA, UserRole.WORKER);
        User existing = userInFarm(2, farmA);

        when(userRepository.findById(2)).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("old-pass", "stored-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-pass")).thenReturn("new-hash");

        userService.changePassword(2, "old-pass", "new-pass");

        assertThat(existing.getPasswordHash()).isEqualTo("new-hash");
    }

    @Test
    void changePassword_throws_whenTargetIsNotCaller() {
        authenticateAs(3, farmA, UserRole.ADMIN);

        assertThatThrownBy(() -> userService.changePassword(2, "old-pass", "new-pass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void changePassword_throws_whenCurrentPasswordWrong() {
        authenticateAs(2, farmA, UserRole.WORKER);
        User existing = userInFarm(2, farmA);

        when(userRepository.findById(2)).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrong-pass", "stored-hash")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(2, "wrong-pass", "new-pass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Current password is incorrect");
    }

    @Test
    void deactivate_deactivatesUser_whenAdminSameFarm() {
        authenticateAs(99, farmA, UserRole.ADMIN);
        User existing = userInFarm(2, farmA);

        when(userRepository.findById(2)).thenReturn(Optional.of(existing));

        userService.deactivate(2);

        assertThat(existing.getActive()).isFalse();
    }

    @Test
    void deactivate_throwsUserNotFound_whenNotAdmin() {
        authenticateAs(3, farmA, UserRole.WORKER);
        User existing = userInFarm(2, farmA);

        when(userRepository.findById(2)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.deactivate(2))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void deactivate_throwsUserNotFound_whenDifferentFarm() {
        authenticateAs(99, farmA, UserRole.ADMIN);
        User existing = userInFarm(2, farmB);

        when(userRepository.findById(2)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.deactivate(2))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }
}
