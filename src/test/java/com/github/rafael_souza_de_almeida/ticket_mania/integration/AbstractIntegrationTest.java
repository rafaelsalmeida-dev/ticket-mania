package com.github.rafael_souza_de_almeida.ticket_mania.integration;

import com.github.rafael_souza_de_almeida.ticket_mania.TicketManiaApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = TicketManiaApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;
}
