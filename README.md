# Jungle Attack Server 

Repo jungle-attack-server. El servidor es la autoridad: calcula movimiento, combate, tazas, bonos y victoria, y a cada cliente le manda solo lo que puede ver. La spec del cliente está en su propio doc. Cada paquete tiene su clase Main para pruebas unitarias.

## Estructura Java

Requiere JDK 25 (LTS) y Maven 3.9 o posterior. El proyecto está organizado en los paquetes `util`, `model`, `mapa`, `game`, `partida` y `red`. Las configuraciones que el spec no define quedan como marcadores por completar. Usa Maven y Gson; se compila con `mvn compile` y se ejecutan las pruebas JUnit 5 con `mvn test`. Las pruebas del model layer están en `src/test/java/model`.

## Patrón Observer y Utilidades (util)

Mismo paquete que en jungle-attack-client.

### IObserver (Interfaz)

Métodos:

- update(Object source, Observable ob): void — Notifica al observador cuando ocurre un cambio en el objeto observado.

### Observable (Clase Abstracta)

Atributos:

- observers: Vector — Colección de observadores suscritos.

Métodos:

- addObserver(IObserver ob): void
- removeObserver(IObserver ob): void
- notifyObservers(Object source): void — Invoca update(source, this) sobre cada observador registrado.

### Constantes

Clase no instanciable que concentra todos los valores constantes y configuraciones del juego.

Variables principales:

- Manadas: TAM\_MANADA\_JUGADOR (10), ENERGIA\_JUGADOR (20), ENERGIA\_MAX\_JUGADOR (20), DANIO\_JUGADOR (2), TAM\_MANADA\_BOT (4), ENERGIA\_BOT (10), DANIO\_BOT (1), CANTIDAD\_ESPECIES (7).
- Combate: COOLDOWN\_ATAQUE\_MS (1000), RADIO\_ENCUENTRO, BOTS\_POR\_BONO (2), BONO\_ENERGIA (1).
- Mapa: CANTIDAD\_ZONAS (12), CANTIDAD\_TAZAS (10), BONO\_DANIO\_TAZA (1), RADIO\_TAZA, RADIO\_BANDERA, VELOCIDAD\_ANIMAL (píxeles por segundo), TICK\_MS.
- Partida: LARGO\_CODIGO (4), CARACTERES\_CODIGO ("A-Z0-9").
- Red: PUERTO, CHARSET ("UTF-8").

### MainUtil

Prueba unitaria independiente para verificar que Observable registra, quita y notifica observadores.

## 1. Model Layer (model)

### Punto

Coordenada en el mapa.

Atributos:

- x: double
- y: double

Métodos:

- distancia(Punto otro): double
- moverHacia(Punto destino, double maxDistancia): Punto — Devuelve el punto al que se llega avanzando hacia el destino sin pasarse.

### Especie (Enum)

Las 7 especies nativas de Costa Rica.

Valores: JAGUAR, DANTA, PEREZOSO, MONO\_CONGO, TUCAN, COCODRILO, PIZOTE

Atributos:

- nombre: String
- rutaSvg: String

### EntidadJuego (Clase Abstracta, Hereda de Observable)

Todo lo que tiene posición en el mapa.

Atributos:

- id: int
- posicion: Punto
- idZona: int

Métodos:

- getId(): int
- getPos(): Punto
- estaEnRadio(EntidadJuego otra, double radio): boolean

### Animal (Clase Abstracta, Hereda de EntidadJuego)

Miembro de una manada, de jugador o bot.

Atributos:

- especie: Especie
- energia: int
- manada: Manada
- ultimoAtaque: long
- ruta: Ruta

Métodos:

- estaVivo(): boolean — Retorna verdadero si la energía es mayor a 0.
- recibirDanio(int valor): void — Resta energía y dispara notificaciones si muere.
- puedeAtacar(long ahora): boolean — Verdadero si pasaron COOLDOWN\_ATAQUE\_MS desde el último ataque.
- atacar(Animal objetivo, long ahora): boolean — Le quita al objetivo el daño de su manada.
- setRuta(Ruta ruta): void
- avanzar(double deltaSeg): void — Avanza VELOCIDAD\_ANIMAL × deltaSeg por la ruta y dispara notificaciones.
- addEnergia(int valor): void — Suma energía sin pasar de getEnergiaMax().
- getEnergiaMax(): int (abstracto)
- esBot(): boolean (abstracto)

### AnimalJugador (Hereda de Animal)

Animal que controla un jugador. Nunca ataca solo.

Atributos:

- objetivo: Animal

Métodos:

- setObjetivo(Animal objetivo): void
- limpiarObjetivo(): void
- getEnergiaMax(): int — Retorna ENERGIA\_MAX\_JUGADOR.
- esBot(): boolean — Retorna falso.

### AnimalBot (Hereda de Animal)

Animal controlado por el servidor. Ataca automáticamente a los animales de jugador en su radio.

Métodos:

- getEnergiaMax(): int — Retorna ENERGIA\_BOT.
- esBot(): boolean — Retorna verdadero.

