package com.clinic.notification.domain;
import jakarta.persistence.*;import java.time.Instant;import java.util.UUID;
@Entity @Table(name="oauth_credentials") public class OAuthCredential{
 @Id private UUID userId;@Column(nullable=false)private String email;@Column(nullable=false,columnDefinition="text")private String accessToken;@Column(columnDefinition="text")private String refreshToken;@Column(nullable=false)private Instant expiresAt;@Column(nullable=false)private Instant updatedAt;
 protected OAuthCredential(){}public OAuthCredential(UUID id,String email,String access,String refresh,Instant expires){userId=id;this.email=email;accessToken=access;refreshToken=refresh;expiresAt=expires;updatedAt=Instant.now();}
 public UUID getUserId(){return userId;}public String getAccessToken(){return accessToken;}public String getRefreshToken(){return refreshToken;}public Instant getExpiresAt(){return expiresAt;}public void refresh(String access,Instant expires){accessToken=access;expiresAt=expires;updatedAt=Instant.now();}
}
