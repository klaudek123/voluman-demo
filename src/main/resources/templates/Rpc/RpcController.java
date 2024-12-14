//package com.example.demo.Rpc;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/rpc")
//public class RpcController {
//    private final RpcService rpcService;
//
//    public RpcController(RpcService rpcService) {
//        this.rpcService = rpcService;
//    }
//
//    @GetMapping("/{methodName}")
//    public ResponseEntity<Object> handleRpcRequest(
//            @PathVariable String methodName,
//            @RequestParam Map<String, String> params) {
//        try {
//            Object result = rpcService.invoke(methodName, params);
//            return ResponseEntity.ok(result);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }
//}