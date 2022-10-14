package com.alfa.alfadairy.account;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;


    public void processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .userId(signUpForm.getUserId())
                .email(signUpForm.getEmail())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .build();

        Account newAccount = accountRepository.save(account);
        return newAccount;
    }

    private void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("알파축산, 회원 가입 인증 ");
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() +"&email=" + newAccount.getEmail());

        javaMailSender.send(mailMessage);
    }
}
