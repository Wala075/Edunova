package edunova.connexion.models;

import java.time.LocalDateTime;

/**
 * Modèle pour les données d'analyse de risque
 */
public class RiskData {
    private int id;
    private int userId;
    private String ipAddress;
    private String country;
    private String device;
    private LocalDateTime loginTime;
    private int failedAttempts;
    private double typingSpeed;
    private int riskScore;
    private String riskLevel;
    private boolean blocked;
    private LocalDateTime createdAt;

    // Constructors
    public RiskData() {}

    public RiskData(int userId, String ipAddress, String country, String device,
                    LocalDateTime loginTime, int failedAttempts, double typingSpeed) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.country = country;
        this.device = device;
        this.loginTime = loginTime;
        this.failedAttempts = failedAttempts;
        this.typingSpeed = typingSpeed;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getIpAddress() { return ipAddress; }
    public String getCountry() { return country; }
    public String getDevice() { return device; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public int getFailedAttempts() { return failedAttempts; }
    public double getTypingSpeed() { return typingSpeed; }
    public int getRiskScore() { return riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public boolean isBlocked() { return blocked; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setCountry(String country) { this.country = country; }
    public void setDevice(String device) { this.device = device; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
    public void setTypingSpeed(double typingSpeed) { this.typingSpeed = typingSpeed; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "RiskData{" +
                "id=" + id +
                ", userId=" + userId +
                ", ipAddress='" + ipAddress + '\'' +
                ", country='" + country + '\'' +
                ", device='" + device + '\'' +
                ", riskScore=" + riskScore +
                ", riskLevel='" + riskLevel + '\'' +
                ", blocked=" + blocked +
                '}';
    }
}
