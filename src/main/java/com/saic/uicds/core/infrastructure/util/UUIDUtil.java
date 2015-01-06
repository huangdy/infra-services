package com.saic.uicds.core.infrastructure.util;

import java.util.UUID;

import com.saic.uicds.core.infrastructure.service.WorkProductService;

public class UUIDUtil {

    public static final String getID(String type) {
        return type + "-" + UUID.randomUUID().toString();
    }

   
}
