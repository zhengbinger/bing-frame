package com.bing.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

// HikariCP相关导入
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

/**
 * 优化的数据库连接池配置类
 * 基于HikariCP连接池的高性能配置，提供连接池监控和调优建议
 * 适用于高并发的审计日志系统
 * 
 * @author zhengbing
 * @date 2024-12-28
 */
@Configuration
@EnableTransactionManagement
@Slf4j
public class OptimizedDataSourceConfig {
    
    /**
     * 数据库连接配置 - 从application.yml读取
     */
    @Value("${spring.datasource.url}")
    private String databaseUrl;
    
    @Value("${spring.datasource.username}")
    private String databaseUsername;
    
    @Value("${spring.datasource.password}")
    private String databasePassword;
    
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;
    
    /**
     * 审计日志数据源配置
     */
    @Bean
    @Primary
    public DataSource primaryDataSource() {
        log.info("初始化优化的主数据源配置");
        
        // 创建HikariDataSource配置
        return new HikariDataSource(new HikariConfig(driverClassName, databaseUrl, databaseUsername, databasePassword));
    }
    
    /**
     * 审计日志专用数据源（独立连接池）
     */
    @Bean(name = "auditDataSource")
    public DataSource auditDataSource() {
        log.info("初始化审计日志专用数据源配置");
        return new HikariDataSource(new AuditHikariConfig(driverClassName, databaseUrl, databaseUsername, databasePassword));
    }
    
    /**
     * 主数据源Hikari配置
     */
    static class HikariConfig extends com.zaxxer.hikari.HikariConfig {
        public HikariConfig(String driverClassName, String jdbcUrl, String username, String password) {
            // 基本连接配置
            setDriverClassName(driverClassName);
            
            // 数据库配置
            setJdbcUrl(jdbcUrl);
            setUsername(username);
            setPassword(password);
            
            // 连接池大小配置 - 根据CPU核心数和负载调整
            // 核心连接数 = CPU核心数 * 5
            int cpuCores = Runtime.getRuntime().availableProcessors();
            int coreSize = Math.max(5, cpuCores * 5);
            int maximumPoolSize = Math.max(coreSize * 2, 20);
            
            setMinimumIdle(coreSize);
            setMaximumPoolSize(maximumPoolSize);
            
            // 连接超时配置
            setConnectionTimeout(30000); // 30秒
            
            // 空闲连接超时时间
            setIdleTimeout(600000); // 10分钟
            
            // 连接最大生存时间
            setMaxLifetime(1800000); // 30分钟
            
            // 连接测试查询
            setConnectionTestQuery("SELECT 1");
            
            // 连接验证配置
            setConnectionInitSql("SELECT 1");
            
            // 设置连接泄露检测阈值
            setLeakDetectionThreshold(60000); // 60秒
            
            // 自动提交
            setAutoCommit(true);
            
            // 连接池名称
            setPoolName("PrimaryHikariPool");
            
            log.info("主数据源配置完成: coreSize={}, maxSize={}, cpuCores={}", 
                    coreSize, maximumPoolSize, cpuCores);
        }
    }
    
    /**
     * 审计日志专用Hikari配置
     */
    static class AuditHikariConfig extends com.zaxxer.hikari.HikariConfig {
        public AuditHikariConfig(String driverClassName, String jdbcUrl, String username, String password) {
            // 数据库配置
            setDriverClassName(driverClassName);
            
            // 数据库配置
            setJdbcUrl(jdbcUrl);
            setUsername(username);
            setPassword(password);
            
            // 审计日志连接池配置 - 专门优化写入性能
            setMinimumIdle(10);
            setMaximumPoolSize(30);
            
            // 更短的连接超时，适应审计日志的高频写入
            setConnectionTimeout(15000); // 15秒
            
            // 较短的超时时间，适合审计日志的短事务
            setIdleTimeout(300000); // 5分钟
            
            setMaxLifetime(900000); // 15分钟
            
            // 专用的连接测试查询
            setConnectionTestQuery("SELECT 1");
            
            // 连接验证配置
            setConnectionInitSql("SELECT 1");
            
            // 连接泄露检测
            setLeakDetectionThreshold(30000); // 30秒
            
            // 自动提交，审计日志采用自动提交模式
            setAutoCommit(true);
            
            // 连接池名称
            setPoolName("AuditHikariPool");
            
            log.info("审计日志数据源配置完成");
        }
    }
    
