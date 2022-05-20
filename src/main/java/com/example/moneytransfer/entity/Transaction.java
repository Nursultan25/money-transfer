package com.example.moneytransfer.entity;

import com.example.moneytransfer.Enums.Currency;
import com.example.moneytransfer.Enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
            @JoinColumn(name = "user_sender", referencedColumnName = "id")
    private User userSender;

    @ManyToOne
            @JoinColumn(name = "user_receiver", referencedColumnName = "id")
    private User userReceiver;

    private String receiverClientNumber;

    private String senderClientNumber;

    private BigDecimal amount;

    private String description;

    @JsonIgnore
    @Column(name = "code", unique = true)
    private String code;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private Status status;

    @JsonIgnore
    @CreationTimestamp
    private Date dateCreated;

    @JsonIgnore
    @UpdateTimestamp
    private Date dateUpdated;

    @JsonIgnore
    @Version
    private int version;
}
