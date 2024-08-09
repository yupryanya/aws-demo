package com.task10.models;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class SignUp {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}