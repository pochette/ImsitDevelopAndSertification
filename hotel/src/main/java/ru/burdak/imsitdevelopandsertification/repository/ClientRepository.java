package ru.burdak.imsitdevelopandsertification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.burdak.imsitdevelopandsertification.model.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
