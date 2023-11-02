package com.my.stock.rdb.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.my.stock.base.entity.BaseTimeEntity;
import com.my.stock.constants.SnsType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@EqualsAndHashCode(callSuper = false)
@Builder
@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private SnsType snsType;

    private String email;

    private String nickName;

    private String password;

    @JsonManagedReference
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankAccount> bankAccount;

    @JsonManagedReference
    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private PersonalSetting personalSetting;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<DailyTotalInvestmentAmount> dailyTotalInvestmentAmounts;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoginHistory> loginHistories;

    private String loginId;

}
