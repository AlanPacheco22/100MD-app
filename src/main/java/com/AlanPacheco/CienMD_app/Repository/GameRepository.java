package com.AlanPacheco.CienMD_app.Repository;

import com.AlanPacheco.CienMD_app.Entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
