package com.quickbite.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlatformStatsDTO {
    private long totalUsers;
    private long totalCustomers;
    private long totalOwners;
    private long totalAgents;
    private long activeUsers;
}
