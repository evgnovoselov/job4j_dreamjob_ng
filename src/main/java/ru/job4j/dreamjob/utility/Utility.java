package ru.job4j.dreamjob.utility;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import ru.job4j.dreamjob.model.User;

public final class Utility {
    private Utility() {
    }

    public static void addUserFromSessionInModel(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            user = new User();
            user.setName("Гость");
        }
        model.addAttribute("user", user);
    }
}
