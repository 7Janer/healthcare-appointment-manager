package com.clinic.ai.domain;
import jakarta.persistence.*;import java.time.Instant;import java.util.UUID;
@Entity @Table(name="ai_summary_cache",uniqueConstraints=@UniqueConstraint(columnNames={"prompt_type","input_hash"}))
public class AiSummaryCache{
 @Id private UUID id; @Column(name="prompt_type",nullable=false)private String promptType;@Column(name="input_hash",nullable=false,length=64)private String inputHash;
 @Column(nullable=false,columnDefinition="text")private String payload;@Column(nullable=false)private String source;@Column(nullable=false,updatable=false)private Instant createdAt;
 protected AiSummaryCache(){} public AiSummaryCache(String type,String hash,String payload,String source){id=UUID.randomUUID();promptType=type;inputHash=hash;this.payload=payload;this.source=source;createdAt=Instant.now();}
 public String getPayload(){return payload;}public String getSource(){return source;}
}
