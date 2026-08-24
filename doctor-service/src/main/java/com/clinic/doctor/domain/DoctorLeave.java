package com.clinic.doctor.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name="doctor_leaves",uniqueConstraints=@UniqueConstraint(columnNames={"doctor_id","leave_date"}))
public class DoctorLeave {
    @Id private UUID id;
    @Column(name="doctor_id",nullable=false) private UUID doctorId;
    @Column(name="leave_date",nullable=false) private LocalDate leaveDate;
    private String reason;
    @Column(nullable=false,updatable=false) private Instant createdAt;
    protected DoctorLeave(){}
    public DoctorLeave(UUID doctorId,LocalDate date,String reason){this.id=UUID.randomUUID();this.doctorId=doctorId;this.leaveDate=date;this.reason=reason;this.createdAt=Instant.now();}
    public UUID getId(){return id;} public UUID getDoctorId(){return doctorId;} public LocalDate getLeaveDate(){return leaveDate;} public String getReason(){return reason;}
}
