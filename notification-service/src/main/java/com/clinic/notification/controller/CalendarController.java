package com.clinic.notification.controller;
import com.clinic.notification.dto.NotificationDtos.*;import com.clinic.notification.service.GoogleCalendarService;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import org.springframework.web.servlet.view.RedirectView;import java.util.UUID;
@RestController @RequestMapping("/api/calendar") public class CalendarController{private final GoogleCalendarService service;public CalendarController(GoogleCalendarService s){service=s;}
 @GetMapping("/oauth/url")CalendarUrlResponse url(@RequestHeader("X-User-Id")UUID user,@RequestHeader("X-User-Email")String email){return new CalendarUrlResponse(service.authorizationUrl(user,email));}
 @GetMapping("/oauth/callback")RedirectView callback(@RequestParam UUID state,@RequestParam String code){return new RedirectView(service.callback(state,code));}
 @GetMapping("/status")CalendarStatusResponse status(@RequestHeader("X-User-Id")UUID user){return new CalendarStatusResponse(service.connected(user));}}
