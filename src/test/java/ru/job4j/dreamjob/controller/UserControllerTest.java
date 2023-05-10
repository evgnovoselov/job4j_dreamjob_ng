package ru.job4j.dreamjob.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class UserControllerTest {
    private UserService userService;
    private UserController userController;
    private HttpSession testSession;

    @BeforeEach
    public void setUp() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
        testSession = new MockHttpSession();
    }

    @Test
    public void whenGetRegistrationPageThenGetPage() {
        String view = userController.getRegistrationPage();

        assertThat(view).isEqualTo("users/register");
    }

    @Test
    public void whenRegisterUserSuccessThenRedirectVacanciesWithUserInSession() {
        User user = new User(1, "name@example.com", "name", "password");
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(userService.save(userArgumentCaptor.capture())).thenReturn(Optional.of(user));

        ConcurrentModel model = new ConcurrentModel();
        String view = userController.register(user, model, testSession);
        User userSession = (User) testSession.getAttribute("user");
        User userCaptor = userArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(userSession).usingRecursiveComparison().isEqualTo(user);
        assertThat(userCaptor).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    public void whenRegisterUserNotSaveThenGetPageRegisterUserWithMessage() {
        User user = new User(1, "name@example.com", "name", "password");
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(userService.save(userArgumentCaptor.capture())).thenReturn(Optional.empty());

        ConcurrentModel model = new ConcurrentModel();
        String view = userController.register(user, model, testSession);
        String errorMessage = (String) model.getAttribute("error");
        Object userSession = testSession.getAttribute("user");
        User userCaptor = userArgumentCaptor.getValue();

        assertThat(view).isEqualTo("users/register");
        assertThat(errorMessage).isEqualTo("Пользователь с такой почтой уже существует");
        assertThat(userCaptor).usingRecursiveComparison().isEqualTo(user);
        assertThat(userSession).isNull();
    }

    @Test
    public void whenGetLoginPageThenGetPage() {
        String view = userController.getLoginPage();

        assertThat(view).isEqualTo("users/login");
    }

    @Test
    public void whenLoginUserRightThenRedirectVacanciesWithUserInSession() {
        User user = new User(1, "name@example.com", "name", "password");
        when(userService.findByEmailAndPassword(any(), any())).thenReturn(Optional.of(user));

        ConcurrentModel model = new ConcurrentModel();
        String view = userController.loginUser(user, model, testSession);
        User userSession = (User) testSession.getAttribute("user");

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(userSession).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    public void whenLoginUserNotRightThenGetLoginPageWithErrorMessage() {
        when(userService.findByEmailAndPassword(any(), any())).thenReturn(Optional.empty());

        ConcurrentModel model = new ConcurrentModel();
        String view = userController.loginUser(new User(), model, testSession);
        String errorMessage = (String) model.getAttribute("error");
        User userSession = (User) testSession.getAttribute("user");

        assertThat(view).isEqualTo("users/login");
        assertThat(errorMessage).isEqualTo("Почта или пароль введены неверно");
        assertThat(userSession).isNull();
    }

    @Test
    public void whenGetLogoutPageThenSessionInvalidateAndRedirectLoginPage() {
        HttpSession session = mock(HttpSession.class);

        String view = userController.logout(session);

        verify(session).invalidate();
        assertThat(view).isEqualTo("redirect:/users/login");
    }
}
