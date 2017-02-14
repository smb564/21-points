package com.smbsoft.health.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

import com.smbsoft.health.domain.enumeration.WeightUnits;

/**
 * A UserSettings.
 */
@Entity
@Table(name = "user_settings")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "usersettings")
public class UserSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Min(value = 10)
    @Column(name = "weekly_goal")
    private Integer weeklyGoal;

    @Enumerated(EnumType.STRING)
    @Column(name = "weight_unit")
    private WeightUnits weightUnit;

    @OneToOne
    @JoinColumn(unique = true)
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getWeeklyGoal() {
        return weeklyGoal;
    }

    public UserSettings weeklyGoal(Integer weeklyGoal) {
        this.weeklyGoal = weeklyGoal;
        return this;
    }

    public void setWeeklyGoal(Integer weeklyGoal) {
        this.weeklyGoal = weeklyGoal;
    }

    public WeightUnits getWeightUnit() {
        return weightUnit;
    }

    public UserSettings weightUnit(WeightUnits weightUnit) {
        this.weightUnit = weightUnit;
        return this;
    }

    public void setWeightUnit(WeightUnits weightUnit) {
        this.weightUnit = weightUnit;
    }

    public User getUser() {
        return user;
    }

    public UserSettings user(User user) {
        this.user = user;
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserSettings userSettings = (UserSettings) o;
        if (userSettings.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, userSettings.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "UserSettings{" +
            "id=" + id +
            ", weeklyGoal='" + weeklyGoal + "'" +
            ", weightUnit='" + weightUnit + "'" +
            '}';
    }
}
