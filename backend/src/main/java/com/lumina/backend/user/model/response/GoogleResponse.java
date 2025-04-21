package com.lumina.backend.user.model.response;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class GoogleResponse implements OAuth2Response{

    private final Map<String, Object> attributes;

    @Override
    public String getProvider() {

        return "google";
    }

    @Override
    public String getProviderId() {

        return attributes.get("sub").toString();
    }

    @Override
    public String getName() {

        return attributes.get("name").toString();
    }

    @Override
    public String getProfileImage() {
        return attributes.get("picture").toString();
    }
}
