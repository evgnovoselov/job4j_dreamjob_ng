package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IndexControllerTest {
    @Test
    public void whenGetIndexThenGetIndexView() {
        IndexController indexController = new IndexController();

        String view = indexController.getIndex();

        assertThat(view).isEqualTo("index");
    }
}
