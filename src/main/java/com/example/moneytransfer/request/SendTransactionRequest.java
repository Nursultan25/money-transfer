package com.example.moneytransfer.request;

import com.example.moneytransfer.Enums.Currency;
import com.example.moneytransfer.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SendTransactionRequest {
    @NotBlank(message = "Имя получателя обязательно")
    private String usernameSender;

    @NotBlank(message = "Номер получателя обязателен")
    @Pattern(regexp="(^$|[0-9]{10})", message = "Номер должен быть формата '0999555444'")
    private String phoneNumberSender;

    @NotBlank(message = "Имя получателя обязательно")
    private String usernameReceiver;

    @NotBlank(message = "Номер получателя обязателен")
    @Pattern(regexp="(^$|[0-9]{10})", message = "Номер должен быть формата '0999555444'")
    private String phoneNumberReceiver;

    private String description;

    @NotNull(message = "Сумма обязательна")
    @Positive(message = "Недопустимая сумма")
    private BigDecimal amount;

    @NotNull(message = "Валюта обязательна")
    private String currency;
}
