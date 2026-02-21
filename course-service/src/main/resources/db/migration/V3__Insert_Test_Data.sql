-- V3__Insert_Test_Data.sql
-- 课程与小节测试数据

-- 课程数据
INSERT INTO courses (id, title, description, cover_image, price, category, instructor_id, level, section_count, status, valid_days, created_at, updated_at) VALUES
(1, 'Spring Boot 从零到精通', '全面讲解 Spring Boot 核心概念，包括 IoC、AOP、数据访问、安全等模块，适合有 Java 基础的开发者。', 'https://picsum.photos/seed/spring/400/225', 99.00, '后端开发', 1, 'BEGINNER', 0, 'ACTIVE', 365, NOW(), NOW()),
(2, 'Spring Cloud 微服务实战', '从单体应用到微服务架构，涵盖 Eureka、Gateway、OpenFeign、RabbitMQ 等核心组件。', 'https://picsum.photos/seed/cloud/400/225', 199.00, '后端开发', 1, 'INTERMEDIATE', 0, 'ACTIVE', 365, NOW(), NOW()),
(3, 'MySQL 性能优化', '深入讲解索引原理、查询优化、慢查询分析，让你的数据库性能提升 10 倍。', 'https://picsum.photos/seed/mysql/400/225', 149.00, '数据库', 1, 'INTERMEDIATE', 0, 'ACTIVE', 180, NOW(), NOW()),
(4, 'React 18 完整教程', '从 JSX 到 Hooks，从组件设计到状态管理，带你系统掌握现代 React 开发。', 'https://picsum.photos/seed/react/400/225', 129.00, '前端开发', 1, 'BEGINNER', 0, 'ACTIVE', 365, NOW(), NOW()),
(5, 'Docker + Kubernetes 实战', '容器化部署从入门到生产环境，包含完整的 CI/CD 流水线搭建。', 'https://picsum.photos/seed/docker/400/225', 179.00, 'DevOps', 1, 'ADVANCED', 0, 'ACTIVE', 365, NOW(), NOW()),
(6, 'Git 工作流与团队协作', '掌握 Git 分支策略、代码审查流程、冲突解决，适合团队开发场景。', 'https://picsum.photos/seed/git/400/225', 0.00, '开发工具', 1, 'BEGINNER', 0, 'ACTIVE', NULL, NOW(), NOW());

-- 小节数据
INSERT INTO course_sections (id, course_id, title, duration, sort_order, is_free, created_at) VALUES
-- Spring Boot 课程小节
(101, 1, '环境搭建与第一个 Spring Boot 项目', 600, 1, 1, NOW()),
(102, 1, 'Spring IoC 容器与依赖注入', 900, 2, 0, NOW()),
(103, 1, 'Spring AOP 面向切面编程', 840, 3, 0, NOW()),
(104, 1, 'Spring Data JPA 数据访问', 1020, 4, 0, NOW()),
(105, 1, 'Spring Security 安全认证', 960, 5, 0, NOW()),

-- Spring Cloud 课程小节
(201, 2, '微服务架构概述', 720, 1, 1, NOW()),
(202, 2, 'Eureka 服务注册与发现', 840, 2, 0, NOW()),
(203, 2, 'Spring Cloud Gateway 网关', 900, 3, 0, NOW()),
(204, 2, 'OpenFeign 服务间调用', 780, 4, 0, NOW()),
(205, 2, 'RabbitMQ 消息队列', 960, 5, 0, NOW()),

-- Git 免费课程小节
(601, 6, 'Git 基础命令', 480, 1, 1, NOW()),
(602, 6, '分支管理策略', 600, 2, 1, NOW()),
(603, 6, 'Pull Request 与代码审查', 540, 3, 1, NOW());

-- 更新小节数量
UPDATE courses SET section_count = 5 WHERE id = 1;
UPDATE courses SET section_count = 5 WHERE id = 2;
UPDATE courses SET section_count = 3 WHERE id = 6;
