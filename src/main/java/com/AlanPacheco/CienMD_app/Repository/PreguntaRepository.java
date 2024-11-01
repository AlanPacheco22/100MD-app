package com.AlanPacheco.CienMD_app.Repository;

import com.AlanPacheco.CienMD_app.Model.Preguntas;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreguntaRepository extends JpaRepository<Preguntas, Long> {
}