### Manada (Clase Abstracta) — Patrón Factory Method: creador

Grupo de animales de una misma especie. El daño es de la manada y lo comparten todos.

Atributos:

- especie: Especie
- miembros: Vector
- danio: int

Métodos:

- getVivos(): Vector
- estaEliminada(): boolean
- addDanio(int valor): void
- crearMiembros(Zona zona, Random random): void — Crea y ubica los miembros dentro de su zona inicial.
- crearMiembro(int id, Punto pos): Animal (abstracto)

### ManadaJugador (Hereda de Manada) — Factory Method: creador concreto

Atributos:

- dueno: Jugador

Métodos:

- crearMiembro(int id, Punto pos): Animal — Crea un AnimalJugador.
- aplicarBonoEnergia(int valor): void — Suma energía a todos los vivos, máximo 20.

### ManadaBot (Hereda de Manada) — Factory Method: creador concreto

Atributos:

- idZonaInicial: int

Métodos:

- crearMiembro(int id, Punto pos): Animal — Crea un AnimalBot.

### TazaCafe (Hereda de EntidadJuego)

Atributos:

- consumida: boolean

Métodos:

- consumir(ManadaJugador manada): boolean — Solo funciona la primera vez; suma BONO\_DANIO\_TAZA a toda la manada.

### Bandera (Hereda de EntidadJuego)

Atributos:

- dueno: Jugador
- color: String

Métodos:

- esTocadaPor(Animal animal): boolean — Solo cuenta un animal del rival dentro de RADIO\_BANDERA.

### MainModel

Prueba unitaria independiente para verificar daño, muerte en 0, cooldown de 1 segundo, límite de energía y que la taza se consuma una sola vez.

## 2. Map Layer (mapa)

### Zona

Una de las 12 zonas del bosque.

Atributos:

- id: int
- limites: Rectangle2D
- vecinos: Vector

Métodos:

- contiene(Punto p): boolean
- puntoAleatorio(Random random): Punto

### Camino

Sendero caminable que une dos zonas.

Atributos:

- idZonaA: int
- idZonaB: int
- puntos: Vector
- largo: double

Métodos:

- conecta(int zonaA, int zonaB): boolean
- puntoAleatorio(Random random): Punto — Usado para poner las tazas.

### Mapa

Grafo de zonas y caminos.

Atributos:

- zonas: Vector
- caminos: Vector

Métodos:

- getZona(int id): Zona
- zonaEn(Punto p): Zona
- getVecinos(int idZona): Vector
- getCamino(int zonaA, int zonaB): Camino

### ConstructorMapa — Patrón Builder

Atributos:

- zonas: Vector
- caminos: Vector

Métodos:

- agregarZona(Zona zona): ConstructorMapa
- conectar(int zonaA, int zonaB, Vector puntos): ConstructorMapa
- construir(): Mapa — Valida que sean 12 zonas y que todas estén conectadas.

### IBuscadorRuta (Interfaz) — Patrón Strategy

Métodos:

- buscarRuta(Mapa mapa, int zonaOrigen, int zonaDestino): Vector — Lista de zonas por recorrer.

### BuscadorBFS (Implementa IBuscadorRuta)

Métodos:

- buscarRuta(Mapa mapa, int zonaOrigen, int zonaDestino): Vector — Ruta con menos zonas.

### BuscadorDijkstra (Implementa IBuscadorRuta)

Métodos:

- buscarRuta(Mapa mapa, int zonaOrigen, int zonaDestino): Vector — Ruta más corta usando el largo de cada camino.

### Ruta

Puntos que un animal va siguiendo hasta su destino.

Atributos:

- puntos: Vector
- indice: int

Métodos:

- crear(Mapa mapa, Vector zonas, Punto inicio, Punto destino): Ruta (estático)
- puntoActual(): Punto
- avanzar(): void
- terminada(): boolean

### MainMapa

Prueba unitaria independiente para verificar que el mapa se construye con 12 zonas y que BFS y Dijkstra encuentran la ruta entre dos zonas.

## 3. Game Layer (game)

### Jugador

Uno de los dos participantes de la partida.

Atributos:

- nickname: String
- conexion: ManejadorCliente
- especie: Especie
- manada: ManadaJugador
- bandera: Bandera
- zonaVista: int
- tazasConsumidas: int
- botsEliminados: int
- animalesPerdidos: int

Métodos:

- registrarBotEliminado(): boolean — Retorna verdadero cuando el contador llega a un múltiplo de BOTS\_POR\_BONO.
- registrarTaza(): void
- puedeVer(int idZona): boolean — Verdadero si tiene al menos un animal vivo en esa zona.
- getNickname(): String

### MundoJuego (Hereda de Observable, Implementa IObserver)

Administra el estado lógico del bosque durante la partida.

Atributos:

- mapa: Mapa
- manadasJugador: Vector
- manadasBot: Vector
- tazas: Vector
- banderas: Vector
- terminado: AtomicBoolean
- ganador: Jugador

Métodos:

