@echo off

echo === 启动后端 ===
start "校园OA后端" cmd /k "cd /d ""D:\campus_oa\backend"" && mvn spring-boot:run"

echo === 启动前端 ===
start "校园OA前端" cmd /k "cd /d ""D:\campus_oa\frontend"" && call npm.cmd run dev"

echo === 已发起启动，请查看后端和前端窗口日志 ===
pause