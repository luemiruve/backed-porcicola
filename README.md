# 🐷 Sistema Porcícola Inteligente

Sistema backend para la gestión integral de granjas porcinas, enfocado en digitalización de procesos productivos, trazabilidad y monitoreo de variables operativas.

---

## 🎯 Problema

En muchas granjas porcinas, el control de:

- ciclos reproductivos
- inventario
- sanidad animal
- eventos críticos

se realiza de forma manual o con sistemas aislados, lo que genera:

- pérdida de información
- errores humanos
- baja eficiencia operativa

---

## 💡 Solución

Se desarrolló un sistema backend que centraliza la información de la granja y permite:

- gestión estructurada de animales
- control automatizado de ciclos reproductivos
- registro sanitario
- notificaciones automáticas de eventos críticos

Este sistema puede integrarse con dispositivos IoT para monitoreo en tiempo real.

---

## ⚙️ Arquitectura

- API REST con Spring Boot
- Arquitectura en capas (Controller, Service, Repository)
- DTO + Mapper para desacoplamiento
- Base de datos relacional con lógica en triggers

---

## 🧪 Tecnologías

- Java 21
- Spring Boot 3.5
- PostgreSQL
- JPA / Hibernate

---

## 🧬 Modelo de Datos

Incluye lógica automatizada mediante triggers para:

- cálculo de fechas de parto
- generación de alertas
- control de inventario

---

## 📊 Potencial de Integración IoT

Este sistema está diseñado para integrarse con:

- sensores de temperatura
- sensores de consumo de alimento
- dispositivos de monitoreo de salud animal

permitiendo evolucionar hacia una plataforma de agricultura inteligente.

---

## 🚀 Escenarios de uso

- Digitalización de granjas tradicionales
- Monitoreo de producción animal
- Base para sistemas de agricultura de precisión
- Integración con dashboards analíticos

---

## 📈 Futuro

- Integración con sensores IoT (ESP32 / Raspberry Pi)
- Dashboard en tiempo real
- Aplicación móvil (Android / iOS)
- Análisis predictivo con machine learning
