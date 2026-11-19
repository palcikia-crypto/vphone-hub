# Termux-движки VPhone Hub

termux-scripts/setup.sh — установка окружения в Termux:

```bash
pkg install -y git
git clone <репозиторий>
bash termux-scripts/setup.sh
```

## Реалистичные ограничения по ОС

| ОС | Возможно ли на телефоне без root | Как |
|----|----------------------------------|-----|
| Debian/Ubuntu/Alpine | Да | proot-distro (используется приложением) |
| Android в Android | Нет (нужен root или AVD-хост) | только上层 платформы типа VMOS/QEMU с KVM (Android 13+, pKVM) |
| Windows x86 | Теоретически, очень медленно | QEMU в Termux (без KVM — эмуляция, ~50-500 раз медленнее) |
| Своя сборка Android | Нет | только кастомные ROM через fastboot |

Движок запуска ОС в приложении подключается через interface-адаптеры
(см. app/src/main/java/com/vphone/hub/engine/).
