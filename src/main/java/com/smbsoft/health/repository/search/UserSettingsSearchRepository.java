package com.smbsoft.health.repository.search;

import com.smbsoft.health.domain.UserSettings;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the UserSettings entity.
 */
public interface UserSettingsSearchRepository extends ElasticsearchRepository<UserSettings, Long> {
}
