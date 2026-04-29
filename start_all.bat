@echo off
echo === 初始化数据库 ===
mysql -u root -p123457896. campus_oa < D:\campus_oa\database\mysql\001_init_schema.sql

echo === 启动后端 ===
cd /d D:\campus_oa\backend
start mvn spring-boot:run

echo === 启动前端 ===
cd /d D:\campus_oa\frontend
start npm.cmd run dev

pause
