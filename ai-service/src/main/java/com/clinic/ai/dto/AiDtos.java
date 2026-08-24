package com.clinic.ai.dto;
import jakarta.validation.constraints.*;import java.util.List;
public final class AiDtos{
 private AiDtos(){}
 public record PreVisitRequest(@NotBlank @Size(max=5000)String symptoms){}
 public record PreVisitResponse(String urgencyLevel,String chiefComplaint,List<String> suggestedQuestions,String generatedBy,String safetyNotice){}
 public record PostVisitRequest(@NotBlank @Size(max=10000)String clinicalNotes,@Size(max=5000)String prescription){}
 public record PostVisitResponse(String visitSummary,List<String> medicationSchedule,List<String> followUpSteps,String generatedBy,String safetyNotice){}
}
