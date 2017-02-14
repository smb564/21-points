package com.smbsoft.health.web.rest;

import com.smbsoft.health.Application;

import com.smbsoft.health.domain.UserSettings;
import com.smbsoft.health.repository.UserSettingsRepository;
import com.smbsoft.health.repository.search.UserSettingsSearchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.smbsoft.health.domain.enumeration.WeightUnits;
/**
 * Test class for the UserSettingsResource REST controller.
 *
 * @see UserSettingsResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UserSettingsResourceIntTest {

    private static final Integer DEFAULT_WEEKLY_GOAL = 10;
    private static final Integer UPDATED_WEEKLY_GOAL = 11;

    private static final WeightUnits DEFAULT_WEIGHT_UNIT = WeightUnits.kg;
    private static final WeightUnits UPDATED_WEIGHT_UNIT = WeightUnits.lb;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private UserSettingsSearchRepository userSettingsSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private EntityManager em;

    private MockMvc restUserSettingsMockMvc;

    private UserSettings userSettings;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
            UserSettingsResource userSettingsResource = new UserSettingsResource(userSettingsRepository, userSettingsSearchRepository);
        this.restUserSettingsMockMvc = MockMvcBuilders.standaloneSetup(userSettingsResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserSettings createEntity(EntityManager em) {
        UserSettings userSettings = new UserSettings()
                .weeklyGoal(DEFAULT_WEEKLY_GOAL)
                .weightUnit(DEFAULT_WEIGHT_UNIT);
        return userSettings;
    }

    @Before
    public void initTest() {
        userSettingsSearchRepository.deleteAll();
        userSettings = createEntity(em);
    }

    @Test
    @Transactional
    public void createUserSettings() throws Exception {
        int databaseSizeBeforeCreate = userSettingsRepository.findAll().size();

        // Create the UserSettings

        restUserSettingsMockMvc.perform(post("/api/user-settings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userSettings)))
            .andExpect(status().isCreated());

        // Validate the UserSettings in the database
        List<UserSettings> userSettingsList = userSettingsRepository.findAll();
        assertThat(userSettingsList).hasSize(databaseSizeBeforeCreate + 1);
        UserSettings testUserSettings = userSettingsList.get(userSettingsList.size() - 1);
        assertThat(testUserSettings.getWeeklyGoal()).isEqualTo(DEFAULT_WEEKLY_GOAL);
        assertThat(testUserSettings.getWeightUnit()).isEqualTo(DEFAULT_WEIGHT_UNIT);

        // Validate the UserSettings in Elasticsearch
        UserSettings userSettingsEs = userSettingsSearchRepository.findOne(testUserSettings.getId());
        assertThat(userSettingsEs).isEqualToComparingFieldByField(testUserSettings);
    }

    @Test
    @Transactional
    public void createUserSettingsWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = userSettingsRepository.findAll().size();

        // Create the UserSettings with an existing ID
        UserSettings existingUserSettings = new UserSettings();
        existingUserSettings.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserSettingsMockMvc.perform(post("/api/user-settings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingUserSettings)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<UserSettings> userSettingsList = userSettingsRepository.findAll();
        assertThat(userSettingsList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllUserSettings() throws Exception {
        // Initialize the database
        userSettingsRepository.saveAndFlush(userSettings);

        // Get all the userSettingsList
        restUserSettingsMockMvc.perform(get("/api/user-settings?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(userSettings.getId().intValue())))
            .andExpect(jsonPath("$.[*].weeklyGoal").value(hasItem(DEFAULT_WEEKLY_GOAL)))
            .andExpect(jsonPath("$.[*].weightUnit").value(hasItem(DEFAULT_WEIGHT_UNIT.toString())));
    }

    @Test
    @Transactional
    public void getUserSettings() throws Exception {
        // Initialize the database
        userSettingsRepository.saveAndFlush(userSettings);

        // Get the userSettings
        restUserSettingsMockMvc.perform(get("/api/user-settings/{id}", userSettings.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(userSettings.getId().intValue()))
            .andExpect(jsonPath("$.weeklyGoal").value(DEFAULT_WEEKLY_GOAL))
            .andExpect(jsonPath("$.weightUnit").value(DEFAULT_WEIGHT_UNIT.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingUserSettings() throws Exception {
        // Get the userSettings
        restUserSettingsMockMvc.perform(get("/api/user-settings/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateUserSettings() throws Exception {
        // Initialize the database
        userSettingsRepository.saveAndFlush(userSettings);
        userSettingsSearchRepository.save(userSettings);
        int databaseSizeBeforeUpdate = userSettingsRepository.findAll().size();

        // Update the userSettings
        UserSettings updatedUserSettings = userSettingsRepository.findOne(userSettings.getId());
        updatedUserSettings
                .weeklyGoal(UPDATED_WEEKLY_GOAL)
                .weightUnit(UPDATED_WEIGHT_UNIT);

        restUserSettingsMockMvc.perform(put("/api/user-settings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedUserSettings)))
            .andExpect(status().isOk());

        // Validate the UserSettings in the database
        List<UserSettings> userSettingsList = userSettingsRepository.findAll();
        assertThat(userSettingsList).hasSize(databaseSizeBeforeUpdate);
        UserSettings testUserSettings = userSettingsList.get(userSettingsList.size() - 1);
        assertThat(testUserSettings.getWeeklyGoal()).isEqualTo(UPDATED_WEEKLY_GOAL);
        assertThat(testUserSettings.getWeightUnit()).isEqualTo(UPDATED_WEIGHT_UNIT);

        // Validate the UserSettings in Elasticsearch
        UserSettings userSettingsEs = userSettingsSearchRepository.findOne(testUserSettings.getId());
        assertThat(userSettingsEs).isEqualToComparingFieldByField(testUserSettings);
    }

    @Test
    @Transactional
    public void updateNonExistingUserSettings() throws Exception {
        int databaseSizeBeforeUpdate = userSettingsRepository.findAll().size();

        // Create the UserSettings

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restUserSettingsMockMvc.perform(put("/api/user-settings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userSettings)))
            .andExpect(status().isCreated());

        // Validate the UserSettings in the database
        List<UserSettings> userSettingsList = userSettingsRepository.findAll();
        assertThat(userSettingsList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteUserSettings() throws Exception {
        // Initialize the database
        userSettingsRepository.saveAndFlush(userSettings);
        userSettingsSearchRepository.save(userSettings);
        int databaseSizeBeforeDelete = userSettingsRepository.findAll().size();

        // Get the userSettings
        restUserSettingsMockMvc.perform(delete("/api/user-settings/{id}", userSettings.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean userSettingsExistsInEs = userSettingsSearchRepository.exists(userSettings.getId());
        assertThat(userSettingsExistsInEs).isFalse();

        // Validate the database is empty
        List<UserSettings> userSettingsList = userSettingsRepository.findAll();
        assertThat(userSettingsList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchUserSettings() throws Exception {
        // Initialize the database
        userSettingsRepository.saveAndFlush(userSettings);
        userSettingsSearchRepository.save(userSettings);

        // Search the userSettings
        restUserSettingsMockMvc.perform(get("/api/_search/user-settings?query=id:" + userSettings.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(userSettings.getId().intValue())))
            .andExpect(jsonPath("$.[*].weeklyGoal").value(hasItem(DEFAULT_WEEKLY_GOAL)))
            .andExpect(jsonPath("$.[*].weightUnit").value(hasItem(DEFAULT_WEIGHT_UNIT.toString())));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserSettings.class);
    }
}
