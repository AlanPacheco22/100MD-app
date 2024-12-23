package com.AlanPacheco.CienMD_app.Repository;

import com.AlanPacheco.CienMD_app.Entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByGameId(Long gameId);
}
