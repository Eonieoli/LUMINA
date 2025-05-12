package com.lumina.backend.user.model.response;

public interface OAuth2Response {

    String getProvider();

    String getProviderId();

    String getName();

    String getProfileImage();
}
