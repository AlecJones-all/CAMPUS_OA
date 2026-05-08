将下载的改名为小写，放在d盘
"D:\campus_oa\backend\src\main\resources\application.yml"使用记事本打开修改成本地mysql自己的密码
修改bat后缀文件的mysql密码


先初始化数据库
mysql -u root -p
输入密码
CREATE DATABASE IF NOT EXISTS campus_oa DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;


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