    /**
     * 数据源监控指标配置
     */
    @Bean
    public DataSourceMonitor dataSourceMonitor() {
        return new DataSourceMonitor();
    }
    
    /**
     * 数据源监控类
     */
    public static class DataSourceMonitor {
        
        /**
         * 获取连接池统计信息
         */
        public PoolStatistics getPoolStatistics(DataSource dataSource) {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDS = (HikariDataSource) dataSource;
                HikariPoolMXBean poolBean = hikariDS.getHikariPoolMXBean();
                
                return new PoolStatistics(
                    poolBean.getActiveConnections(),
                    poolBean.getIdleConnections(),
                    poolBean.getTotalConnections(),
                    poolBean.getThreadsAwaitingConnection(),
                    hikariDS.getMaximumPoolSize(),
                    hikariDS.getMinimumIdle(),
                    hikariDS.getConnectionTimeout(),
                    hikariDS.getIdleTimeout(),
                    hikariDS.getMaxLifetime(),
                    calculateConnectionUsageRate(hikariDS),
                    calculatePoolHealthScore(hikariDS)
                );
            }
            return null;
        }
        
        /**
         * 计算连接使用率
         */
        private double calculateConnectionUsageRate(HikariDataSource dataSource) {
            HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
            int active = poolBean.getActiveConnections();
            int total = poolBean.getTotalConnections();
            return total > 0 ? (double) active / total : 0.0;
        }
        
        /**
         * 计算连接池健康分数
         */
        private int calculatePoolHealthScore(HikariDataSource dataSource) {
            double usageRate = calculateConnectionUsageRate(dataSource);
            HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
            int waitingThreads = poolBean.getThreadsAwaitingConnection();
            
            int score = 100;
            
            // 连接使用率超过80%扣20分
            if (usageRate > 0.8) {
                score -= 20;
            }
            
            // 有等待线程扣分
            if (waitingThreads > 0) {
                score -= Math.min(30, waitingThreads * 5);
            }
            
            // 空闲连接过少扣分
            if (poolBean.getIdleConnections() < dataSource.getMinimumIdle()) {
                score -= 15;
            }
            
            return Math.max(0, score);
        }
        
        /**
         * 连接池统计信息
         */
        public static class PoolStatistics {
            private final int activeConnections;
            private final int idleConnections;
            private final int totalConnections;
            private final int threadsAwaitingConnection;
            private final int maximumPoolSize;
            private final int minimumIdle;
            private final long connectionTimeout;
            private final long idleTimeout;
            private final long maxLifetime;
            private final double connectionUsageRate;
            private final int healthScore;
            
            public PoolStatistics(int activeConnections, int idleConnections, int totalConnections,
                                int threadsAwaitingConnection, int maximumPoolSize, int minimumIdle,
                                long connectionTimeout, long idleTimeout, long maxLifetime,
                                double connectionUsageRate, int healthScore) {
                this.activeConnections = activeConnections;
                this.idleConnections = idleConnections;
                this.totalConnections = totalConnections;
                this.threadsAwaitingConnection = threadsAwaitingConnection;
                this.maximumPoolSize = maximumPoolSize;
                this.minimumIdle = minimumIdle;
                this.connectionTimeout = connectionTimeout;
                this.idleTimeout = idleTimeout;
                this.maxLifetime = maxLifetime;
                this.connectionUsageRate = connectionUsageRate;
                this.healthScore = healthScore;
            }
            
            // Getters
            public int getActiveConnections() { return activeConnections; }
            public int getIdleConnections() { return idleConnections; }
            public int getTotalConnections() { return totalConnections; }
            public int getThreadsAwaitingConnection() { return threadsAwaitingConnection; }
            public int getMaximumPoolSize() { return maximumPoolSize; }
            public int getMinimumIdle() { return minimumIdle; }
            public long getConnectionTimeout() { return connectionTimeout; }
            public long getIdleTimeout() { return idleTimeout; }
            public long getMaxLifetime() { return maxLifetime; }
            public double getConnectionUsageRate() { return connectionUsageRate; }
            public int getHealthScore() { return healthScore; }
            
