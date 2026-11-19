#!/data/data/com.termux/files/usr/bin/bash
set -e

pkg update -y && pkg upgrade -y
pkg install -y proot-distro python git openssl-tool

proot-distro install debian || echo "debian уже установлен"

echo "Готово. Запуск окружения: proot-distro login debian"
