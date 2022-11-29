package com.alfa.alfadairy.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final PasswordFormValidator passwordFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;


    @InitBinder("signUpForm")
    public void signUpFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(signUpFormValidator);
    }

    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(passwordFormValidator);
    }

    @GetMapping("/register")
    public String signUpForm(Model model){
        model.addAttribute(new SignUpForm());
        return "account/register";
    }

    @PostMapping("/register")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors){
        if (errors.hasErrors()){
            return "account/register";
        }
        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model){
        Account account = accountRepository.findByEmail(email);

        /** account가 비어있을 때 */
        if (account == null){
            model.addAttribute("error", "wrong.email");
            return "account/checked-email";
        }

        /** token 값이 올바르지 않을 때 */
        if (!account.isValidToken(token)){
            model.addAttribute("error", "wrong.token");
            return "account/checked-email";
        }

        accountService.completeSignUp(account);
        model.addAttribute("userId", account.getUserId());
        return "account/checked-email";
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentAccount Account account, Model model){
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendEmail(@CurrentAccount Account account, Model model){
        if(!account.canSendConfirmEmail()){
            model.addAttribute("error", "인증 이메일은 1시간에 한번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());

            return "account/check-email";
        }

        account.generateEmailCheckToken();
        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    }

    @GetMapping("/profile/{userId}")
    public String viewProfile(@PathVariable String userId, Model model, @CurrentAccount Account account){
        Account byUserId = accountRepository.findByUserId(userId);

        if (byUserId == null){
            throw new IllegalArgumentException(userId + "에 해당하는 사용자가 없습니다.");
        }

        model.addAttribute("account", byUserId);
        model.addAttribute("isOwner", byUserId.equals(account));
        return "account/profile";
    }

    @GetMapping("/profile/address")
    public String setAddress(@CurrentAccount Account account, Model model){

        model.addAttribute("account", account);
        model.addAttribute("isOwner", account.equals(account));
        model.addAttribute(new AddressForm());
        return "account/address";
    }

    @PostMapping("/profile/address")
    public String editAddress(@CurrentAccount Account account, @Valid AddressForm addressForm, Errors errors){

        if(errors.hasErrors()){
            return "account/address";
        }

        accountService.updateAddress(account, addressForm);
        return "redirect:/profile/" + account.getUserId();
    }

    @GetMapping("/profile/password")
    public String passwordForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());

        return "account/password";
    }

    @PostMapping("/profile/password")
    public String updatePassword(@CurrentAccount Account account, Model model,
                                 @Valid PasswordForm passwordForm, Errors errors){

        if(errors.hasErrors()){
            model.addAttribute(account);
            return "account/password";
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        return "redirect:/profile/" + account.getUserId();
    }

    @GetMapping("/email-login")
    public String emailLoginForm(){
        return "account/email-login";
    }

    @PostMapping("/email-login")
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes){
        Account account = accountRepository.findByEmail(email);

        if (account == null){
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }

//        if (!account.canSendConfirmEmail()){
//            model.addAttribute("error", "이메일 로그인은 1시간 뒤에 사용할 수 있습니다.");
//            return "account/email-login";
//        }

        accountService.sendLoginLink(account);
        attributes.addFlashAttribute("message", "로그인 링크를 이메일로 발송했습니다.");
        return "redirect:/email-login";
    }

    @GetMapping("/login-by-email")
    public String loginByEmail(String token, String email, Model model){
        Account account = accountRepository.findByEmail(email);

        if (account == null || !account.isValidToken(token)){
            model.addAttribute("error", "로그인할 수 없습니다.");
            return "account/logged-by-email";
        }

        accountService.login(account);
        return "account/logged-by-email";
    }
}
