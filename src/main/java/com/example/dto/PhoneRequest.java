package com.example.dto;

public class PhoneRequest {

    private Long number;
    private Integer citycode;
    private String contrycode; // sí, con "n", porque así venía en el enunciado 🙃

    // Getters y setters
    public Long getNumber() { return number; }
    public void setNumber(Long number) { this.number = number; }

    public Integer getCitycode() { return citycode; }
    public void setCitycode(Integer citycode) { this.citycode = citycode; }

    public String getContrycode() { return contrycode; }
    public void setContrycode(String contrycode) { this.contrycode = contrycode; }
}
