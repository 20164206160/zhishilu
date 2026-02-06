package com.zhishilu.repository;

import com.zhishilu.entity.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户Repository
 */
@Repository
public interface UserRepository extends ElasticsearchRepository<User, String> {
    
    /**
     * 根据用户名查询
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查询
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
}
