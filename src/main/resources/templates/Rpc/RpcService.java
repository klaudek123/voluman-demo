//package com.example.demo.Rpc;
//
//import com.example.demo.Action.ActionService;
//import com.example.demo.Volunteer.Candidate.Candidate;
//import com.example.demo.Volunteer.Candidate.CandidateService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
//@Service
//public class RpcService {
//
//    private final Map<String, Function<Map<String, String>, Object>> methodRegistry = new HashMap<>();
//
//    public RpcService(CandidateService candidateService, ActionService actionService) {
//        setupCandidateMethods(candidateService);
//        //setup reszte
//
//    }
//    public Object invoke(String methodName, Map<String, String> params) {
//        if (!methodRegistry.containsKey(methodName)) {
//            throw new IllegalArgumentException("Method not found: " + methodName);
//        }
//        return methodRegistry.get(methodName).apply(params);
//    }
//
//    private void setupCandidateMethods(CandidateService candidateService) {
//        methodRegistry.put("getCandidates", params -> {
//            Long recruiterId = Long.valueOf(params.get("recruiterId"));
//            return candidateService.getCandidates(recruiterId);
//        });
//
//        methodRegistry.put("getCandidate", params -> {
//            Long recruiterId = Long.valueOf(params.get("recruiterId"));
//            Long idCandidate = Long.valueOf(params.get("idCandidate"));
//            return candidateService.getCandidate(idCandidate, recruiterId);
//        });
//
//        methodRegistry.put("acceptCandidate", params -> {
//            Long recruiterId = Long.valueOf(params.get("recruiterId"));
//            Long idCandidate = Long.valueOf(params.get("idCandidate"));
//            return candidateService.acceptCandidate(idCandidate, recruiterId);
//        });
//
//        methodRegistry.put("refuseCandidate", params -> {
//            Long recruiterId = Long.valueOf(params.get("recruiterId"));
//            Long idCandidate = Long.valueOf(params.get("idCandidate"));
//            return candidateService.refuseCandidate(idCandidate, recruiterId);
//        });
//
//        methodRegistry.put("addCandidate", params -> {
//            String candidateJson = params.get("candidate");
//            if (candidateJson == null || candidateJson.isEmpty()) {
//                throw new IllegalArgumentException("Missing 'candidate' parameter");
//            }
//
//            try {
//                ObjectMapper objectMapper = new ObjectMapper();
//                Candidate candidate = objectMapper.readValue(candidateJson, Candidate.class);
//                return candidateService.addCandidate(candidate);
//            } catch (Exception e) {
//                throw new IllegalArgumentException("Invalid candidate JSON: " + e.getMessage());
//            }
//        });
//    }
//}