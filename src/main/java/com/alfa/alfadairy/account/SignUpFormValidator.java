package com.alfa.alfadairy.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUpForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) target;

        if(accountRepository.existsByUserId(signUpForm.getUserId())){
            errors.rejectValue("userId", "invalid.userId",
                    new Object[]{signUpForm.getUserId()}, "이미 사용중인 아이디입니다.");
        }

        if(accountRepository.existsByEmail(signUpForm.getEmail())){
            errors.rejectValue("email", "invalid.email",
                    new Object[]{signUpForm.getUserId()}, "이미 사용중인 이메일입니다.");
        }
    }
}
