package com.yoy.monitorproject.monitor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface MonitorRepository extends JpaRepository<Monitor, Long> {

    Optional<Monitor> findByName(String name);
}
