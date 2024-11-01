package com.AlanPacheco.CienMD_app.Model;

import jakarta.persistence.*;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Preguntas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String texto;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL)
    private List<Respuestas> respuestas;


}
