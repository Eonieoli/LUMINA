package com.lumina.backend.admin.service;

import java.util.Map;

public interface AdminService {

    Map<String, Object> getUser(Long userId, int pageNum);
}
