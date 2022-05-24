package com.julescarboni.speedcamerawarning.enums;

public enum CameraZoneType {
    NO_CAMERAS,
    MOBILE_ONLY,
    FIXED_ONLY,
    BOTH_CAMERAS,
    UNCERTAIN, // Use only for case when location is unknown or uncertain.
}