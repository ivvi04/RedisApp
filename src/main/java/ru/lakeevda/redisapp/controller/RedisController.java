package ru.lakeevda.redisapp.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/redis")
public class RedisController {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public RedisController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @GetMapping("/keys")
    public ResponseEntity<Set<String>> getKeys() {
        Set<String> keys = redisTemplate.keys("*");
        return ResponseEntity.ok(keys);
    }
    
    @GetMapping("/key/{key}")
    public ResponseEntity<Object> getKey(@PathVariable String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(value);
    }
    
    @PostMapping("/key")
    public ResponseEntity<Void> setKey(@RequestParam String key, @RequestBody Object value) {
        redisTemplate.opsForValue().set(key, value);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @PutMapping("/key/{key}")
    public ResponseEntity<Object> updateKey(@PathVariable String key, @RequestBody Object value) {
        redisTemplate.opsForValue().set(key, value);
        return ResponseEntity.ok(value);
    }
    
    @DeleteMapping("/key/{key}")
    public ResponseEntity<Void> deleteKey(@PathVariable String key) {
        redisTemplate.delete(key);
        return ResponseEntity.noContent().build();
    }
}
