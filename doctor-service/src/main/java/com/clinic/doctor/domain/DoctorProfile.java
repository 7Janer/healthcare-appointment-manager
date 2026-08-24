package com.clinic.doctor.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name="doctor_profiles")
public class DoctorProfile {
    @Id private UUID id;
    @Column(nullable=false,unique=true) private UUID userId;
    @Column(nullable=false) private String fullName;
    @Column(nullable=false) private String email;
    @Column(nullable=false) private String specialisation;
    private String qualifications;
    @Column(nullable=false) private LocalTime workingStart;
    @Column(nullable=false) private LocalTime workingEnd;
    @Column(nullable=false) private int slotDurationMinutes;
    @Column(nullable=false) private boolean active;
    @Column(nullable=false,updatable=false) private Instant createdAt;
    protected DoctorProfile() {}
    public DoctorProfile(UUID userId,String fullName,String email,String specialisation,String qualifications,LocalTime workingStart,LocalTime workingEnd,int slotDurationMinutes){
        this.id=UUID.randomUUID();this.userId=userId;this.fullName=fullName;this.email=email.toLowerCase();this.specialisation=specialisation;
        this.qualifications=qualifications;this.workingStart=workingStart;this.workingEnd=workingEnd;this.slotDurationMinutes=slotDurationMinutes;this.active=true;this.createdAt=Instant.now();
    }
    public void update(String fullName,String email,String specialisation,String qualifications,LocalTime start,LocalTime end,int duration,boolean active){
        this.fullName=fullName;this.email=email.toLowerCase();this.specialisation=specialisation;this.qualifications=qualifications;this.workingStart=start;this.workingEnd=end;this.slotDurationMinutes=duration;this.active=active;
    }
    public UUID getId(){return id;} public UUID getUserId(){return userId;} public String getFullName(){return fullName;} public String getEmail(){return email;}
    public String getSpecialisation(){return specialisation;} public String getQualifications(){return qualifications;} public LocalTime getWorkingStart(){return workingStart;}
    public LocalTime getWorkingEnd(){return workingEnd;} public int getSlotDurationMinutes(){return slotDurationMinutes;} public boolean isActive(){return active;}
}