- getAnimalesVivos(): Vector
- buscarAnimal(int id): Animal
- hayGanador(): boolean
- getGanador(): Jugador
- terminar(Jugador ganador): void
- update(Object source, Observable ob): void — Escucha a los animales; cuando uno muere actualiza contadores y notifica a la Partida.

### InicializadorMundo

Arma el mundo al inicio de la partida.

Atributos:

- random: Random — Se inyecta para poder probar con semilla fija.

Métodos:

- elegirZonasIniciales(Mapa mapa, int cantidad): Vector — 7 zonas distintas de las 12.
- colocarTazas(MundoJuego mundo): void — 10 tazas en puntos aleatorios de los caminos.
- colocarBanderas(MundoJuego mundo): void

### IReglaJuego (Interfaz) — Patrón Strategy

Métodos:

- aplicar(MundoJuego mundo, long ahora, double deltaSeg): void

### Reglas (Implementan IReglaJuego)

- ReglaMovimiento: Avanza cada animal por su ruta a velocidad constante.
- ReglaAtaqueBot: Cada bot dentro de RADIO\_ENCUENTRO ataca automáticamente al animal del jugador.
- ReglaAtaqueJugador: Cada AnimalJugador con objetivo lo ataca si está en rango y venció el cooldown; si el objetivo muere o sale del rango, se detiene.
- ReglaTazaCafe: La primera taza que toca un animal se consume.
- ReglaBonoEnergia: Cada 2 bots eliminados, +1 de energía a todos los vivos de la manada, máximo 20.
- ReglaVictoria: Si un animal toca la bandera rival, termina la partida.

### CicloJuego (Implementa Runnable)

Bucle del juego en el servidor. No usa Thread.sleep por animal.

Atributos:

- mundo: MundoJuego
- reglas: Vector
- reloj: Clock
- ultimoTick: long
- activo: AtomicBoolean

Métodos:

- run(): void — Cada TICK\_MS llama a tick.
- tick(long ahora): void — Calcula deltaSeg y aplica todas las reglas en orden.
- detener(): void

### ServicioVisibilidad

Hace cumplir la niebla de guerra desde el servidor.

Métodos:

- zonasVisibles(Jugador jugador): Vector — Zonas donde el jugador tiene animales vivos.
- crearSnapshot(MundoJuego mundo, Jugador jugador): EstadoVisible — Solo incluye lo que hay en la zona que está viendo.

### MainGame

Prueba unitaria independiente para validar el ejemplo del enunciado (un animal de 20 de energía muere en 5 segundos contra 4 bots), el bono cada 2 bots con tope 20 y la victoria al tocar la bandera.

## 4. Match Layer (partida)

### EstadoPartida (Enum)

Valores: ESPERANDO\_JUGADOR, ELIGIENDO\_ANIMALES, EN\_JUEGO, TERMINADA

### GeneradorCodigo

Atributos:

- random: Random

Métodos:

- generar(Set existentes): String — 4 caracteres alfanuméricos, único entre las partidas activas.
- normalizar(String codigo): String (estático) — Pasa a mayúsculas para comparar sin importar mayúsculas.

### SelectorEspecies

Atributos:

- disponibles: Set
- elecciones: Map

Métodos:

- elegir(Jugador jugador, Especie especie): boolean — Falso si el otro jugador ya la tomó.
- todosEligieron(): boolean
- especiesBot(): Vector — Las 5 especies que quedan como bots.

### Partida (Implementa IObserver)

Una partida activa identificada por su código.

Atributos:

- codigo: String
- estado: EstadoPartida
- jugadores: Vector — Máximo 2.
- selector: SelectorEspecies
- mundo: MundoJuego
- ciclo: CicloJuego
- inicio: long

Métodos:

- agregarJugador(Jugador jugador): void
- estaLlena(): boolean
- elegirEspecie(Jugador jugador, Especie especie): void
- iniciar(): void — Crea las 7 manadas (2 de jugador y 5 bots) y arranca el ciclo.
- enviarEstado(): void — Manda a cada jugador su snapshot visible.
- terminar(Jugador ganador): void — Envía GAME\_OVER con el resumen.
- manejarDesconexion(Jugador jugador): void — Regla: el otro jugador gana.
- update(Object source, Observable ob): void — Escucha al MundoJuego para enviar eventos y detectar el fin.

### AdministradorPartidas

Soporta N partidas a la vez.

Atributos:

- partidas: ConcurrentHashMap
- generador: GeneradorCodigo

Métodos:

- crearPartida(Jugador anfitrion): Partida
- unirse(String codigo, Jugador invitado): Partida — Error si el código no existe o la partida está llena.
- buscar(String codigo): Partida
- eliminar(String codigo): void — Se llama cuando la partida termina.
- getCantidadActivas(): int

### MainPartida

Prueba unitaria independiente para validar la generación del código (largo, caracteres, unicidad), unirse con código inválido o partida llena, y que no se repita la especie.

## 5. Network Layer (red)

Las clases de protocolo (TipoMensaje, Mensaje, CodificadorJson, FabricaMensajes y DTOs) están iguales en el cliente.

