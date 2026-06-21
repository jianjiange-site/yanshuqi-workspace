package com.dating.user.service;

import com.dating.user.dto.UpdateProfileCommand;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.service.support.ProfileAgeResolver;
import com.dating.user.service.support.ProfileBirthdayParser;
import com.dating.user.service.support.ProfileCompletionCalculator;
import com.dating.user.service.support.ProfileFieldValidator;
import com.dating.user.service.support.ProfileJsonSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserProfileSwaggerFieldMappingTest {

    private final ProfileJsonSupport jsonSupport = new ProfileJsonSupport();
    private final ProfileCompletionCalculator calculator = new ProfileCompletionCalculator(jsonSupport);
    private final ProfileBirthdayParser birthdayParser = new ProfileBirthdayParser();

    @Test
    void ageShouldDeriveFromBirthdayFirst() {
        UserProfileEntity entity = new UserProfileEntity();
        entity.setBirthDate(LocalDate.now().minusYears(25));
        entity.setAge(30);
        assertTrue(new ProfileAgeResolver(birthdayParser).resolveDisplayAge(entity) <= 26);
    }

    @Test
    void locationShouldCountTowardCompletion() {
        UserProfileEntity entity = new UserProfileEntity();
        entity.setNickname("n");
        entity.setGender("MALE");
        entity.setBirthDate(LocalDate.of(1995, 1, 1));
        entity.setLocation("Shanghai");
        entity.setBio("bio");
        entity.setHeight(170);
        entity.setOccupation("Dev");
        entity.setEducation("BS");
        assertEquals(1, calculator.calculateFromEntity(entity).getProfileCompleted());
    }

    @Test
    void legacyUpdateCommandStillCalculates() {
        UpdateProfileCommand command = new UpdateProfileCommand();
        command.setNickname("Alice");
        command.setGender("FEMALE");
        command.setBirthDate(LocalDate.of(1995, 6, 15));
        command.setCountryCode("CN");
        command.setCityCode("SH");
        assertTrue(calculator.calculate(command).getProfileScore() >= 80);
    }
}
