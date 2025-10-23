# Ejecución Automatizada del Sistema con Script `run.sh`

Este archivo explica cómo usar el script `run.sh` para compilar todos los microservicios y levantar el entorno completo del sistema distribuido con Docker Compose.

---

## ¿Qué hace el script?

El script `run.sh` realiza los siguientes pasos automáticamente:

1. **Compila cada microservicio con Maven** (`mvn clean install`).
2. **Verifica errores** después de cada compilación.
3. **Lanza Docker Compose** con la opción `--build` para asegurar que se construyan nuevas imágenes.

---

## ¿Cómo ejecutarlo?

1. Asegúrate de estar en la raíz del proyecto (donde se encuentra `run.sh`).
2. Da permisos de ejecución si es necesario:

```bash
chmod +x run.sh
```

3. Ejecuta el script:

```bash
./run.sh
```

---

## ¿Qué hacer si el script falla?

El script está diseñado para **detenerse automáticamente si una compilación falla**. En ese caso:

### 1. Revisa el servicio que falló

El mensaje de error indicará exactamente en qué microservicio ocurrió el problema. Por ejemplo:

```
Error compilando reviews-service. Abortando.
```

### 2. Soluciona el error

Abre la carpeta del servicio problemático y ejecuta manualmente el comando:

```bash
cd inventory-service
mvn clean install
```

Corrige cualquier error (dependencias, tests, errores de sintaxis, etc.)

### 3. Vuelve a ejecutar el script

Después de corregir el problema, vuelve a ejecutar el script completo desde la raíz:

```bash
./run.sh
```

---

## Resultado esperado

Si todo está correcto, al final verás los logs de los contenedores iniciando correctamente. Esto incluye:

- `category-service`, `products-service`, `qa-service`, `reviews-service`, `inventory-service` y `api-gateway` corriendo sin errores

Puedes verificar el estado de los contenedores con:

```bash
docker ps
```

---

## En caso de no funcionar

Realizar una instalación manual de cada servicio de Maven

```bash
cd {cada servicio}
mvn clean install
```

Para luego ejecutar docker compose con el siguiente comando en la carpeta raíz del proyecto:

```bash
docker-compose up --build
```
