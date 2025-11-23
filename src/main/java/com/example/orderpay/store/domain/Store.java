package com.example.orderpay.store.domain;

import com.example.orderpay.member.User;
import jakarta.persistence.*;
import lombok.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stores")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    // 메뉴 목록
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>();
}
