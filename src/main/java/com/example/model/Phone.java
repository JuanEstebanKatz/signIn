package com.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "phones")
public class Phone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long number;
    private Integer citycode;
    private String contrycode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getNumber() { return number; }
    public void setNumber(Long number) { this.number = number; }

    public Integer getCitycode() { return citycode; }
    public void setCitycode(Integer citycode) { this.citycode = citycode; }

    public String getContrycode() { return contrycode; }
    public void setContrycode(String contrycode) { this.contrycode = contrycode; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
