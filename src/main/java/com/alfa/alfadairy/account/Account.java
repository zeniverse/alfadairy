package com.alfa.alfadairy.account;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Getter
public class Account {

    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String userId;

    @Column(unique = true)
    private String email;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;
    
    private LocalDateTime emailCheckTokenGeneratedAt;

    private LocalDateTime joinedAt;

    // TODO : Order Entity를 만들고 난 후에 연관관계 설정해야한다
//    private List<Order> orders = new ArrayList<>();

    private String city;

    private String state;

    private String zipcode;

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }
}
