package com.example.caplog.domain.users.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UsersPhotoConsentRequest(
        @JsonProperty("isApproved")
        Boolean isApproved
) {
}
