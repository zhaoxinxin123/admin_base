# Phase 2 Verification

## Required Commands

- `mvn test`
- `mvn -DskipTests dependency:tree`
- `rg -n "com.baomidou.mybatisplus|mybatis-plus|QueryWrapper|BaseMapper|IPage|IService" pom.xml src/main/java src/main/resources`
- `rg -n "foreign key|tb_keys|tb_records" docs/database/admin-base-schema-v2.sql docs/database/admin-base-seed-v2.sql`

## Expected Results

- Tests pass using the `test` profile against `192.168.3.3` MySQL and Redis.
- MyBatis Plus dependencies and imports are absent from main code and resources.
- Mapper interfaces and XML files are absent.
- Spring Data JPA repositories back the system modules.
- V2 schema has no foreign keys.
- V2 schema and seed scripts exclude `tb_keys` and `tb_records`.
