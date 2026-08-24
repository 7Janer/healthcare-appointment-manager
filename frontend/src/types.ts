export type Role='PATIENT'|'DOCTOR'|'ADMIN';
export interface AuthData{token:string;userId:string;fullName:string;email:string;role:Role}
export interface Doctor{id:string;userId:string;fullName:string;email:string;specialisation:string;qualifications?:string;workingStart:string;workingEnd:string;slotDurationMinutes:number;active:boolean}
export interface Appointment{id:string;patientId:string;patientName:string;patientEmail:string;doctorId:string;doctorUserId:string;doctorName:string;doctorEmail:string;startAt:string;durationMinutes:number;status:'CONFIRMED'|'CANCELLED'|'COMPLETED';symptoms:string;preVisitSummary?:string;urgencyLevel?:string;clinicalNotes?:string;prescriptionText?:string;postVisitSummary?:string;cancellationReason?:string}
export interface Slot{startAt:string;available:boolean}