### TipoMensaje (Enum)

Valores: CREATE\_MATCH, JOIN\_MATCH, SELECT\_ANIMAL, MOVE\_ANIMALS, ATTACK, VIEW\_ZONE, MATCH\_CREATED, MATCH\_JOINED, ERROR, ANIMAL\_SELECTION, GAME\_STARTED, STATE\_UPDATE, GAME\_OVER

### Mensaje

Sobre de cada mensaje JSON: una línea por mensaje.

Atributos:

- tipo: TipoMensaje
- payload: JsonObject

Métodos:

- getTipo(): TipoMensaje
- getPayload(Class tipo): Object — Convierte el payload al DTO indicado.

### CodificadorJson — Patrón Singleton

Atributos:

- instancia: CodificadorJson (estático)
- gson: Gson

Métodos:

- getInstancia(): CodificadorJson (estático)
- codificar(Mensaje mensaje): String
- decodificar(String linea): Mensaje

### FabricaMensajes — Patrón Factory

Clase no instanciable.

Métodos:

- crear(TipoMensaje tipo, Object payload): Mensaje (estático)
- error(String razon): Mensaje (estático)

### DTOs (objetos de transferencia de datos)

- DatosCrearPartida: nickname: String
- DatosUnirse: nickname: String, codigo: String
- DatosElegirAnimal: especie: Especie
- DatosMover: idsAnimales: Vector, destinoX: double, destinoY: double
- DatosAtacar: idsAnimales: Vector, idObjetivo: int
- DatosVerZona: idZona: int
- DatosPartidaCreada: codigo: String
- DatosSeleccion: disponibles: Vector, eleccionRival: Especie
- DatosInicio: mapa: VistaMapa, zonaPropia: int, manada: Vector
- EstadoVisible: idZona: int, animales: Vector, tazas: Vector, bandera: Punto, eventos: Vector, danio: int, tazasConsumidas: int, botsEliminados: int
- ResumenPartida: ganador: String, botsEliminados: Map, animalesPerdidos: Map, tazas: Map, duracionSeg: long
- VistaAnimal: id: int, especie: Especie, esBot: boolean, esMio: boolean, x: double, y: double, energia: int
- VistaMapa: zonas: Vector, caminos: Vector

### IComando (Interfaz) — Patrón Command

Métodos:

- ejecutar(ManejadorCliente cliente, Mensaje mensaje): void

### Comandos (Implementan IComando)

- ComandoCrearPartida: Crea la partida y responde MATCH\_CREATED con el código.
- ComandoUnirse: Une al jugador o responde ERROR (código inválido o partida llena).
- ComandoElegirAnimal: Elige la especie o responde ERROR si ya está tomada.
- ComandoMover: Calcula la ruta de los animales seleccionados.
- ComandoAtacar: Asigna el objetivo a los animales seleccionados.
- ComandoVerZona: Cambia la zona que el jugador está viendo si tiene animales ahí.

### EnrutadorMensajes — Command: invocador

Atributos:

- comandos: Map — Un IComando por cada TipoMensaje.

Métodos:

- registrar(TipoMensaje tipo, IComando comando): void
- enrutar(ManejadorCliente cliente, Mensaje mensaje): void

### ManejadorCliente (Implementa Runnable)

Uno por cada cliente conectado, en su propio hilo.

Atributos:

- socket: Socket
- lector: BufferedReader
- escritor: PrintWriter
- enrutador: EnrutadorMensajes
- jugador: Jugador

Métodos:

- run(): void — Lee una línea JSON por mensaje; cierra todo en finally.
- enviar(Mensaje mensaje): void
- cerrar(): void — Avisa a la Partida de la desconexión.

### ServidorJuego

Atributos:

- puerto: int
- serverSocket: ServerSocket
- hilos: ExecutorService
- administrador: AdministradorPartidas
- enrutador: EnrutadorMensajes
- activo: boolean

Métodos:

- iniciar(): void — Acepta conexiones y crea un ManejadorCliente por cada una.
- detener(): void

### ServerMain

Métodos:

- main(String\[\] args): void (estático)

### MainRed

Prueba unitaria independiente para verificar la ida y vuelta a JSON de cada tipo de mensaje y que el enrutador mande cada tipo a su comando.

## Resumen de patrones, herencia y polimorfismo

| Patrón | Participantes | Para qué |
| --- | --- | --- |
| Observer | IObserver, Observable; sujetos: EntidadJuego, MundoJuego; observadores: MundoJuego, Partida | Avisar muertes, tazas y bandera sin acoplar las reglas a la partida |
| Command | IComando y comandos con EnrutadorMensajes | Una clase por tipo de mensaje en vez de un switch gigante |
| Strategy | IBuscadorRuta (BuscadorBFS, BuscadorDijkstra); IReglaJuego (las 6 reglas) | Cambiar el algoritmo de rutas o agregar reglas sin tocar CicloJuego |
| Factory Method | Manada, ManadaJugador, ManadaBot | Cada manada crea su tipo de animal |
| Factory | FabricaMensajes | Crear mensajes con el tipo y payload correctos |
| Singleton | CodificadorJson | Un solo Gson para todo el servidor |
| Builder | ConstructorMapa | Armar y validar las 12 zonas y sus caminos |

