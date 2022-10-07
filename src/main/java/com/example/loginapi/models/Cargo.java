package com.example.loginapi.models;

import javax.persistence.*;

@Entity
@Table(name="cargos")
public class Cargo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private EnumCargo name;

    public Cargo(){

    }

    public Cargo(EnumCargo name){
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public EnumCargo getNome() {
        return name;
    }

    public void setNome(EnumCargo nome) {
        this.name = nome;
    }
}
