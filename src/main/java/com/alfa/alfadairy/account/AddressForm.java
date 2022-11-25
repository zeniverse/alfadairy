package com.alfa.alfadairy.account;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class AddressForm {

    @NotBlank
    @Length(min = 3, max = 60)
    private String streetAddress;

    private String detailAddress;

    @NotBlank
    @Length(max = 30)
    private String zipcode;

}
