package com.clinic.ai.service;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;import java.net.http.*;import java.time.*;import java.util.concurrent.atomic.*;

@Component
public class GeminiClient{
 private final String apiKey,primaryModel,fallbackModel;private final ObjectMapper mapper;private final HttpClient http;
 private final AtomicInteger failures=new AtomicInteger();private final AtomicReference<Instant> openUntil=new AtomicReference<>(Instant.EPOCH);
 public GeminiClient(@Value("${gemini.api-key:}")String key,@Value("${gemini.primary-model}")String primary,@Value("${gemini.fallback-model}")String fallback,ObjectMapper mapper){
  this.apiKey=key;this.primaryModel=primary;this.fallbackModel=fallback;this.mapper=mapper;this.http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
 }
 public String primary(String prompt){return callWithRetries(primaryModel,prompt);}
 public String secondary(String prompt){return callWithRetries(fallbackModel,prompt);}
 private String callWithRetries(String model,String prompt){
  if(apiKey==null||apiKey.isBlank())throw new IllegalStateException("Gemini is not configured");
  if(Instant.now().isBefore(openUntil.get()))throw new IllegalStateException("Gemini circuit is temporarily open");
  RuntimeException last=null;
  for(int attempt=1;attempt<=2;attempt++)try{return call(model,prompt);}catch(RuntimeException e){last=e;if(attempt<2)try{Thread.sleep(250L*attempt);}catch(InterruptedException ie){Thread.currentThread().interrupt();throw new IllegalStateException("AI request interrupted",ie);}}
  if(failures.incrementAndGet()>=3){openUntil.set(Instant.now().plusSeconds(60));failures.set(0);}throw last;
 }
 private String call(String model,String prompt){
  try{
   ObjectNode body=mapper.createObjectNode();body.putArray("contents").addObject().putArray("parts").addObject().put("text",prompt);
   body.putObject("generationConfig").put("temperature",0.15).put("responseMimeType","application/json");
   HttpRequest request=HttpRequest.newBuilder(URI.create("https://generativelanguage.googleapis.com/v1beta/models/"+model+":generateContent?key="+apiKey))
    .timeout(Duration.ofSeconds(12)).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
   HttpResponse<String> response=http.send(request,HttpResponse.BodyHandlers.ofString());
   if(response.statusCode()/100!=2)throw new IllegalStateException("Gemini HTTP "+response.statusCode());
   JsonNode root=mapper.readTree(response.body());String text=root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
   if(text.isBlank())throw new IllegalStateException("Gemini returned an empty response");failures.set(0);return text;
  }catch(Exception e){if(e instanceof InterruptedException)Thread.currentThread().interrupt();throw new IllegalStateException("Gemini call failed",e);}
 }
}
