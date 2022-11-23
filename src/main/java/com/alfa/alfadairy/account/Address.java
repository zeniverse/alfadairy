package com.alfa.alfadairy.account;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
public class Address {
    private String zipcode;
    private String StreetAddress;
    private String DetailAddress;
}
