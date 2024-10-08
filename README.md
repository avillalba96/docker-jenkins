# README.md

## Descripción

El entorno está compuesto por dos servicios principales:

- **Jenkins Master**: El servidor Jenkins principal que gestiona los pipelines y despliegues.
- **Jenkins Agent**: Un agente de Jenkins que se conecta al servidor Master y ejecuta las tareas asignadas, incluyendo la capacidad de ejecutar contenedores Docker.

Ambos servicios están configurados para utilizar Docker-in-Docker (DinD), lo que permite a Jenkins ejecutar tareas de Docker dentro de los contenedores, como construir imágenes o ejecutar contenedores adicionales.

### Servicios

1. **Jenkins Master** (`jenkins`):
   - Se ejecuta en el puerto `8090` para la interfaz web de Jenkins y el puerto `50000` para la comunicación entre agentes.
   - Almacena los datos persistentes de Jenkins en la carpeta `./jenkins`.
   - Se configura para ejecutar sin el asistente de configuración inicial.
   - Volúmenes montados:
     - `/var/jenkins_home`: almacena los datos de Jenkins.
     - `/var/run/docker.sock`: permite que Docker se ejecute dentro del contenedor.

2. **Jenkins Agent** (`jenkins-agent`):
   - Se conecta al Jenkins Master utilizando el protocolo de WebSocket.
   - Ejecuta tareas desde Jenkins, incluidas las que involucran contenedores Docker.
   - Volúmenes montados:
     - `/var/run/docker.sock`: permite que Docker se ejecute dentro del contenedor.
     - `/home/jenkins/agent`: almacena los datos del agente Jenkins.

## Requisitos

- **Docker**: Asegúrate de tener Docker instalado y funcionando en tu máquina anfitriona.
- **Red Docker Externa**: Esta configuración asume que ya tienes creada una red Docker externa llamada `npm-network`.

Puedes crear la red con el siguiente comando si no existe:

```bash
docker network create npm-network
```

## Variables de entorno

Algunas variables de entorno están definidas en el archivo `.env`:

```env
JENKINS_AGENT_NAME=jenkins-agent-xxxxxx
JENKINS_SECRET=xxxxxxxxxxxxxxxxxxxxxx
```

## Configuración y Ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/avillalba96/docker-jenkins.git
cd docker-jenkins
```

### 2. Crear archivo `.env`

Crea un archivo `.env` con las variables necesarias para tu entorno, como se describe en la sección anterior.

### 3. Construir y ejecutar los contenedores

Para construir las imágenes de Jenkins Master y Jenkins Agent, y ejecutarlos en segundo plano, usa el siguiente comando:

```bash
docker-compose up -d --build
```

### 4. Ver los logs

Para monitorear los logs en tiempo real y ver el estado de ambos servicios:

```bash
docker-compose logs -ft --tail=35
```

### 5. Acceder a Jenkins

Una vez que Jenkins esté en ejecución, puedes acceder a la interfaz web de Jenkins navegando a:

```bash
http://localhost:8090
```

## Notas

- **Persistencia de Datos**: Los datos de Jenkins se almacenan en el directorio `./jenkins`. Asegúrate de no eliminar esta carpeta si deseas conservar los datos de Jenkins a lo largo del tiempo.
- **Red Docker Externa**: Ambos servicios utilizan la red externa `npm-network`. Si prefieres utilizar una red interna, puedes modificar la configuración en `docker-compose.yml`.
- **Jenkins Plugins**: Los plugins se definen en el archivo `plugins.txt` y se instalan automáticamente cuando Jenkins se inicia por primera vez.

## Personalización

Puedes modificar los Dockerfiles y los archivos de configuración para adaptar esta configuración a tus necesidades específicas, como agregar más agentes o ajustar los volúmenes.

## Troubleshooting

Si experimentas problemas durante la ejecución de Jenkins o los agentes, asegúrate de que:

1. Docker esté instalado y ejecutándose correctamente en tu máquina.
2. La red externa `npm-network` esté disponible.
3. No haya conflictos de puertos con otros servicios que usen el puerto `8090` o `50000`.

