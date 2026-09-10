package com.lifelink.gateway.web;

/**
 * Starter-grade login payload. {@code role} is expected to be either
 * {@code "PROVIDER"} (hospitals/pharmacies/blood banks/restaurants posting
 * surplus) or {@code "NGO"} (NGOs/coordinators posting needs and approving
 * matches). See {@link AuthController} for why there is no real credential
 * check behind this.
 */
public record LoginRequest(String username, String password, String role) {
}
