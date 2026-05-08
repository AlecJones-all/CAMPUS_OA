将下载的改名为小写，放在d盘
"D:\campus_oa\backend\src\main\resources\application.yml"使用记事本打开修改成本地mysql自己的密码
修改bat后缀文件的mysql密码


建数据库 
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS campus_oa DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"  
初始化
方式1：
mysql -u root -p campus_oa < D:\campus_oa\database\mysql\001_init_schema.sql  

方式2：
mysql -u root -p  
-- 选择数据库  
USE campus_oa;  
-- 导入脚本  
SOURCE D:/campus_oa/database/mysql/001_init_schema.sql;  

验证：  
mysql -u root -p  
-- 查看数据库  
SHOW DATABASES;  
-- 进入数据库  
USE campus_oa;  
-- 查看表  
SHOW TABLES;  
-- 检查测试账号  
SELECT * FROM sys_user;  
-- 检查角色表  
SELECT * FROM sys_role;  

下载前端依赖：
npm.cmd install

启动，bat即可

或者手动启动

打开第一个cmd
cd D:\campus_oa\backend
mvn spring-boot:run

打开第二个cmd
cd D:\campus_oa\frontend
npm.cmd run dev