            /**
             * 获取优化建议
             */
            public String getOptimizationSuggestions() {
                StringBuilder suggestions = new StringBuilder();
                
                if (connectionUsageRate > 0.8) {
                    suggestions.append("连接使用率过高(").append(String.format("%.1f%%", connectionUsageRate * 100))
                             .append(")，建议增加maximumPoolSize或优化慢查询。");
                }
                
                if (threadsAwaitingConnection > 0) {
                    suggestions.append("存在").append(threadsAwaitingConnection).append("个线程等待连接，建议优化连接池配置或增加连接池大小。");
                }
                
                if (idleConnections < minimumIdle) {
                    suggestions.append("空闲连接数量不足，建议适当降低minimumIdle或增加maximumPoolSize。");
                }
                
                if (healthScore < 70) {
                    suggestions.append("连接池健康分数较低(").append(healthScore).append("分)，建议进行全面调优。");
                }
                
                return suggestions.toString();
            }
        }
    }
    
    /**
     * 数据源配置优化建议组件
     */
    @Bean
    public DataSourceOptimizer dataSourceOptimizer() {
        return new DataSourceOptimizer();
    }
    
    /**
     * 数据源优化建议器
     */
    public static class DataSourceOptimizer {
        
        /**
         * 根据系统负载生成优化建议
         */
        public OptimizationRecommendations getOptimizationRecommendations() {
            int cpuCores = Runtime.getRuntime().availableProcessors();
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            
            double memoryUsage = (double)(totalMemory - freeMemory) / maxMemory;
            
            return new OptimizationRecommendations(
                calculateOptimalPoolSize(cpuCores, memoryUsage),
                calculateOptimalTimeout(memoryUsage),
                calculateOptimalLifetime(cpuCores),
                generateOptimizationTips(memoryUsage, cpuCores)
            );
        }
        
        /**
         * 计算最优连接池大小
         */
        private int calculateOptimalPoolSize(int cpuCores, double memoryUsage) {
            // 基于CPU核心数和内存使用率计算最优连接池大小
            int baseSize = cpuCores * 5;
            
            // 内存使用率高时适当减少连接池大小
            if (memoryUsage > 0.8) {
                baseSize = (int)(baseSize * 0.8);
            }
            
            return Math.max(10, Math.min(baseSize, 50));
        }
        
        /**
         * 计算最优超时时间
         */
        private long calculateOptimalTimeout(double memoryUsage) {
            // 内存使用率高时增加超时时间
            if (memoryUsage > 0.7) {
                return 45000; // 45秒
            }
            return 30000; // 30秒
        }
        
        /**
         * 计算最优连接生存时间
         */
        private long calculateOptimalLifetime(int cpuCores) {
            // CPU核心数多时可以适当延长连接生存时间
            return cpuCores > 4 ? 2400000L : 1800000L; // 40分钟或30分钟
        }
        
        /**
         * 生成优化建议
         */
        private String[] generateOptimizationTips(double memoryUsage, int cpuCores) {
            if (memoryUsage > 0.8) {
                return new String[]{
                    "内存使用率较高(" + String.format("%.1f%%", memoryUsage * 100) + ")，建议优化JVM堆内存配置",
                    "考虑增加-Xmx参数或优化应用内存使用"
                };
            }
            
            if (cpuCores > 8) {
                return new String[]{
                    "CPU核心数较多，可以适当增加连接池大小以提高并发性能",
                    "建议监控连接池使用情况，根据实际负载调整配置"
                };
            }
            
            return new String[]{
                "当前配置适用于标准负载",
                "建议定期监控连接池性能指标"
            };
        }
    }
    
    /**
     * 优化建议结果
     */
    public static class OptimizationRecommendations {
        private final int optimalPoolSize;
        private final long optimalTimeout;
        private final long optimalLifetime;
        private final String[] tips;
        
        public OptimizationRecommendations(int optimalPoolSize, long optimalTimeout, 
                                         long optimalLifetime, String[] tips) {
            this.optimalPoolSize = optimalPoolSize;
            this.optimalTimeout = optimalTimeout;
            this.optimalLifetime = optimalLifetime;
            this.tips = tips;
        }
        
        public int getOptimalPoolSize() { return optimalPoolSize; }
        public long getOptimalTimeout() { return optimalTimeout; }
        public long getOptimalLifetime() { return optimalLifetime; }
        public String[] getTips() { return tips; }
    }
}