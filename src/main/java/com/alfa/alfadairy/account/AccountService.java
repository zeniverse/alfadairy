package com.alfa.alfadairy.account;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;


    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);

        return newAccount;
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

    public void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("알파축산, 회원 가입 인증 ");
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() +"&email=" + newAccount.getEmail());

        javaMailSender.send(mailMessage);
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    public void login(Account account){
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Account account = accountRepository.findByUserId(userId);

        if (account == null){
            throw new UsernameNotFoundException(userId);
        }

        return new UserAccount(account);
    }

    public void updateAddress(Account account, AddressForm addressForm) {
        Address address = Address.builder()
                .streetAddress(addressForm.getStreetAddress())
                .detailAddress(addressForm.getDetailAddress())
                .zipcode(addressForm.getZipcode())
                .build();

        account.updateAddress(address);
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPassword) {
        account.passwordUpdate(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void sendLoginLink(Account account) {
        account.generateEmailCheckToken();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getEmail());
        mailMessage.setSubject("알파축산, 로그인 링크");
        mailMessage.setText("/login-by-email?token=" + account.getEmailCheckToken()
                + "&email=" + account.getEmail());

        javaMailSender.send(mailMessage);
    }
}
