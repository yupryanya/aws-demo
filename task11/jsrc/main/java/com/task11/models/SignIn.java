package com.task11.models;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class SignIn {
    private String email;
    private String password;
}