| Clase padre | Clases hijas | Métodos polimórficos |
| --- | --- | --- |
| Observable | EntidadJuego, MundoJuego | notifyObservers() |
| EntidadJuego | Animal, TazaCafe, Bandera | estaEnRadio() |
| Animal | AnimalJugador, AnimalBot | getEnergiaMax(), esBot() |
| Manada | ManadaJugador, ManadaBot | crearMiembro() |

Estáticos: todas las constantes de Constantes, ServerMain.main(), getInstancia() de CodificadorJson, los métodos de FabricaMensajes, Ruta.crear() y GeneradorCodigo.normalizar(). No hay estado estático mutable.

---
## Diagrama Mermaid

```mermaid
classDiagram
    %% util - Utilidades
    namespace Util {
        class IObserver {
        +update(source: Object, ob: Observable): void
        }
        class Observable {
        #observers: Vector
        +addObserver(ob: IObserver): void
        +removeObserver(ob: IObserver): void
        +notifyObservers(source: Object): void
        }
        class Constantes {
        TAM_MANADA_JUGADOR = 10$
        ENERGIA_JUGADOR = 20$
        ENERGIA_MAX_JUGADOR = 20$
        DANIO_JUGADOR = 2$
        TAM_MANADA_BOT = 4$
        ENERGIA_BOT = 10$
        DANIO_BOT = 1$
        CANTIDAD_ESPECIES = 7$
        COOLDOWN_ATAQUE_MS = 1000$
        RADIO_ENCUENTRO$
        BOTS_POR_BONO = 2$
        BONO_ENERGIA = 1$
        CANTIDAD_ZONAS = 12$
        CANTIDAD_TAZAS = 10$
        BONO_DANIO_TAZA = 1$
        RADIO_TAZA$
        RADIO_BANDERA$
        VELOCIDAD_ANIMAL$
        TICK_MS$
        LARGO_CODIGO = 4$
        CARACTERES_CODIGO: String$
        PUERTO: int$
        CHARSET: String$
        }
        class MainUtil {
        +main(args: String[]): void$
        }
    }
    %% model - Modelo
    namespace Model {
        class Punto {
        -x: double
        -y: double
        +distancia(otro: Punto): double
        +moverHacia(destino: Punto, maxDistancia: double): Punto
        }
        class Especie {
        JAGUAR
        DANTA
        PEREZOSO
        MONO_CONGO
        TUCAN
        COCODRILO
        PIZOTE
        -nombre: String
        -rutaSvg: String
        }
        class EntidadJuego {
        #id: int
        #posicion: Punto
        #idZona: int
        +getId(): int
        +getPos(): Punto
        +estaEnRadio(otra: EntidadJuego, radio: double): boolean
        }
        class Animal {
        #especie: Especie
        #energia: int
        #manada: Manada
        #ultimoAtaque: long
        #ruta: Ruta
        +estaVivo(): boolean
        +recibirDanio(valor: int): void
        +puedeAtacar(ahora: long): boolean
        +atacar(objetivo: Animal, ahora: long): boolean
        +setRuta(ruta: Ruta): void
        +avanzar(deltaSeg: double): void
        +addEnergia(valor: int): void
        +getEnergiaMax(): int
        +esBot(): boolean
        }
        class AnimalJugador {
        -objetivo: Animal
        +setObjetivo(objetivo: Animal): void
        +limpiarObjetivo(): void
        +getEnergiaMax(): int
        +esBot(): boolean
        }
        class AnimalBot {
        +getEnergiaMax(): int
        +esBot(): boolean
        }
        class Manada {
        #especie: Especie
        #miembros: Vector
        #danio: int
        +getVivos(): Vector
        +estaEliminada(): boolean
        +addDanio(valor: int): void
        +crearMiembros(zona: Zona, random: Random): void
        +crearMiembro(id: int, pos: Punto): Animal
        }
        class ManadaJugador {
        -dueno: Jugador
        +crearMiembro(id: int, pos: Punto): Animal
        +aplicarBonoEnergia(valor: int): void
        }
        class ManadaBot {
        -idZonaInicial: int
        +crearMiembro(id: int, pos: Punto): Animal
        }
        class TazaCafe {
        -consumida: boolean
        +consumir(manada: ManadaJugador): boolean
        }
        class Bandera {
        -dueno: Jugador
        -color: String
        +esTocadaPor(animal: Animal): boolean
        }
        class MainModel {
        +main(args: String[]): void$
        }
    }
    %% mapa - Mapa y rutas
    namespace MapaLayer {
        class Zona {
        -id: int
        -limites: Rectangle2D
        -vecinos: Vector
        +contiene(p: Punto): boolean
        +puntoAleatorio(random: Random): Punto
        }
        class Camino {
        -idZonaA: int
        -idZonaB: int
        -puntos: Vector
        -largo: double
        +conecta(zonaA: int, zonaB: int): boolean
        +puntoAleatorio(random: Random): Punto
        }
        class Mapa {
        -zonas: Vector
        -caminos: Vector
        +getZona(id: int): Zona
        +zonaEn(p: Punto): Zona
        +getVecinos(idZona: int): Vector
        +getCamino(zonaA: int, zonaB: int): Camino
        }
        class ConstructorMapa {
        -zonas: Vector
        -caminos: Vector
        +agregarZona(zona: Zona): ConstructorMapa
        +conectar(zonaA: int, zonaB: int, puntos: Vector): ConstructorMapa
        +construir(): Mapa
        }
        class IBuscadorRuta {
        +buscarRuta(mapa: Mapa, zonaOrigen: int, zonaDestino: int): Vector
        }
        class BuscadorBFS {
        +buscarRuta(mapa: Mapa, zonaOrigen: int, zonaDestino: int): Vector
        }
        class BuscadorDijkstra {
        +buscarRuta(mapa: Mapa, zonaOrigen: int, zonaDestino: int): Vector
        }
        class Ruta {
        -puntos: Vector
        -indice: int
        +crear(mapa: Mapa, zonas: Vector, inicio: Punto, destino: Punto): Ruta$
        +puntoActual(): Punto
        +avanzar(): void
        +terminada(): boolean
        }
        class MainMapa {
        +main(args: String[]): void$
        }
    }
    %% game - Lógica del juego
    namespace Game {
        class Jugador {
        -nickname: String
        -conexion: ManejadorCliente
        -especie: Especie
        -manada: ManadaJugador
        -bandera: Bandera
        -zonaVista: int
        -tazasConsumidas: int
        -botsEliminados: int
        -animalesPerdidos: int
        +registrarBotEliminado(): boolean
        +registrarTaza(): void
        +puedeVer(idZona: int): boolean
        +getNickname(): String
        }
        class MundoJuego {
        -mapa: Mapa
        -manadasJugador: Vector
        -manadasBot: Vector
        -tazas: Vector
        -banderas: Vector
        -terminado: AtomicBoolean
        -ganador: Jugador
        +getAnimalesVivos(): Vector
        +buscarAnimal(id: int): Animal
        +hayGanador(): boolean
        +getGanador(): Jugador
        +terminar(ganador: Jugador): void
        +update(source: Object, ob: Observable): void
        }
        class InicializadorMundo {
        -random: Random
        +elegirZonasIniciales(mapa: Mapa, cantidad: int): Vector
        +colocarTazas(mundo: MundoJuego): void
        +colocarBanderas(mundo: MundoJuego): void
        }
        class IReglaJuego {
        +aplicar(mundo: MundoJuego, ahora: long, deltaSeg: double): void
        }
        class ReglaMovimiento {
        +aplicar(mundo: MundoJuego, ahora: long, deltaSeg: double): void
        }
        class ReglaAtaqueBot {
        +aplicar(mundo: MundoJuego, ahora: long, deltaSeg: double): void
        }
        class ReglaAtaqueJugador {
        +aplicar(mundo: MundoJuego, ahora: long, deltaSeg: double): void
        }
        class ReglaTazaCafe {
        +aplicar(mundo: MundoJuego, ahora: long, deltaSeg: double): void
        }
        class ReglaBonoEnergia {
        +aplicar(mundo: MundoJuego, ahora: long, deltaSeg: double): void
        }
        class ReglaVictoria {
        +aplicar(mundo: MundoJuego, ahora: long, deltaSeg: double): void
        }
        class CicloJuego {
        -mundo: MundoJuego
        -reglas: Vector
        -reloj: Clock
        -ultimoTick: long
        -activo: AtomicBoolean
        +run(): void
        +tick(ahora: long): void
        +detener(): void
        }
        class ServicioVisibilidad {
        +zonasVisibles(jugador: Jugador): Vector
        +crearSnapshot(mundo: MundoJuego, jugador: Jugador): EstadoVisible
        }
        class MainGame {
        +main(args: String[]): void$
        }
    }
    %% partida - Administración de partidas
    namespace PartidaLayer {
        class EstadoPartida {
        ESPERANDO_JUGADOR
        ELIGIENDO_ANIMALES
        EN_JUEGO
        TERMINADA
        }
        class GeneradorCodigo {
        -random: Random
        +generar(existentes: Set): String
        +normalizar(codigo: String): String$
        }
        class SelectorEspecies {
        -disponibles: Set
        -elecciones: Map
        +elegir(jugador: Jugador, especie: Especie): boolean
        +todosEligieron(): boolean
        +especiesBot(): Vector
        }
        class Partida {
        -codigo: String
        -estado: EstadoPartida
        -jugadores: Vector
        -selector: SelectorEspecies
        -mundo: MundoJuego
        -ciclo: CicloJuego
        -inicio: long
        +agregarJugador(jugador: Jugador): void
        +estaLlena(): boolean
        +elegirEspecie(jugador: Jugador, especie: Especie): void
        +iniciar(): void
        +enviarEstado(): void
        +terminar(ganador: Jugador): void
        +manejarDesconexion(jugador: Jugador): void
        +update(source: Object, ob: Observable): void
        }
        class AdministradorPartidas {
        -partidas: ConcurrentHashMap
        -generador: GeneradorCodigo
        +crearPartida(anfitrion: Jugador): Partida
        +unirse(codigo: String, invitado: Jugador): Partida
        +buscar(codigo: String): Partida
        +eliminar(codigo: String): void
        +getCantidadActivas(): int
        }
        class MainPartida {
        +main(args: String[]): void$
        }
    }
    %% red - Comunicación y protocolo
    namespace Red {
        class TipoMensaje {
        CREATE_MATCH
        JOIN_MATCH
        SELECT_ANIMAL
        MOVE_ANIMALS
        ATTACK
        VIEW_ZONE
        MATCH_CREATED
        MATCH_JOINED
        ERROR
        ANIMAL_SELECTION
        GAME_STARTED
        STATE_UPDATE
        GAME_OVER
        }
        class Mensaje {
        -tipo: TipoMensaje
        -payload: JsonObject
        +getTipo(): TipoMensaje
        +getPayload(tipo: Class): Object
        }
        class CodificadorJson {
        - instancia: CodificadorJson$
        -gson: Gson
        +getInstancia(): CodificadorJson$
        +codificar(mensaje: Mensaje): String
        +decodificar(linea: String): Mensaje
        }
        class FabricaMensajes {
        +crear(tipo: TipoMensaje, payload: Object): Mensaje$
        +error(razon: String): Mensaje$
        }
        class DatosCrearPartida {
        +nickname: String
        }
        class DatosUnirse {
        +nickname: String
        +codigo: String
        }
        class DatosElegirAnimal {
        +especie: Especie
        }
        class DatosMover {
        +idsAnimales: Vector
        +destinoX: double
        +destinoY: double
        }
        class DatosAtacar {
        +idsAnimales: Vector
        +idObjetivo: int
        }
        class DatosVerZona {
        +idZona: int
        }
        class DatosPartidaCreada {
        +codigo: String
        }
        class DatosSeleccion {
        +disponibles: Vector
        +eleccionRival: Especie
        }
        class DatosInicio {
        +mapa: VistaMapa
        +zonaPropia: int
        +manada: Vector
        }
        class EstadoVisible {
        +idZona: int
        +animales: Vector
        +tazas: Vector
        +bandera: Punto
        +eventos: Vector
        +danio: int
        +tazasConsumidas: int
        +botsEliminados: int
        }
        class ResumenPartida {
        +ganador: String
        +botsEliminados: Map
        +animalesPerdidos: Map
        +tazas: Map
        +duracionSeg: long
        }
        class VistaAnimal {
        +id: int
        +especie: Especie
        +esBot: boolean
        +esMio: boolean
        +x: double
        +y: double
        +energia: int
        }
        class VistaMapa {
        +zonas: Vector
        +caminos: Vector
        }
        class IComando {
        +ejecutar(cliente: ManejadorCliente, mensaje: Mensaje): void
        }
        class ComandoCrearPartida {
        +ejecutar(cliente: ManejadorCliente, mensaje: Mensaje): void
        }
        class ComandoUnirse {
        +ejecutar(cliente: ManejadorCliente, mensaje: Mensaje): void
        }
        class ComandoElegirAnimal {
        +ejecutar(cliente: ManejadorCliente, mensaje: Mensaje): void
        }
        class ComandoMover {
        +ejecutar(cliente: ManejadorCliente, mensaje: Mensaje): void
        }
        class ComandoAtacar {
        +ejecutar(cliente: ManejadorCliente, mensaje: Mensaje): void
        }
        class ComandoVerZona {
        +ejecutar(cliente: ManejadorCliente, mensaje: Mensaje): void
        }
        class EnrutadorMensajes {
        -comandos: Map
        +registrar(tipo: TipoMensaje, comando: IComando): void
        +enrutar(cliente: ManejadorCliente, mensaje: Mensaje): void
        }
        class ManejadorCliente {
        -socket: Socket
        -lector: BufferedReader
        -escritor: PrintWriter
        -enrutador: EnrutadorMensajes
        -jugador: Jugador
        +run(): void
        +enviar(mensaje: Mensaje): void
        +cerrar(): void
        }
        class ServidorJuego {
        -puerto: int
        -serverSocket: ServerSocket
        -hilos: ExecutorService
        -administrador: AdministradorPartidas
        -enrutador: EnrutadorMensajes
        -activo: boolean
        +iniciar(): void
        +detener(): void
        }
        class ServerMain {
        +main(args: String[]): void$
        }
        class MainRed {
        +main(args: String[]): void$
        }
    }
    Observable <|-- EntidadJuego
    Observable <|-- MundoJuego
    EntidadJuego <|-- Animal
    EntidadJuego <|-- TazaCafe
    EntidadJuego <|-- Bandera
    Animal <|-- AnimalJugador
    Animal <|-- AnimalBot
    Manada <|-- ManadaJugador
    Manada <|-- ManadaBot
    IObserver <|.. MundoJuego
    IObserver <|.. Partida
    IBuscadorRuta <|.. BuscadorBFS
    IBuscadorRuta <|.. BuscadorDijkstra
    IReglaJuego <|.. ReglaMovimiento
    IReglaJuego <|.. ReglaAtaqueBot
    IReglaJuego <|.. ReglaAtaqueJugador
    IReglaJuego <|.. ReglaTazaCafe
    IReglaJuego <|.. ReglaBonoEnergia
    IReglaJuego <|.. ReglaVictoria
    IComando <|.. ComandoCrearPartida
    IComando <|.. ComandoUnirse
    IComando <|.. ComandoElegirAnimal
    IComando <|.. ComandoMover
    IComando <|.. ComandoAtacar
    IComando <|.. ComandoVerZona
    Runnable <|.. CicloJuego
    Runnable <|.. ManejadorCliente
    EntidadJuego *-- Punto : posicion
    Animal --> Especie
    Animal --> Manada
    Animal --> Ruta
    AnimalJugador --> Animal : objetivo
    Manada --> Especie
    Manada o-- Animal : miembros
    ManadaJugador --> Jugador : dueno
    TazaCafe ..> ManadaJugador : consumir
    Bandera --> Jugador : dueno
    Bandera ..> Animal : detectar
    Mapa o-- Zona
    Mapa o-- Camino
    Zona ..> Punto
    Camino ..> Punto
    ConstructorMapa ..> Zona
    ConstructorMapa ..> Camino
    ConstructorMapa ..> Mapa : construir
    BuscadorBFS ..> Mapa
    BuscadorDijkstra ..> Mapa
    Ruta ..> Mapa
    Ruta o-- Punto
    Jugador --> ManadaJugador
    Jugador --> Bandera
    Jugador --> Especie
    Jugador --> ManejadorCliente
    MundoJuego --> Mapa
    MundoJuego o-- ManadaJugador
    MundoJuego o-- ManadaBot
    MundoJuego o-- TazaCafe
    MundoJuego o-- Bandera
    MundoJuego --> Jugador : ganador
    InicializadorMundo ..> MundoJuego
    InicializadorMundo ..> Mapa
    CicloJuego --> MundoJuego
    CicloJuego o-- IReglaJuego : reglas
    ServicioVisibilidad ..> MundoJuego
    ServicioVisibilidad ..> Jugador
    ServicioVisibilidad ..> EstadoVisible
    Partida --> EstadoPartida
    Partida o-- Jugador
    Partida --> SelectorEspecies
    Partida --> MundoJuego
    Partida --> CicloJuego
    Partida ..> ResumenPartida
    SelectorEspecies ..> Jugador
    SelectorEspecies ..> Especie
    AdministradorPartidas o-- Partida
    AdministradorPartidas --> GeneradorCodigo
    Mensaje --> TipoMensaje
    Mensaje ..> CodificadorJson
    FabricaMensajes ..> Mensaje
    FabricaMensajes ..> TipoMensaje
    CodificadorJson ..> Mensaje
    Mensaje ..> DatosCrearPartida
    Mensaje ..> DatosUnirse
    Mensaje ..> DatosElegirAnimal
    Mensaje ..> DatosMover
    Mensaje ..> DatosAtacar
    Mensaje ..> DatosVerZona
    Mensaje ..> DatosPartidaCreada
    Mensaje ..> DatosSeleccion
    Mensaje ..> DatosInicio
    Mensaje ..> EstadoVisible
    Mensaje ..> ResumenPartida
    DatosInicio --> VistaMapa
    DatosInicio ..> VistaAnimal
    EstadoVisible ..> VistaAnimal
    EstadoVisible ..> Punto
    VistaMapa ..> Zona
    VistaMapa ..> Camino
    EnrutadorMensajes o-- IComando : comandos
    ComandoCrearPartida ..> AdministradorPartidas
    ComandoUnirse ..> AdministradorPartidas
    ComandoElegirAnimal ..> Partida
    ComandoMover ..> Animal
    ComandoAtacar ..> Animal
    ComandoVerZona ..> ServicioVisibilidad
    ComandoCrearPartida ..> DatosCrearPartida
    ComandoUnirse ..> DatosUnirse
    ComandoElegirAnimal ..> DatosElegirAnimal
    ComandoMover ..> DatosMover
    ComandoAtacar ..> DatosAtacar
    ComandoVerZona ..> DatosVerZona
    ManejadorCliente --> EnrutadorMensajes
    ManejadorCliente --> Jugador
    ManejadorCliente ..> Mensaje
    ServidorJuego o-- ManejadorCliente : crea
    ServidorJuego --> AdministradorPartidas
    ServidorJuego --> EnrutadorMensajes
    ServerMain ..> ServidorJuego
    EntidadJuego ..> IObserver : notifica cambios
    MundoJuego ..> IObserver : notifica cambios
    MundoJuego ..> Partida : eventos de partida
```
