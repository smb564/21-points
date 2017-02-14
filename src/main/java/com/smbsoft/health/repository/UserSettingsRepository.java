package com.smbsoft.health.repository;

import com.smbsoft.health.domain.UserSettings;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the UserSettings entity.
 */
@SuppressWarnings("unused")
public interface UserSettingsRepository extends JpaRepository<UserSettings,Long> {

}
