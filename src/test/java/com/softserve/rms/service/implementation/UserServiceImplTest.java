package com.softserve.rms.service.implementation;

import com.softserve.rms.dto.template.ResourceTemplateDTO;
import com.softserve.rms.dto.user.PasswordEditDto;
import com.softserve.rms.dto.user.PermissionUserDto;
import com.softserve.rms.dto.user.RegistrationDto;
import com.softserve.rms.dto.user.UserEditDto;
import com.softserve.rms.entities.Role;
import com.softserve.rms.entities.User;
import com.softserve.rms.exceptions.NotFoundException;
import com.softserve.rms.exceptions.NotSavedException;
import com.softserve.rms.exceptions.user.WrongEmailException;
import com.softserve.rms.exceptions.user.WrongPasswordException;
import com.softserve.rms.repository.AdminRepository;
import com.softserve.rms.repository.UserRepository;
import com.sun.security.auth.UserPrincipal;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.security.Principal;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;


@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private DataSource dataSource;
    @Mock
    private Principal principal;
    @Mock
    private UserServiceImpl userService2;
    @Mock
    private AdminRepository adminRepository;
    @Mock
    private JdbcTemplate jdbcTemplate=new JdbcTemplate(new DriverManagerDataSource());
    @InjectMocks
    private UserServiceImpl userService;

    private Principal principal2 = new UserPrincipal("test3@gmail.com");

    User testUser = new User(1L, "first1", "last1", "email1","phone1","password1",true, new Role(1L,"USER"),"imageUrl","google","324253674", Collections.emptyList(),"token",Collections.emptyList());

    private User user =
            User.builder()
                    .id(1L)
                    .firstName("test")
                    .lastName("test")
                    .email("test@gmail.com")
                    .phone("+380111111111")
                    .password("qwertQWE!@1")
                    .build();

    private User secondUser =
            User.builder()
                    .id(1L)
                    .firstName("test2")
                    .lastName("test2")
                    .email("test2@gmail.com")
                    .phone("+380222222222")
                    .password("qwertQWE!@2")
                    .build();

    private User thirdUser =
            User.builder()
                    .id(1L)
                    .firstName("test3")
                    .lastName("test3")
                    .email("test3@gmail.com")
                    .phone("+380333333333")
                    .password("mwertQWE!@2")
                    .role(new Role(1L, "ROLE_MANAGER"))
                    .build();

    private RegistrationDto registrationDto =
            RegistrationDto.builder()
                    .firstName("test")
                    .lastName("test")
                    .email("test@gmail.com")
                    .phone("+380111111111")
                    .password("qwertQWE!@1")
                    .build();
    private UserEditDto userEditDto =
            new UserEditDto("test","test",
                    "+380111111111");

    private PasswordEditDto passwordEditDto =
            new PasswordEditDto("qwertQWE!@1","qwertyQQ1!!");

    private PermissionUserDto permissionUserDto =
            new PermissionUserDto("test3@gmail.com", "test3", "test3", new Role(1L, "ROLE_MANAGER"));

    @Test
    public void saveTest() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        userService.save(registrationDto);
        verify(userRepository,times(1)).save(any());
        assertEquals(user, userRepository.save(user));
    }

    @Test(expected = NotSavedException.class)
    public void saveWithErrorTest() {
        when(userRepository.save(any())).thenReturn(null);
        userService.save(registrationDto);
    }

    @Test
    public void updateTest(){
        when(userRepository.findUserByEmail(any())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        userService.update(userEditDto,user.getEmail());
        verify(userRepository,times(1)).save(any());
    }

    @Test(expected = NotFoundException.class)
    public void updateWrongEmailTest(){
        when(userRepository.findUserByEmail(any())).thenReturn(Optional.empty());
        userService.update(userEditDto,any());
        verify(userRepository,times(1)).save(any());
    }

    @Test
    public void editPasswordTest() {
        when(userRepository.findUserByEmail(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(),any())).thenReturn(true);
        userService.editPassword(passwordEditDto,
                user.getEmail());
        verify(userRepository,times(1)).save(any());
    }


    @Test(expected = NotFoundException.class)
    public void editPasswordWrongEmailTest(){
        userService.editPassword(passwordEditDto,any());
        when(userRepository.findUserByEmail(any())).thenReturn(Optional.empty());
        verify(userRepository,times(1)).save(any());
    }

    @Test
    public void getUserByEmailTest(){

        when(userRepository.findUserByEmail("email1")).thenReturn(Optional.of(testUser));

        User expectedUser = userService.getUserByEmail("email1");

        assertEquals(testUser, expectedUser);

        verify(userRepository, times(1)).findUserByEmail("email1");
    }

    @Test(expected = NotFoundException.class)
    public void getByEmailExceptionTest() {

        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());

        userService.getUserByEmail(anyString());
    }

    @Test
    public void getUserByIdTest() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User expectedUser = userService.getById(1L);

        assertEquals(testUser, expectedUser);

        verify(userRepository, times(1)).findById(1L);
    }

    @Test(expected = NotFoundException.class)
    public void getByIdExceptionTest() {

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        userService.getById(anyLong());
    }

    @Test
    public void getUserRoleTestSuccess() {
        lenient().when(userService2.getUserByEmail(anyString())).thenReturn(thirdUser);
        userService2.getUserRole(principal2);
    }

    @Test(expected = NotFoundException.class)
    public void getUserRoleTestFail() {
        doThrow(new NotFoundException("exception")).when(userRepository).findUserByEmail(anyString());
        userService.getUserByEmail(anyString());
    }

    @Test
    public void getUsersTestSuccess() {
        Page<User> userPage = new PageImpl<>(Collections.singletonList(thirdUser));
        Mockito.when(adminRepository.getAllByEnabled(anyBoolean(), any(Pageable.class))).thenReturn(userPage);
        List<PermissionUserDto> resourceTemplateDTOs = Collections.singletonList(permissionUserDto);
        Assert.assertEquals(resourceTemplateDTOs, userService.getUsers(1, 1).getContent());
    }
}
