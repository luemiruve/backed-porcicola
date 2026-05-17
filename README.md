# 🐷 Sistema Porcícola Inteligente

Sistema backend para la gestión integral de granjas porcinas, enfocado en digitalización de procesos productivos, trazabilidad y monitoreo de variables operativas.

---

## 🛠 Tech Stack & Badges
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Container-2496ED?style=for-the-badge&logo=docker&logoColor=white)

---

## 🎯 El Problema Operativo
En la industria porcícola tradicional (maternidad y desarrollo), el control de los ciclos reproductivos, el historial clínico, el inventario de alimento y los eventos críticos se suele gestionar de forma manual (hojas de cálculo o papel). Esto provoca:
* **Pérdida Crítica de Datos:** Falta de trazabilidad histórica por animal.
* **Errores Humanos de Captura:** Desajustes en las fechas proyectadas de parto.
* **Baja Eficiencia:** Tiempos muertos en la toma de decisiones veterinarias urgentes.

## 💡 La Solución e Impacto Proyectado
Este backend centraliza la lógica de negocio de la granja mediante una arquitectura robusta, permitiendo una gestión automatizada del ciclo de vida del ganado. 

* **Capacidad de Escala:** Diseñado para soportar el registro automatizado de **~200 eventos de producción al mes** por módulo de maternidad.
* **Optimización del Tiempo:** Reduce a cero el uso de hojas de cálculo manuales para el cálculo de alertas veterinarias mediante automatización en capa de datos.

---

## ⚙️ Arquitectura del Sistema
El sistema se rige bajo buenas prácticas de desarrollo corporativo:
* **Arquitectura en Capas:** Desacoplamiento estricto mediante `Controller` -> `Service` -> `Repository`.
* **Transferencia de Datos:** Implementación de patrones `DTO` + `Mappers` para proteger la integridad del modelo de dominio.
* **Lógica Enmascarada en BD:** Uso de persistencia relacional optimizada para mitigar la carga computacional en el servidor de aplicaciones.

## 🧬 Lógica Automatizada (Capa de Datos)
Para asegurar consistencia absoluta sin depender de cronjobs externos, la base de datos implementa **Triggers automáticos** para:
1. **Cálculo de Gestación:** Proyección exacta de fechas de parto al registrar una inseminación exitosa.
2. **Alertas de Sanidad:** Disparo automático de notificaciones ante eventos clínicos críticos.
3. **Control de Stock:** Ajuste en tiempo real del inventario físico de alimento/medicamentos basado en el consumo registrado.

---

## 🚀 Próximos Pasos Identificados (Product Roadmap)
Como parte de la evolución de este sistema hacia un ecosistema de **Agricultura de Precisión (AgTech)**, se tienen priorizados los siguientes sprints de desarrollo:

* [ ] **Integración IoT / Edge Computing:** Conexión de microcontroladores (ESP32) mediante protocolo MQTT para la lectura automatizada de sensores de temperatura ambiental y consumo de alimento en comederos dinámicos.
* [ ] **Módulo de Streaming en Tiempo Real:** Implementación de WebSockets en Spring Boot para actualizar tableros analíticos (*Dashboards*) sin recarga de página.
* [ ] **Capa de Movilidad Remota:** Desarrollo de la aplicación móvil nativa (iOS/Android) enfocada en el registro de campo en zonas de baja conectividad (Offline-First).
* [ ] **Analítica Predictiva:** Modelado de datos en Python/Pandas para la predicción de curvas de rendimiento cárnico mediante Machine Learning.
