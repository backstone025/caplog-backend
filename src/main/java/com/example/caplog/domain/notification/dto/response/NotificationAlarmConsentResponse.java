package com.example.caplog.domain.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NotificationAlarmConsentResponse(
        @JsonProperty("isApproved")
        Boolean isApproved
) {
}
