package com.clinic.notification.domain;
import jakarta.persistence.*;import java.time.Instant;import java.util.UUID;
@Entity @Table(name="notification_jobs") public class NotificationJob{
 public enum Status{PENDING,RETRY,SENT,FAILED} public enum Channel{EMAIL,CALENDAR}
 @Id private UUID id;@Column(nullable=false,unique=true)private String dedupeKey;@Enumerated(EnumType.STRING)@Column(nullable=false)private Channel channel;
 @Column(nullable=false)private String action;private UUID userId;private String destination;private UUID appointmentId;@Column(nullable=false,columnDefinition="text")private String payload;
 @Enumerated(EnumType.STRING)@Column(nullable=false)private Status status;@Column(nullable=false)private int attempts;@Column(nullable=false)private Instant nextAttemptAt;private String lastError;@Column(nullable=false,updatable=false)private Instant createdAt;private Instant completedAt;
 protected NotificationJob(){} public NotificationJob(String key,Channel channel,String action,UUID userId,String destination,UUID appointmentId,String payload){id=UUID.randomUUID();dedupeKey=key;this.channel=channel;this.action=action;this.userId=userId;this.destination=destination;this.appointmentId=appointmentId;this.payload=payload;status=Status.PENDING;attempts=0;nextAttemptAt=Instant.now();createdAt=Instant.now();}
 public UUID getId(){return id;}public Channel getChannel(){return channel;}public String getAction(){return action;}public UUID getUserId(){return userId;}public String getDestination(){return destination;}public UUID getAppointmentId(){return appointmentId;}public String getPayload(){return payload;}public int getAttempts(){return attempts;}
 public void sent(){status=Status.SENT;completedAt=Instant.now();lastError=null;}public void failed(String error){attempts++;lastError=error==null?"Unknown failure":error.substring(0,Math.min(error.length(),900));if(attempts>=5){status=Status.FAILED;}else{status=Status.RETRY;nextAttemptAt=Instant.now().plusSeconds((long)Math.pow(2,attempts)*30);}}
}
