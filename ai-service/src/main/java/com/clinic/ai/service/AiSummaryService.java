package com.clinic.ai.service;

import com.clinic.ai.domain.AiSummaryCache;import com.clinic.ai.dto.AiDtos.*;import com.clinic.ai.repository.AiSummaryCacheRepository;import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;import org.springframework.stereotype.Service;import java.nio.charset.StandardCharsets;import java.security.MessageDigest;import java.util.*;

@Service
public class AiSummaryService{
 private static final String SAFETY="AI-generated support text; it is not a diagnosis and must be reviewed by a clinician.";
 private final GeminiClient gemini;private final AiSummaryCacheRepository cache;private final ObjectMapper mapper;
 public AiSummaryService(GeminiClient gemini,AiSummaryCacheRepository cache,ObjectMapper mapper){this.gemini=gemini;this.cache=cache;this.mapper=mapper;}
 public PreVisitResponse preVisit(String symptoms){String key=hash(symptoms);String prompt=prePrompt(symptoms);
  Optional<PreVisitResponse> primary=preFrom(()->gemini.primary(prompt),"GEMINI_PRIMARY",key);if(primary.isPresent())return primary.get();
  Optional<PreVisitResponse> secondary=preFrom(()->gemini.secondary(prompt),"GEMINI_SECONDARY",key);if(secondary.isPresent())return secondary.get();
  Optional<PreVisitResponse> cached=cache.findByPromptTypeAndInputHash("PRE_VISIT",key).flatMap(c->readPre(c.getPayload(),"CACHE"));if(cached.isPresent())return cached.get();
  PreVisitResponse rules=rulePre(symptoms);save("PRE_VISIT",key,rules,"RULES");return rules;
 }
 public PostVisitResponse postVisit(String notes,String prescription){String input=notes+"\n---PRESCRIPTION---\n"+Objects.toString(prescription,"");String key=hash(input);String prompt=postPrompt(notes,prescription);
  Optional<PostVisitResponse> primary=postFrom(()->gemini.primary(prompt),"GEMINI_PRIMARY",key);if(primary.isPresent())return primary.get();
  Optional<PostVisitResponse> secondary=postFrom(()->gemini.secondary(prompt),"GEMINI_SECONDARY",key);if(secondary.isPresent())return secondary.get();
  Optional<PostVisitResponse> cached=cache.findByPromptTypeAndInputHash("POST_VISIT",key).flatMap(c->readPost(c.getPayload(),"CACHE"));if(cached.isPresent())return cached.get();
  PostVisitResponse rules=rulePost(notes,prescription);save("POST_VISIT",key,rules,"RULES");return rules;
 }
 private Optional<PreVisitResponse> preFrom(SupplierWithFailure call,String source,String key){try{PreVisitResponse raw=mapper.readValue(call.get(),PreVisitResponse.class);PreVisitResponse v=new PreVisitResponse(normalizeUrgency(raw.urgencyLevel()),required(raw.chiefComplaint()),three(raw.suggestedQuestions()),source,SAFETY);save("PRE_VISIT",key,v,source);return Optional.of(v);}catch(Exception ignored){return Optional.empty();}}
 private Optional<PostVisitResponse> postFrom(SupplierWithFailure call,String source,String key){try{PostVisitResponse raw=mapper.readValue(call.get(),PostVisitResponse.class);PostVisitResponse v=new PostVisitResponse(required(raw.visitSummary()),safeList(raw.medicationSchedule()),safeList(raw.followUpSteps()),source,SAFETY);save("POST_VISIT",key,v,source);return Optional.of(v);}catch(Exception ignored){return Optional.empty();}}
 private Optional<PreVisitResponse> readPre(String json,String source){try{PreVisitResponse r=mapper.readValue(json,PreVisitResponse.class);return Optional.of(new PreVisitResponse(r.urgencyLevel(),r.chiefComplaint(),r.suggestedQuestions(),source,SAFETY));}catch(Exception e){return Optional.empty();}}
 private Optional<PostVisitResponse> readPost(String json,String source){try{PostVisitResponse r=mapper.readValue(json,PostVisitResponse.class);return Optional.of(new PostVisitResponse(r.visitSummary(),r.medicationSchedule(),r.followUpSteps(),source,SAFETY));}catch(Exception e){return Optional.empty();}}
 private PreVisitResponse rulePre(String s){String lower=s.toLowerCase();String urgency=contains(lower,"chest pain","difficulty breathing","unconscious","severe bleeding","stroke","suicidal")?"HIGH":contains(lower,"high fever","persistent","severe pain","vomiting","infection")?"MEDIUM":"LOW";String complaint=s.strip().replaceAll("\\s+"," ");if(complaint.length()>220)complaint=complaint.substring(0,217)+"...";return new PreVisitResponse(urgency,complaint,List.of("When did the symptoms begin and how have they changed?","What makes the symptoms better or worse?","Could medicines, allergies, or medical history be related?"),"RULES",SAFETY);}
 private PostVisitResponse rulePost(String notes,String prescription){String summary=notes.strip().replaceAll("\\s+"," ");if(summary.length()>500)summary=summary.substring(0,497)+"...";List<String> meds=prescription==null||prescription.isBlank()?List.of("No medication schedule was recorded. Confirm with your clinician."):Arrays.stream(prescription.split("[\\n;]+")).map(String::trim).filter(x->!x.isBlank()).toList();return new PostVisitResponse(summary,meds,List.of("Follow the clinician's written instructions.","Seek medical help if symptoms worsen or new urgent symptoms occur.","Attend the scheduled follow-up, if advised."),"RULES",SAFETY);}
 private String prePrompt(String s){return "You are a clinical intake assistant, not a diagnosing clinician. Return only JSON with keys urgencyLevel (LOW, MEDIUM, or HIGH), chiefComplaint, suggestedQuestions (exactly 3 strings). Flag emergency red symptoms HIGH. Do not diagnose or invent facts. Symptoms: "+s;}
 private String postPrompt(String n,String p){return "Convert the clinical notes into plain, patient-friendly language. Return only JSON with keys visitSummary, medicationSchedule (string array), followUpSteps (string array). Preserve dosages exactly, never add medication or advice, and state when information is missing. Clinical notes: "+n+"\nPrescription: "+Objects.toString(p,"");}
 private void save(String type,String key,Object value,String source){try{cache.save(new AiSummaryCache(type,key,mapper.writeValueAsString(value),source));}catch(DataIntegrityViolationException ignored){}catch(Exception ignored){}}
 private String hash(String s){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.strip().toLowerCase().getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
 private boolean contains(String s,String...terms){return Arrays.stream(terms).anyMatch(s::contains);}private String normalizeUrgency(String s){if(s==null)return"MEDIUM";String u=s.toUpperCase();return Set.of("LOW","MEDIUM","HIGH").contains(u)?u:"MEDIUM";}
 private String required(String s){if(s==null||s.isBlank())throw new IllegalArgumentException("Missing required AI field");return s;}private List<String> three(List<String> l){if(l==null||l.size()!=3)throw new IllegalArgumentException("Expected three questions");return l;}private List<String> safeList(List<String> l){return l==null?List.of():l;}
 @FunctionalInterface private interface SupplierWithFailure{String get();}
}
