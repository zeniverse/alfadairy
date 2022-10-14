package com.alfa.alfadairy.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired MockMvc mvc;
    @Autowired AccountRepository accountRepository;

    @MockBean JavaMailSender javaMailSender;

    @DisplayName("이메일 전송 - 입력값 오류")
    @Test
    public void checkEmailToken_wrong_input() throws Exception {
        mvc.perform(get("/check-email-token")
                        .param("token", "adfdfasdfas")
                        .param("email", "test@dddd.com")
                )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"));
    }

    @DisplayName("이메일 전송 - 입력값 정상")
    @Test
    public void checkEmailToken() throws Exception {
        Account account = Account.builder()
                .userId("test")
                .email("test@test.com")
                .password("123456789")
                .build();
        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();


        mvc.perform(get("/check-email-token")
                        .param("token", newAccount.getEmailCheckToken())
                        .param("email", newAccount.getEmail())
                )
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("userId"))
                .andExpect(view().name("account/checked-email"));
    }

    @DisplayName("회원 가입 화면이 보이는지 확인")
    @Test
    public void signUpView() throws Exception {
        mvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"));
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    public void signUp_With_WrongInput() throws Exception {
        mvc.perform(post("/sign-up")
                        .param("userId", "test")
                        .param("email", "email....")
                        .param("password", "1234")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"));
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    public void signUp() throws Exception {
        mvc.perform(post("/sign-up")
                        .param("userId", "test")
                        .param("email", "test@test.com")
                        .param("password", "123456789")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        Account account = accountRepository.findByEmail("test@test.com");
        assertNotNull(account);
        assertNotNull(account.getEmailCheckToken());
        assertNotEquals(account.getPassword(), "123456789");
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }
}